package com.sprizen.uashoppingcenter.DATA_CLASS

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class PRODUCT(

    var productId: String = "",

    val imagesUrls: List<String> = emptyList(),

    val productName: String = "",

    val productPriceSelling: Double = 0.0,

    val productDescription: String = "",

    val productPriceActual: Double = 0.0,

    val discountOfProduct: Double = 0.0,

    val stockAvailable: Int = 0,

    val lowStockAlert: Int = 0,

    val colorsAvailable: List<String> = emptyList(),

    val productWeight: Double = 0.0,

    val productVolume: Double = 0.0,

    val shippingCharges: Double = 0.0,

    val category: String = "",

    val subCategory: String = "",

    val location: String = "",

    val rating: Double = 0.0
)