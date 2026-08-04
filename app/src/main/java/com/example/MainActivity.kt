package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.FolderZip
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Screen
import com.example.ui.components.AppHeader
import com.example.ui.components.DownloadProgressDialog
import com.example.ui.components.InvoiceDialog
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AssetMarketViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: AssetMarketViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                AssetMarketApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun AssetMarketApp(viewModel: AssetMarketViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val products by viewModel.filteredProducts.collectAsStateWithLifecycle()
    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val wishlistIds by viewModel.wishlistProductIds.collectAsStateWithLifecycle()
    val selectedProduct by viewModel.selectedProduct.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsStateWithLifecycle()
    val sortBy by viewModel.sortBy.collectAsStateWithLifecycle()
    val appliedCoupon by viewModel.appliedCoupon.collectAsStateWithLifecycle()
    val selectedPaymentGateway by viewModel.selectedPaymentGateway.collectAsStateWithLifecycle()

    val downloads by viewModel.userDownloads.collectAsStateWithLifecycle()
    val userOrders by viewModel.userOrders.collectAsStateWithLifecycle()
    val allOrders by viewModel.allOrders.collectAsStateWithLifecycle()
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
    val allCoupons by viewModel.allCoupons.collectAsStateWithLifecycle()
    val allReviews by viewModel.allReviews.collectAsStateWithLifecycle()
    val storeSettings by viewModel.storeSettings.collectAsStateWithLifecycle()

    val invoiceOrder by viewModel.invoiceOrder.collectAsStateWithLifecycle()
    val downloadingProduct by viewModel.downloadingProduct.collectAsStateWithLifecycle()

    val totalCartCount = cartItems.sumOf { it.quantity }
    val wishlistProducts = allProducts.filter { wishlistIds.contains(it.id) }

    val productReviews = remember(selectedProduct, allReviews) {
        if (selectedProduct != null) {
            allReviews.filter { it.productId == selectedProduct!!.id }
        } else {
            emptyList()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (currentScreen != Screen.AUTH) {
                AppHeader(
                    currentScreen = currentScreen,
                    currentUser = currentUser,
                    cartCount = totalCartCount,
                    wishlistCount = wishlistIds.size,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { viewModel.setSearchQuery(it) },
                    onToggleRole = { viewModel.toggleRole() },
                    onNavigate = { viewModel.navigateTo(it) },
                    onBack = { viewModel.navigateTo(if (currentUser?.role == "ADMIN") Screen.ADMIN_DASHBOARD else Screen.MARKETPLACE) }
                )
            }
        },
        bottomBar = {
            // Show bottom bar for customer screens
            if (currentUser?.role != "ADMIN" && currentScreen != Screen.AUTH) {
                NavigationBar(modifier = Modifier.testTag("customer_bottom_bar")) {
                    NavigationBarItem(
                        selected = (currentScreen == Screen.MARKETPLACE),
                        onClick = { viewModel.navigateTo(Screen.MARKETPLACE) },
                        icon = { Icon(Icons.Outlined.Storefront, contentDescription = "Store") },
                        label = { Text("Store") },
                        modifier = Modifier.testTag("bottom_nav_store")
                    )
                    NavigationBarItem(
                        selected = (currentScreen == Screen.MY_DOWNLOADS),
                        onClick = { viewModel.navigateTo(Screen.MY_DOWNLOADS) },
                        icon = { Icon(Icons.Outlined.FolderZip, contentDescription = "Downloads") },
                        label = { Text("Downloads") },
                        modifier = Modifier.testTag("bottom_nav_downloads")
                    )
                    NavigationBarItem(
                        selected = (currentScreen == Screen.WISHLIST),
                        onClick = { viewModel.navigateTo(Screen.WISHLIST) },
                        icon = { Icon(Icons.Outlined.FavoriteBorder, contentDescription = "Wishlist") },
                        label = { Text("Wishlist") },
                        modifier = Modifier.testTag("bottom_nav_wishlist")
                    )
                    NavigationBarItem(
                        selected = (currentScreen == Screen.CUSTOMER_PROFILE),
                        onClick = { viewModel.navigateTo(Screen.CUSTOMER_PROFILE) },
                        icon = { Icon(Icons.Outlined.Person, contentDescription = "Account") },
                        label = { Text("Account") },
                        modifier = Modifier.testTag("bottom_nav_account")
                    )
                }
            } else if (currentUser?.role == "ADMIN" && currentScreen != Screen.AUTH) {
                // Bottom bar for admin control panel
                NavigationBar(modifier = Modifier.testTag("admin_bottom_bar")) {
                    NavigationBarItem(
                        selected = (currentScreen == Screen.ADMIN_DASHBOARD),
                        onClick = { viewModel.navigateTo(Screen.ADMIN_DASHBOARD) },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                        label = { Text("Dashboard") },
                        modifier = Modifier.testTag("admin_nav_dashboard")
                    )
                    NavigationBarItem(
                        selected = (currentScreen == Screen.ADMIN_PRODUCTS),
                        onClick = { viewModel.navigateTo(Screen.ADMIN_PRODUCTS) },
                        icon = { Icon(Icons.Default.Inventory, contentDescription = "Products") },
                        label = { Text("Products") },
                        modifier = Modifier.testTag("admin_nav_products")
                    )
                    NavigationBarItem(
                        selected = (currentScreen == Screen.ADMIN_ORDERS),
                        onClick = { viewModel.navigateTo(Screen.ADMIN_ORDERS) },
                        icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Orders") },
                        label = { Text("Orders") },
                        modifier = Modifier.testTag("admin_nav_orders")
                    )
                    NavigationBarItem(
                        selected = (currentScreen == Screen.ADMIN_USERS),
                        onClick = { viewModel.navigateTo(Screen.ADMIN_USERS) },
                        icon = { Icon(Icons.Default.Group, contentDescription = "Users") },
                        label = { Text("Users") },
                        modifier = Modifier.testTag("admin_nav_users")
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentScreen) {
                Screen.MARKETPLACE -> {
                    MarketplaceHomeScreen(
                        products = products,
                        categories = categories,
                        selectedCategoryId = selectedCategoryId,
                        selectedSort = sortBy,
                        wishlistIds = wishlistIds,
                        onSelectCategory = { viewModel.selectCategory(it) },
                        onSelectSort = { viewModel.setSortBy(it) },
                        onProductClick = { viewModel.openProductDetail(it) },
                        onAddToCart = { viewModel.addToCart(it) },
                        onToggleWishlist = { viewModel.toggleWishlist(it.id) }
                    )
                }

                Screen.PRODUCT_DETAIL -> {
                    ProductDetailScreen(
                        product = selectedProduct,
                        reviews = productReviews,
                        isInWishlist = wishlistIds.contains(selectedProduct?.id ?: ""),
                        onAddToCart = { viewModel.addToCart(it) },
                        onToggleWishlist = { viewModel.toggleWishlist(it.id) },
                        onSubmitReview = { pId, rating, comment ->
                            viewModel.submitReview(pId, rating, comment) {}
                        },
                        onBack = { viewModel.navigateTo(Screen.MARKETPLACE) }
                    )
                }

                Screen.CART_CHECKOUT -> {
                    CartCheckoutScreen(
                        cartItems = cartItems,
                        appliedCoupon = appliedCoupon,
                        selectedPaymentGateway = selectedPaymentGateway,
                        onUpdateQuantity = { pId, q -> viewModel.updateCartQuantity(pId, q) },
                        onRemoveItem = { pId -> viewModel.removeFromCart(pId) },
                        onApplyCoupon = { code, cb -> viewModel.applyCoupon(code, cb) },
                        onRemoveCoupon = { viewModel.removeCoupon() },
                        onSelectPaymentGateway = { viewModel.setPaymentGateway(it) },
                        onCheckout = { viewModel.checkout {} },
                        onContinueShopping = { viewModel.navigateTo(Screen.MARKETPLACE) }
                    )
                }

                Screen.MY_DOWNLOADS -> {
                    CustomerDownloadsScreen(
                        downloads = downloads,
                        products = allProducts,
                        orders = userOrders,
                        onStartDownload = { viewModel.startDownload(it) },
                        onShowInvoice = { viewModel.showInvoice(it) },
                        onBrowseMarket = { viewModel.navigateTo(Screen.MARKETPLACE) }
                    )
                }

                Screen.WISHLIST -> {
                    WishlistScreen(
                        wishlistProducts = wishlistProducts,
                        wishlistIds = wishlistIds,
                        onProductClick = { viewModel.openProductDetail(it) },
                        onAddToCart = { viewModel.addToCart(it) },
                        onToggleWishlist = { viewModel.toggleWishlist(it.id) },
                        onBrowseMarket = { viewModel.navigateTo(Screen.MARKETPLACE) }
                    )
                }

                Screen.CUSTOMER_PROFILE -> {
                    CustomerProfileScreen(
                        user = currentUser,
                        orders = userOrders,
                        downloadsCount = downloads.size,
                        wishlistCount = wishlistIds.size,
                        onNavigate = { viewModel.navigateTo(it) },
                        onShowInvoice = { viewModel.showInvoice(it) },
                        onToggleRole = { viewModel.toggleRole() },
                        onLogout = { viewModel.logout() }
                    )
                }

                Screen.AUTH -> {
                    AuthScreen(
                        onPerformLogin = { email, password, callback ->
                            viewModel.performLogin(email, password, callback)
                        },
                        onPerformRegister = { email, name, password, role, callback ->
                            viewModel.performRegister(email, name, password, role, callback)
                        }
                    )
                }

                Screen.ADMIN_DASHBOARD -> {
                    AdminDashboardScreen(
                        products = allProducts,
                        orders = allOrders,
                        users = allUsers,
                        onNavigate = { viewModel.navigateTo(it) },
                        onShowInvoice = { viewModel.showInvoice(it) }
                    )
                }

                Screen.ADMIN_PRODUCTS -> {
                    AdminProductsScreen(
                        products = allProducts,
                        categories = categories,
                        onSaveProduct = { viewModel.adminSaveProduct(it) },
                        onDeleteProduct = { viewModel.adminDeleteProduct(it) }
                    )
                }

                Screen.ADMIN_ORDERS -> {
                    AdminOrdersScreen(
                        orders = allOrders,
                        onUpdateOrderStatus = { orderId, status ->
                            viewModel.adminUpdateOrderStatus(orderId, status)
                        },
                        onShowInvoice = { viewModel.showInvoice(it) }
                    )
                }

                Screen.ADMIN_USERS -> {
                    AdminUsersScreen(
                        users = allUsers,
                        onUpdateUserBlock = { uId, isBlocked -> viewModel.adminUpdateUserBlock(uId, isBlocked) },
                        onUpdateUserRole = { uId, role -> viewModel.adminUpdateUserRole(uId, role) }
                    )
                }

                Screen.ADMIN_COUPONS -> {
                    AdminCouponsScreen(
                        coupons = allCoupons,
                        onSaveCoupon = { viewModel.adminSaveCoupon(it) },
                        onDeleteCoupon = { viewModel.adminDeleteCoupon(it) }
                    )
                }

                Screen.ADMIN_REVIEWS -> {
                    AdminReviewsScreen(
                        reviews = allReviews,
                        products = allProducts,
                        onUpdateApproval = { rId, isApproved -> viewModel.adminUpdateReviewApproval(rId, isApproved) },
                        onDeleteReview = { rId -> viewModel.adminDeleteReview(rId) }
                    )
                }

                Screen.ADMIN_SETTINGS -> {
                    AdminSettingsScreen(
                        currentSettings = storeSettings,
                        onSaveSettings = { viewModel.updateStoreSettings(it) }
                    )
                }
            }
        }
    }

    // Dialog Overlays
    if (invoiceOrder != null) {
        InvoiceDialog(
            order = invoiceOrder!!.first,
            items = invoiceOrder!!.second,
            onDismiss = { viewModel.dismissInvoice() },
            onGoToDownloads = {
                viewModel.dismissInvoice()
                viewModel.navigateTo(Screen.MY_DOWNLOADS)
            }
        )
    }

    if (downloadingProduct != null) {
        DownloadProgressDialog(
            product = downloadingProduct!!,
            onDismiss = { viewModel.dismissDownload() }
        )
    }
}
