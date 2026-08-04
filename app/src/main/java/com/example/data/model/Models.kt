package com.example.data.model

import com.example.data.local.entity.ProductEntity

enum class UserRole {
    CUSTOMER,
    ADMIN
}

enum class OrderStatus {
    COMPLETED,
    REFUNDED,
    CANCELLED
}

enum class PaymentGateway(val displayName: String) {
    STRIPE("Stripe"),
    PAYPAL("PayPal"),
    RAZORPAY("Razorpay")
}

data class CartItem(
    val product: ProductEntity,
    val quantity: Int = 1
)

data class StoreSettings(
    val currencySymbol: String = "$",
    val taxPercentage: Double = 5.0,
    val downloadLinkValidityDays: Int = 365,
    val stripeEnabled: Boolean = true,
    val paypalEnabled: Boolean = true,
    val razorpayEnabled: Boolean = true
)

enum class Screen {
    MARKETPLACE,
    PRODUCT_DETAIL,
    CART_CHECKOUT,
    MY_DOWNLOADS,
    WISHLIST,
    CUSTOMER_PROFILE,
    ADMIN_DASHBOARD,
    ADMIN_PRODUCTS,
    ADMIN_ORDERS,
    ADMIN_USERS,
    ADMIN_COUPONS,
    ADMIN_REVIEWS,
    ADMIN_SETTINGS,
    AUTH
}
