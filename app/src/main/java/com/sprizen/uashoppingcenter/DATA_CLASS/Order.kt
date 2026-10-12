package com.sprizen.uashoppingcenter.DATA_CLASS

import com.google.firebase.database.IgnoreExtraProperties
import java.io.Serializable

@IgnoreExtraProperties
data class OrderItem(
    var productId: String = "",
    var productName: String = "",
    var price: Double = 0.0,
    var quantity: Int = 1,
    var imageUrl: String = ""
) : Serializable

@IgnoreExtraProperties
data class Order(
    var orderId: String = "",
    var userId: String = "",
    var userEmail: String = "",
    var customerName: String = "",
    var customerPhone: String = "",
    var shippingAddress: String = "",
    var city: String = "",
    var postalCode: String = "",
    var items: List<OrderItem> = emptyList(),
    var totalAmount: Double = 0.0,
    var paymentMethod: String = "", // "Card", "Wallet", "COD"
    var paymentStatus: String = "PAID", // "PAID", "PENDING_COD"
    var orderStatus: String = "Processing", // "Pending", "Processing", "Shipped", "Delivered", "Cancelled"
    var timestamp: Long = System.currentTimeMillis()
) : Serializable
