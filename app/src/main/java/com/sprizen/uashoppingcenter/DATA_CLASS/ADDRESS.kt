package com.sprizen.uashoppingcenter.DATA_CLASS

data class ADDRESS(
    var id: String = "",
    var title: String = "Home",
    var province: String = "",
    var city: String = "",
    var addressLine1: String = "",
    var addressLine2: String = "",
    var phone: String = "",
    var isSelected: Boolean = false
)
