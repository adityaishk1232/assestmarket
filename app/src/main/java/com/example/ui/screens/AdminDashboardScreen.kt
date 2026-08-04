package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.OrderEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.UserEntity
import com.example.data.model.Screen

@Composable
fun AdminDashboardScreen(
    products: List<ProductEntity>,
    orders: List<OrderEntity>,
    users: List<UserEntity>,
    onNavigate: (Screen) -> Unit,
    onShowInvoice: (OrderEntity) -> Unit
) {
    val totalRevenue = orders.filter { it.status == "COMPLETED" }.sumOf { it.totalAmount }
    val totalSalesCount = orders.filter { it.status == "COMPLETED" }.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("admin_dashboard_screen")
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header Title
        Text(
            text = "Admin Analytics Dashboard",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "Real-time store performance, revenue, orders, and user insights.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // KPI Metric Cards Grid
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF10B981).copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981)),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Total Revenue", style = MaterialTheme.typography.labelSmall, color = Color(0xFF065F46))
                    Text(
                        text = "$${String.format("%.2f", totalRevenue)}",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold, color = Color(0xFF047857))
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF6366F1).copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6366F1)),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Total Orders", style = MaterialTheme.typography.labelSmall, color = Color(0xFF3730A3))
                    Text(
                        text = "$totalSalesCount",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold, color = Color(0xFF4338CA))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B)),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Active Products", style = MaterialTheme.typography.labelSmall, color = Color(0xFF92400E))
                    Text(
                        text = "${products.size}",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold, color = Color(0xFFD97706))
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF06B6D4).copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF06B6D4)),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Registered Users", style = MaterialTheme.typography.labelSmall, color = Color(0xFF155E75))
                    Text(
                        text = "${users.size}",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold, color = Color(0xFF0891B2))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Admin Management Shortcuts Grid
        Text(
            text = "Store Management",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                AdminNavigationCard(
                    title = "Manage Products",
                    subtitle = "Create, edit & set prices",
                    icon = Icons.Default.Inventory,
                    color = Color(0xFF6366F1),
                    onClick = { onNavigate(Screen.ADMIN_PRODUCTS) },
                    modifier = Modifier.weight(1f).testTag("nav_admin_products")
                )
                AdminNavigationCard(
                    title = "Manage Orders",
                    subtitle = "Status, refunds & exports",
                    icon = Icons.Default.ReceiptLong,
                    color = Color(0xFF10B981),
                    onClick = { onNavigate(Screen.ADMIN_ORDERS) },
                    modifier = Modifier.weight(1f).testTag("nav_admin_orders")
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                AdminNavigationCard(
                    title = "Manage Users",
                    subtitle = "Roles & block controls",
                    icon = Icons.Default.Group,
                    color = Color(0xFFF59E0B),
                    onClick = { onNavigate(Screen.ADMIN_USERS) },
                    modifier = Modifier.weight(1f).testTag("nav_admin_users")
                )
                AdminNavigationCard(
                    title = "Coupons & Discounts",
                    subtitle = "Create promo codes",
                    icon = Icons.Default.LocalOffer,
                    color = Color(0xFFEC4899),
                    onClick = { onNavigate(Screen.ADMIN_COUPONS) },
                    modifier = Modifier.weight(1f).testTag("nav_admin_coupons")
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                AdminNavigationCard(
                    title = "Moderate Reviews",
                    subtitle = "Approve & clean feedback",
                    icon = Icons.Default.RateReview,
                    color = Color(0xFF8B5CF6),
                    onClick = { onNavigate(Screen.ADMIN_REVIEWS) },
                    modifier = Modifier.weight(1f).testTag("nav_admin_reviews")
                )
                AdminNavigationCard(
                    title = "Store Settings",
                    subtitle = "Currencies & Tax rates",
                    icon = Icons.Default.Settings,
                    color = Color(0xFF64748B),
                    onClick = { onNavigate(Screen.ADMIN_SETTINGS) },
                    modifier = Modifier.weight(1f).testTag("nav_admin_settings")
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Recent Orders List Preview
        Text(
            text = "Recent Customer Purchases",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (orders.isEmpty()) {
            Text("No recent orders recorded.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            orders.take(5).forEach { order ->
                Surface(
                    onClick = { onShowInvoice(order) },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("${order.id} • ${order.userName}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("Gateway: ${order.paymentGateway} • Status: ${order.status}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("$${String.format("%.2f", order.totalAmount)}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun AdminNavigationCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = color, modifier = Modifier.size(22.dp))
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(text = title, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                Text(text = subtitle, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
