package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.CouponEntity
import java.util.UUID

@Composable
fun AddCouponModal(
    onDismiss: () -> Unit,
    onSave: (CouponEntity) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var discountPercentStr by remember { mutableStateOf("20") }
    var discountFixedStr by remember { mutableStateOf("0") }
    var minPurchaseStr by remember { mutableStateOf("0") }
    var expiryDaysStr by remember { mutableStateOf("30") }
    var usageLimitStr by remember { mutableStateOf("100") }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("add_coupon_dialog"),
        confirmButton = {
            Button(
                onClick = {
                    val cleanCode = code.ifBlank { "PROMO" + (100..999).random() }.uppercase()
                    val percent = discountPercentStr.toIntOrNull() ?: 0
                    val fixed = discountFixedStr.toDoubleOrNull() ?: 0.0
                    val minPurchase = minPurchaseStr.toDoubleOrNull() ?: 0.0
                    val expiryDays = expiryDaysStr.toLongOrNull() ?: 30L
                    val limit = usageLimitStr.toIntOrNull() ?: 100

                    val coupon = CouponEntity(
                        id = "c_" + UUID.randomUUID().toString().take(8),
                        code = cleanCode,
                        discountPercent = percent,
                        discountFixed = fixed,
                        minPurchase = minPurchase,
                        expiryTimestamp = System.currentTimeMillis() + (expiryDays * 24 * 3600 * 1000),
                        usageLimit = limit,
                        timesUsed = 0,
                        isActive = true
                    )
                    onSave(coupon)
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Coupon", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
                Text("Cancel")
            }
        },
        title = {
            Text(
                text = "Create Discount Coupon",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it.uppercase() },
                    label = { Text("Coupon Code (e.g. FLASH50)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_coupon_code")
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = discountPercentStr,
                        onValueChange = { discountPercentStr = it },
                        label = { Text("Discount (%)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = discountFixedStr,
                        onValueChange = { discountFixedStr = it },
                        label = { Text("Or Fixed ($)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = minPurchaseStr,
                        onValueChange = { minPurchaseStr = it },
                        label = { Text("Min Purchase ($)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = expiryDaysStr,
                        onValueChange = { expiryDaysStr = it },
                        label = { Text("Valid Days") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = usageLimitStr,
                    onValueChange = { usageLimitStr = it },
                    label = { Text("Usage Limit Count") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}
