package com.example.data.repository

import com.example.data.local.dao.AppDao
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow

class AssetMarketRepository(private val dao: AppDao) {

    // Flows
    val allProducts: Flow<List<ProductEntity>> = dao.getAllProducts()
    val activeProducts: Flow<List<ProductEntity>> = dao.getActiveProducts()
    val allCategories: Flow<List<CategoryEntity>> = dao.getAllCategories()
    val allOrders: Flow<List<OrderEntity>> = dao.getAllOrders()
    val allUsers: Flow<List<UserEntity>> = dao.getAllUsers()
    val allCoupons: Flow<List<CouponEntity>> = dao.getAllCoupons()
    val allReviews: Flow<List<ReviewEntity>> = dao.getAllReviews()

    fun getOrdersForUser(userId: String): Flow<List<OrderEntity>> = dao.getOrdersForUser(userId)
    fun getOrderItems(orderId: String): Flow<List<OrderItemEntity>> = dao.getOrderItems(orderId)
    fun getDownloadsForUser(userId: String): Flow<List<DownloadEntity>> = dao.getDownloadsForUser(userId)
    fun getWishlistProductIds(userId: String): Flow<List<String>> = dao.getWishlistProductIds(userId)
    fun getApprovedReviewsForProduct(productId: String): Flow<List<ReviewEntity>> = dao.getApprovedReviewsForProduct(productId)

    suspend fun getProductById(id: String): ProductEntity? = dao.getProductById(id)
    suspend fun getUserByEmail(email: String): UserEntity? = dao.getUserByEmail(email)
    suspend fun getCouponByCode(code: String): CouponEntity? = dao.getCouponByCode(code)

    // Mutations
    suspend fun insertUser(user: UserEntity) = dao.insertUser(user)
    suspend fun updateUserBlockStatus(userId: String, isBlocked: Boolean) = dao.updateUserBlockStatus(userId, isBlocked)
    suspend fun updateUserRole(userId: String, role: String) = dao.updateUserRole(userId, role)

    suspend fun insertProduct(product: ProductEntity) = dao.insertProduct(product)
    suspend fun updateProduct(product: ProductEntity) = dao.updateProduct(product)
    suspend fun deleteProduct(product: ProductEntity) = dao.deleteProduct(product)

    suspend fun createOrder(
        order: OrderEntity,
        items: List<OrderItemEntity>,
        downloads: List<DownloadEntity>
    ) {
        dao.insertOrder(order)
        dao.insertOrderItems(items)
        for (download in downloads) {
            dao.insertDownload(download)
        }
        for (item in items) {
            val prod = dao.getProductById(item.productId)
            if (prod != null) {
                dao.updateProduct(prod.copy(salesCount = prod.salesCount + 1))
            }
        }
    }

    suspend fun updateOrderStatus(orderId: String, status: String) = dao.updateOrderStatus(orderId, status)

    suspend fun insertDownload(download: DownloadEntity) = dao.insertDownload(download)

    suspend fun insertReview(review: ReviewEntity) {
        dao.insertReview(review)
        val prod = dao.getProductById(review.productId)
        if (prod != null) {
            val newCount = prod.reviewCount + 1
            val newRating = ((prod.rating * prod.reviewCount) + review.rating) / newCount
            dao.updateProduct(prod.copy(rating = newRating, reviewCount = newCount))
        }
    }

    suspend fun updateReviewApproval(reviewId: String, isApproved: Boolean) = dao.updateReviewApproval(reviewId, isApproved)
    suspend fun deleteReview(reviewId: String) = dao.deleteReview(reviewId)

    suspend fun insertCoupon(coupon: CouponEntity) = dao.insertCoupon(coupon)
    suspend fun deleteCoupon(id: String) = dao.deleteCoupon(id)

    suspend fun addToWishlist(userId: String, productId: String) {
        dao.addToWishlist(WishlistEntity(userId = userId, productId = productId))
    }

    suspend fun removeFromWishlist(userId: String, productId: String) {
        dao.removeFromWishlist(userId, productId)
    }

    suspend fun seedInitialDataIfEmpty() {
        val existingCustomer = dao.getUserByEmail("customer@assetmarket.com")
        if (existingCustomer == null) {
            val defaultCustomer = UserEntity(
                id = "user_customer",
                email = "customer@assetmarket.com",
                name = "Alex Mercer",
                role = "CUSTOMER",
                password = "customer123",
                avatarUrl = ""
            )
            val defaultAdmin = UserEntity(
                id = "user_admin",
                email = "admin@assetmarket.com",
                name = "Sarah Connor (Admin)",
                role = "ADMIN",
                password = "admin123",
                avatarUrl = ""
            )
            val ownerAdmin = UserEntity(
                id = "user_owner_admin",
                email = "adityaishk1232@gmail.com",
                name = "Aditya (Store Owner)",
                role = "ADMIN",
                password = "admin123",
                avatarUrl = ""
            )
            dao.insertUser(defaultCustomer)
            dao.insertUser(defaultAdmin)
            dao.insertUser(ownerAdmin)

            val categories = listOf(
                CategoryEntity("cat_ui", "UI & Design Kits", "brush", "Figma, Sketch, and Adobe XD app UI design systems"),
                CategoryEntity("cat_3d", "3D Models & Icons", "view_in_ar", "Glossy 3D icons, characters, and isometric scenes"),
                CategoryEntity("cat_code", "Source Code & Apps", "code", "Kotlin Compose starters, Retrofit APIs, and full stack scripts"),
                CategoryEntity("cat_audio", "Audio & Music", "graphic_eq", "Royalty-free synth loops, sound effects, and stems"),
                CategoryEntity("cat_ebook", "E-Books & Guides", "book", "Digital product strategy, marketing, and design guides")
            )
            dao.insertCategories(categories)

            val products = listOf(
                ProductEntity(
                    id = "prod_ui_kit",
                    title = "Neon Mobile UI Design Kit",
                    description = "Over 150+ vibrant mobile app screen templates built with vector auto-layout components. Includes Dark & Light modes, multi-platform design tokens, and customizable micro-interactions.",
                    categoryId = "cat_ui",
                    price = 49.00,
                    discountPrice = 39.00,
                    version = "v2.4",
                    fileSize = "128 MB",
                    fileType = "Figma / ZIP",
                    fileDownloadUrl = "https://assetmarket.com/files/neon_ui_kit_v2.4.zip",
                    imageResName = "img_product_ui_kit",
                    tags = "Figma, Mobile, iOS, Android, Design System",
                    rating = 4.9f,
                    reviewCount = 28,
                    salesCount = 142
                ),
                ProductEntity(
                    id = "prod_3d_pack",
                    title = "Glossy 3D Isometric Icon Pack",
                    description = "A collection of 80 high-resolution glossy 3D rendered icons for web and mobile dashboards. Rendered at 4K resolution in transparent PNG, OBJ, and Blender source files.",
                    categoryId = "cat_3d",
                    price = 29.00,
                    discountPrice = null,
                    version = "v1.8",
                    fileSize = "340 MB",
                    fileType = "PNG / OBJ / ZIP",
                    fileDownloadUrl = "https://assetmarket.com/files/glossy_3d_icons_v1.8.zip",
                    imageResName = "img_product_3d_pack",
                    tags = "3D, Blender, Isometric, Glossy, Dashboard",
                    rating = 4.8f,
                    reviewCount = 19,
                    salesCount = 89
                ),
                ProductEntity(
                    id = "prod_audio_bundle",
                    title = "SynthWave Sound Producer Bundle",
                    description = "Premium royalty-free synthwave music tracks, retro drum kits, vocal chops, and serum synth presets for background scoring and video projects.",
                    categoryId = "cat_audio",
                    price = 35.00,
                    discountPrice = 25.00,
                    version = "v3.0",
                    fileSize = "850 MB",
                    fileType = "WAV / MP3 / ZIP",
                    fileDownloadUrl = "https://assetmarket.com/files/synthwave_audio_v3.0.zip",
                    imageResName = "img_product_audio_tracks",
                    tags = "Audio, WAV, Music, Synthwave, Cyberpunk",
                    rating = 5.0f,
                    reviewCount = 15,
                    salesCount = 95
                ),
                ProductEntity(
                    id = "prod_kotlin_compose",
                    title = "Kotlin Compose E-Commerce Starter Engine",
                    description = "Complete production-ready Android app template with Room local cache, M3 components, stateful ViewModel architecture, and mock payment gateway flows.",
                    categoryId = "cat_code",
                    price = 79.00,
                    discountPrice = null,
                    version = "v1.2",
                    fileSize = "45 MB",
                    fileType = "Kotlin / ZIP",
                    fileDownloadUrl = "https://assetmarket.com/files/compose_starter_v1.2.zip",
                    imageResName = "img_banner_hero",
                    tags = "Kotlin, Android, Compose, Room, MVVM",
                    rating = 4.95f,
                    reviewCount = 32,
                    salesCount = 210
                ),
                ProductEntity(
                    id = "prod_ebook_saas",
                    title = "SaaS Growth & Micro-Copy Master Guide",
                    description = "Comprehensive 120-page guide on writing high-converting CTA copy, onboarding flows, and digital product pricing psychology for maximum conversion.",
                    categoryId = "cat_ebook",
                    price = 19.00,
                    discountPrice = 14.00,
                    version = "v1.0",
                    fileSize = "12 MB",
                    fileType = "PDF / EPUB",
                    fileDownloadUrl = "https://assetmarket.com/files/saas_growth_guide_v1.0.pdf",
                    imageResName = "img_product_ui_kit",
                    tags = "PDF, Marketing, Copywriting, SaaS, Growth",
                    rating = 4.7f,
                    reviewCount = 11,
                    salesCount = 64
                )
            )
            dao.insertProducts(products)

            val coupons = listOf(
                CouponEntity("c_1", "WELCOME20", discountPercent = 20, expiryTimestamp = System.currentTimeMillis() + 30L * 24 * 3600 * 1000, usageLimit = 500, timesUsed = 42),
                CouponEntity("c_2", "SUMMER50", discountFixed = 15.0, minPurchase = 50.0, expiryTimestamp = System.currentTimeMillis() + 15L * 24 * 3600 * 1000, usageLimit = 100, timesUsed = 18),
                CouponEntity("c_3", "ADMINSPECIAL", discountPercent = 50, expiryTimestamp = System.currentTimeMillis() + 60L * 24 * 3600 * 1000, usageLimit = 50, timesUsed = 5)
            )
            for (c in coupons) {
                dao.insertCoupon(c)
            }

            val sampleOrder1 = OrderEntity(
                id = "ORD-98214",
                userId = "user_customer",
                userEmail = "customer@assetmarket.com",
                userName = "Alex Mercer",
                totalAmount = 39.00,
                discountAmount = 10.00,
                couponCode = "WELCOME20",
                paymentGateway = "Stripe",
                status = "COMPLETED",
                createdAt = System.currentTimeMillis() - 2L * 24 * 3600 * 1000
            )
            dao.insertOrder(sampleOrder1)

            val item1 = OrderItemEntity(
                id = "item_1",
                orderId = "ORD-98214",
                productId = "prod_ui_kit",
                productTitle = "Neon Mobile UI Design Kit",
                priceAtPurchase = 39.00,
                versionAtPurchase = "v2.4",
                imageResName = "img_product_ui_kit"
            )
            dao.insertOrderItems(listOf(item1))

            val download1 = DownloadEntity(
                id = "dl_1",
                userId = "user_customer",
                productId = "prod_ui_kit",
                orderId = "ORD-98214",
                versionDownloaded = "v2.4"
            )
            dao.insertDownload(download1)

            val review1 = ReviewEntity(
                id = "rev_1",
                productId = "prod_ui_kit",
                userId = "user_customer",
                userName = "Alex Mercer",
                rating = 5.0f,
                comment = "Incredible vector organization! Saved me 40+ hours of UI design work on my latest mobile app client project."
            )
            dao.insertReview(review1)
        }
    }
}
