package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.firebase.FirebaseAuthManager
import com.example.data.firebase.AuthResult
import com.example.data.local.entity.*
import com.example.data.model.*
import com.example.data.repository.AssetMarketRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

enum class SortOption(val title: String) {
    POPULARITY("Popularity"),
    NEWEST("Newest"),
    PRICE_LOW_HIGH("Price: Low to High"),
    PRICE_HIGH_LOW("Price: High to Low"),
    RATING("Highest Rated")
}

class AssetMarketViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AssetMarketRepository
    private val dao = AppDatabase.getDatabase(application).appDao()

    init {
        repository = AssetMarketRepository(dao)
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    // Database Flows
    val allProducts: StateFlow<List<ProductEntity>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeProducts: StateFlow<List<ProductEntity>> = repository.activeProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<CategoryEntity>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOrders: StateFlow<List<OrderEntity>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<UserEntity>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCoupons: StateFlow<List<CouponEntity>> = repository.allCoupons
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allReviews: StateFlow<List<ReviewEntity>> = repository.allReviews
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active User State
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    // Navigation State
    private val _currentScreen = MutableStateFlow(Screen.AUTH)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _selectedProduct = MutableStateFlow<ProductEntity?>(null)
    val selectedProduct: StateFlow<ProductEntity?> = _selectedProduct.asStateFlow()

    // Filtering & Searching State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    val selectedCategoryId: StateFlow<String?> = _selectedCategoryId.asStateFlow()

    private val _sortBy = MutableStateFlow(SortOption.POPULARITY)
    val sortBy: StateFlow<SortOption> = _sortBy.asStateFlow()

    // Shopping Cart State
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _appliedCoupon = MutableStateFlow<CouponEntity?>(null)
    val appliedCoupon: StateFlow<CouponEntity?> = _appliedCoupon.asStateFlow()

    private val _selectedPaymentGateway = MutableStateFlow(PaymentGateway.STRIPE)
    val selectedPaymentGateway: StateFlow<PaymentGateway> = _selectedPaymentGateway.asStateFlow()

    // Download & Invoice Dialog State
    private val _invoiceOrder = MutableStateFlow<Pair<OrderEntity, List<OrderItemEntity>>?>(null)
    val invoiceOrder: StateFlow<Pair<OrderEntity, List<OrderItemEntity>>?> = _invoiceOrder.asStateFlow()

    private val _downloadingProduct = MutableStateFlow<ProductEntity?>(null)
    val downloadingProduct: StateFlow<ProductEntity?> = _downloadingProduct.asStateFlow()

    // Wishlist Flow for current user
    val wishlistProductIds: StateFlow<Set<String>> = _currentUser.flatMapLatest { user ->
        if (user != null) {
            repository.getWishlistProductIds(user.id).map { it.toSet() }
        } else {
            flowOf(emptySet())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    // Downloads Flow for current user
    val userDownloads: StateFlow<List<DownloadEntity>> = _currentUser.flatMapLatest { user ->
        if (user != null) {
            repository.getDownloadsForUser(user.id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Orders Flow for current user
    val userOrders: StateFlow<List<OrderEntity>> = _currentUser.flatMapLatest { user ->
        if (user != null) {
            repository.getOrdersForUser(user.id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Store Settings
    private val _storeSettings = MutableStateFlow(StoreSettings())
    val storeSettings: StateFlow<StoreSettings> = _storeSettings.asStateFlow()

    // Filtered Products for Customer view
    val filteredProducts: StateFlow<List<ProductEntity>> = combine(
        activeProducts,
        _searchQuery,
        _selectedCategoryId,
        _sortBy
    ) { products, query, categoryId, sort ->
        var list = products.filter { prod ->
            val matchesQuery = query.isBlank() ||
                    prod.title.contains(query, ignoreCase = true) ||
                    prod.tags.contains(query, ignoreCase = true) ||
                    prod.description.contains(query, ignoreCase = true)
            val matchesCat = categoryId == null || prod.categoryId == categoryId
            matchesQuery && matchesCat
        }

        list = when (sort) {
            SortOption.POPULARITY -> list.sortedByDescending { it.salesCount }
            SortOption.NEWEST -> list.sortedByDescending { it.createdAt }
            SortOption.PRICE_LOW_HIGH -> list.sortedBy { it.discountPrice ?: it.price }
            SortOption.PRICE_HIGH_LOW -> list.sortedByDescending { it.discountPrice ?: it.price }
            SortOption.RATING -> list.sortedByDescending { it.rating }
        }

        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // User / Auth actions
    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun openProductDetail(product: ProductEntity) {
        _selectedProduct.value = product
        _currentScreen.value = Screen.PRODUCT_DETAIL
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(categoryId: String?) {
        _selectedCategoryId.value = categoryId
    }

    fun setSortBy(option: SortOption) {
        _sortBy.value = option
    }

    val authManager by lazy { FirebaseAuthManager(repository) }

    fun performLogin(email: String, password: String, onResult: (AuthResult) -> Unit) {
        viewModelScope.launch {
            val result = authManager.authenticate(email, password)
            if (result is AuthResult.Success) {
                _currentUser.value = result.user
                if (result.user.role == "ADMIN") {
                    _currentScreen.value = Screen.ADMIN_DASHBOARD
                } else {
                    _currentScreen.value = Screen.MARKETPLACE
                }
            }
            onResult(result)
        }
    }

    fun performRegister(
        email: String,
        name: String,
        password: String,
        role: String,
        onResult: (AuthResult) -> Unit
    ) {
        viewModelScope.launch {
            val result = authManager.registerUser(email, name, password, role)
            if (result is AuthResult.Success) {
                _currentUser.value = result.user
                if (result.user.role == "ADMIN") {
                    _currentScreen.value = Screen.ADMIN_DASHBOARD
                } else {
                    _currentScreen.value = Screen.MARKETPLACE
                }
            }
            onResult(result)
        }
    }

    fun toggleRole() {
        logout()
    }

    fun loginUser(email: String, name: String, role: UserRole) {
        viewModelScope.launch {
            val cleanEmail = email.trim()
            val isOwnerAdmin = cleanEmail.equals("adityaishk1232@gmail.com", ignoreCase = true) ||
                               cleanEmail.equals("admin@assetmarket.com", ignoreCase = true)
            val effectiveRole = if (isOwnerAdmin) "ADMIN" else role.name

            var existing = repository.getUserByEmail(cleanEmail)
            if (existing == null) {
                existing = UserEntity(
                    id = "usr_" + UUID.randomUUID().toString().take(8),
                    email = cleanEmail,
                    name = name.ifBlank { if (effectiveRole == "ADMIN") "Store Owner" else "Customer" },
                    role = effectiveRole
                )
                repository.insertUser(existing)
            } else if (isOwnerAdmin && existing.role != "ADMIN") {
                // Ensure owner admin email always has ADMIN role
                existing = existing.copy(role = "ADMIN")
                repository.updateUserRole(existing.id, "ADMIN")
            }
            _currentUser.value = existing
            if (existing.role == "ADMIN") {
                _currentScreen.value = Screen.ADMIN_DASHBOARD
            } else {
                _currentScreen.value = Screen.MARKETPLACE
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _currentScreen.value = Screen.AUTH
    }

    // Cart actions
    fun addToCart(product: ProductEntity) {
        val currentList = _cartItems.value.toMutableList()
        val existingIndex = currentList.indexOfFirst { it.product.id == product.id }
        if (existingIndex >= 0) {
            val item = currentList[existingIndex]
            currentList[existingIndex] = item.copy(quantity = item.quantity + 1)
        } else {
            currentList.add(CartItem(product, 1))
        }
        _cartItems.value = currentList
    }

    fun removeFromCart(productId: String) {
        _cartItems.value = _cartItems.value.filter { it.product.id != productId }
    }

    fun updateCartQuantity(productId: String, quantity: Int) {
        if (quantity <= 0) {
            removeFromCart(productId)
            return
        }
        val currentList = _cartItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            currentList[index] = currentList[index].copy(quantity = quantity)
            _cartItems.value = currentList
        }
    }

    fun applyCoupon(code: String, onResult: (Boolean, String) -> Unit) {
        val cleanCode = code.trim().uppercase()
        if (cleanCode.isBlank()) {
            onResult(false, "Please enter a coupon code.")
            return
        }
        viewModelScope.launch {
            val coupon = repository.getCouponByCode(cleanCode)
            if (coupon == null || !coupon.isActive) {
                onResult(false, "Invalid or expired coupon code.")
                return@launch
            }
            if (coupon.expiryTimestamp < System.currentTimeMillis()) {
                onResult(false, "This coupon code has expired.")
                return@launch
            }
            val subtotal = _cartItems.value.sumOf { (it.product.discountPrice ?: it.product.price) * it.quantity }
            if (coupon.minPurchase > 0 && subtotal < coupon.minPurchase) {
                onResult(false, "Minimum purchase of $${coupon.minPurchase} required for this coupon.")
                return@launch
            }
            _appliedCoupon.value = coupon
            onResult(true, "Coupon '${coupon.code}' applied successfully!")
        }
    }

    fun removeCoupon() {
        _appliedCoupon.value = null
    }

    fun setPaymentGateway(gateway: PaymentGateway) {
        _selectedPaymentGateway.value = gateway
    }

    fun checkout(onSuccess: (OrderEntity) -> Unit) {
        val user = _currentUser.value ?: return
        val items = _cartItems.value
        if (items.isEmpty()) return

        val subtotal = items.sumOf { (it.product.discountPrice ?: it.product.price) * it.quantity }
        val coupon = _appliedCoupon.value

        var discount = 0.0
        if (coupon != null) {
            if (coupon.discountPercent > 0) {
                discount = subtotal * (coupon.discountPercent / 100.0)
            } else if (coupon.discountFixed > 0) {
                discount = coupon.discountFixed
            }
        }
        val total = (subtotal - discount).coerceAtLeast(0.0)

        val orderId = "ORD-" + (10000..99999).random()
        val order = OrderEntity(
            id = orderId,
            userId = user.id,
            userEmail = user.email,
            userName = user.name,
            totalAmount = total,
            discountAmount = discount,
            couponCode = coupon?.code,
            paymentGateway = _selectedPaymentGateway.value.displayName,
            status = "COMPLETED"
        )

        val orderItems = items.map { cartItem ->
            OrderItemEntity(
                id = "item_" + UUID.randomUUID().toString().take(8),
                orderId = orderId,
                productId = cartItem.product.id,
                productTitle = cartItem.product.title,
                priceAtPurchase = cartItem.product.discountPrice ?: cartItem.product.price,
                versionAtPurchase = cartItem.product.version,
                imageResName = cartItem.product.imageResName
            )
        }

        val downloads = items.map { cartItem ->
            DownloadEntity(
                id = "dl_" + UUID.randomUUID().toString().take(8),
                userId = user.id,
                productId = cartItem.product.id,
                orderId = orderId,
                versionDownloaded = cartItem.product.version
            )
        }

        viewModelScope.launch {
            repository.createOrder(order, orderItems, downloads)
            // Clear cart
            _cartItems.value = emptyList()
            _appliedCoupon.value = null

            // Show invoice dialog
            _invoiceOrder.value = Pair(order, orderItems)

            onSuccess(order)
        }
    }

    fun showInvoice(order: OrderEntity) {
        viewModelScope.launch {
            val itemsFlow = repository.getOrderItems(order.id)
            val items = itemsFlow.first()
            _invoiceOrder.value = Pair(order, items)
        }
    }

    fun dismissInvoice() {
        _invoiceOrder.value = null
    }

    fun toggleWishlist(productId: String) {
        val user = _currentUser.value ?: return
        val currentWishlist = wishlistProductIds.value
        viewModelScope.launch {
            if (currentWishlist.contains(productId)) {
                repository.removeFromWishlist(user.id, productId)
            } else {
                repository.addToWishlist(user.id, productId)
            }
        }
    }

    fun startDownload(product: ProductEntity) {
        _downloadingProduct.value = product
    }

    fun dismissDownload() {
        _downloadingProduct.value = null
    }

    fun submitReview(productId: String, rating: Float, comment: String, onSubmitted: () -> Unit) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val review = ReviewEntity(
                id = "rev_" + UUID.randomUUID().toString().take(8),
                productId = productId,
                userId = user.id,
                userName = user.name,
                rating = rating,
                comment = comment,
                isApproved = true
            )
            repository.insertReview(review)
            onSubmitted()
        }
    }

    // Admin Actions
    fun adminSaveProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.insertProduct(product)
        }
    }

    fun adminDeleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }

    fun adminUpdateOrderStatus(orderId: String, status: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, status)
        }
    }

    fun adminUpdateUserBlock(userId: String, isBlocked: Boolean) {
        viewModelScope.launch {
            repository.updateUserBlockStatus(userId, isBlocked)
        }
    }

    fun adminUpdateUserRole(userId: String, role: String) {
        viewModelScope.launch {
            repository.updateUserRole(userId, role)
        }
    }

    fun adminSaveCoupon(coupon: CouponEntity) {
        viewModelScope.launch {
            repository.insertCoupon(coupon)
        }
    }

    fun adminDeleteCoupon(couponId: String) {
        viewModelScope.launch {
            repository.deleteCoupon(couponId)
        }
    }

    fun adminUpdateReviewApproval(reviewId: String, isApproved: Boolean) {
        viewModelScope.launch {
            repository.updateReviewApproval(reviewId, isApproved)
        }
    }

    fun adminDeleteReview(reviewId: String) {
        viewModelScope.launch {
            repository.deleteReview(reviewId)
        }
    }

    fun updateStoreSettings(settings: StoreSettings) {
        _storeSettings.value = settings
    }
}
