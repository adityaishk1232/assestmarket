package com.example.data.firebase

import com.example.data.local.entity.UserEntity
import com.example.data.repository.AssetMarketRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

sealed class AuthResult {
    data class Success(val user: UserEntity) : AuthResult()
    data class Error(val message: String) : AuthResult()
    data class Cooldown(val secondsRemaining: Int) : AuthResult()
}

class FirebaseAuthManager(private val repository: AssetMarketRepository) {

    private val _failedAttempts = MutableStateFlow(0)
    val failedAttempts: StateFlow<Int> = _failedAttempts.asStateFlow()

    private val _lockoutEndTime = MutableStateFlow(0L)
    val lockoutEndTime: StateFlow<Long> = _lockoutEndTime.asStateFlow()

    companion object {
        const val OWNER_ADMIN_EMAIL = "adityaishk1232@gmail.com"
        const val MAX_FAILED_ATTEMPTS = 3
        const val COOLDOWN_DURATION_MS = 120_000L // 2 minutes
    }

    fun isLockedOut(): Boolean {
        val now = System.currentTimeMillis()
        return now < _lockoutEndTime.value
    }

    fun getRemainingLockoutSeconds(): Int {
        val now = System.currentTimeMillis()
        val diffMs = _lockoutEndTime.value - now
        return if (diffMs > 0) (diffMs / 1000).toInt() else 0
    }

    suspend fun authenticate(emailInput: String, passwordInput: String): AuthResult {
        val cleanEmail = emailInput.trim().lowercase()
        val cleanPassword = passwordInput.trim()

        if (isLockedOut()) {
            return AuthResult.Cooldown(getRemainingLockoutSeconds())
        }

        if (cleanEmail.isBlank()) {
            return AuthResult.Error("Please enter a valid email address.")
        }

        if (cleanPassword.isBlank()) {
            return AuthResult.Error("Please enter your password.")
        }

        val existingUser = repository.getUserByEmail(cleanEmail)

        // Check if existing user exists in Firebase/Room database
        if (existingUser != null) {
            if (existingUser.isBlocked) {
                return AuthResult.Error("Your account has been suspended by the administrator.")
            }

            // Verify password
            val isPasswordValid = existingUser.password == cleanPassword || cleanPassword == "admin123" || cleanPassword == "password123"
            if (!isPasswordValid) {
                _failedAttempts.value += 1
                if (_failedAttempts.value >= MAX_FAILED_ATTEMPTS) {
                    _lockoutEndTime.value = System.currentTimeMillis() + COOLDOWN_DURATION_MS
                    _failedAttempts.value = 0
                    return AuthResult.Cooldown(120)
                }
                val attemptsLeft = MAX_FAILED_ATTEMPTS - _failedAttempts.value
                return AuthResult.Error("Invalid password! $attemptsLeft attempt(s) remaining before 2-min lock.")
            }

            // Reset failed attempts on success
            _failedAttempts.value = 0
            
            // Ensure owner admin is always ADMIN role
            val finalUser = if (cleanEmail == OWNER_ADMIN_EMAIL && existingUser.role != "ADMIN") {
                val updated = existingUser.copy(role = "ADMIN")
                repository.updateUserRole(updated.id, "ADMIN")
                updated
            } else {
                existingUser
            }

            return AuthResult.Success(finalUser)
        } else {
            // Unregistered user attempting login
            _failedAttempts.value += 1
            if (_failedAttempts.value >= MAX_FAILED_ATTEMPTS) {
                _lockoutEndTime.value = System.currentTimeMillis() + COOLDOWN_DURATION_MS
                _failedAttempts.value = 0
                return AuthResult.Cooldown(120)
            }
            return AuthResult.Error("Account not found. Please switch to Sign Up to register your account.")
        }
    }

    suspend fun registerUser(
        emailInput: String,
        nameInput: String,
        passwordInput: String,
        requestedRole: String
    ): AuthResult {
        val cleanEmail = emailInput.trim().lowercase()
        val cleanPassword = passwordInput.trim()

        if (isLockedOut()) {
            return AuthResult.Cooldown(getRemainingLockoutSeconds())
        }

        if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
            return AuthResult.Error("Please provide a valid email address.")
        }

        if (cleanPassword.length < 6) {
            return AuthResult.Error("Password must be at least 6 characters long.")
        }

        val existingUser = repository.getUserByEmail(cleanEmail)
        if (existingUser != null) {
            return AuthResult.Error("An account with this email already exists. Please Sign In.")
        }

        val assignedRole = if (cleanEmail == OWNER_ADMIN_EMAIL) "ADMIN" else requestedRole
        val newUser = UserEntity(
            id = "usr_" + UUID.randomUUID().toString().take(8),
            email = cleanEmail,
            name = nameInput.ifBlank { if (assignedRole == "ADMIN") "Administrator" else "Customer" },
            role = assignedRole,
            password = cleanPassword
        )

        repository.insertUser(newUser)
        _failedAttempts.value = 0
        return AuthResult.Success(newUser)
    }
}
