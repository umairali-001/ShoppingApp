package com.sprizen.uashoppingcenter.Model

data class SellerProduct(
    val productId: String = "",
    val userId: String = "",
    val productName: String = "",
    val sellingPrice: Double = 0.0,
    val actualPrice: Double = 0.0,
    val stock: Int = 0,
    val category: String = "",
    val subCategory: String = "",
    val imageUrls: List<String> = emptyList()
)