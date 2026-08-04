package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.StoreSettings

@Composable
fun AdminSettingsScreen(
    currentSettings: StoreSettings,
    onSaveSettings: (StoreSettings) -> Unit
) {
    var currencySymbol by remember { mutableStateOf(currentSettings.currencySymbol) }
    var taxRateStr by remember { mutableStateOf(currentSettings.taxPercentage.toString()) }
    var enableStripe by remember { mutableStateOf(currentSettings.stripeEnabled) }
    var enablePaypal by remember { mutableStateOf(currentSettings.paypalEnabled) }
    var enableRazorpay by remember { mutableStateOf(currentSettings.razorpayEnabled) }

    var saveSuccessMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("admin_settings_screen")
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Store Configuration & Settings",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "Configure global currencies, payment gateways, tax rules, and branding.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (saveSuccessMessage != null) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF10B981).copy(alpha = 0.2f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = saveSuccessMessage!!,
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF10B981), fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(10.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Text("Currency Symbol", style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("$", "€", "£", "₹").forEach { sym ->
                FilterChip(
                    selected = (currencySymbol == sym),
                    onClick = { currencySymbol = sym },
                    label = { Text(sym, fontWeight = FontWeight.Bold) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = taxRateStr,
            onValueChange = { taxRateStr = it },
            label = { Text("Tax Percentage (%)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Payment Gateways Control", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Enable Stripe Payments")
            Switch(checked = enableStripe, onCheckedChange = { enableStripe = it })
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Enable PayPal Integration")
            Switch(checked = enablePaypal, onCheckedChange = { enablePaypal = it })
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Enable Razorpay Integration")
            Switch(checked = enableRazorpay, onCheckedChange = { enableRazorpay = it })
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val tax = taxRateStr.toDoubleOrNull() ?: 5.0
                val updated = StoreSettings(
                    currencySymbol = currencySymbol,
                    taxPercentage = tax,
                    stripeEnabled = enableStripe,
                    paypalEnabled = enablePaypal,
                    razorpayEnabled = enableRazorpay
                )
                onSaveSettings(updated)
                saveSuccessMessage = "Store settings saved successfully!"
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_settings_button")
        ) {
            Text("Save Configuration", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
