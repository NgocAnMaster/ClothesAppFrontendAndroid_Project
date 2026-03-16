package com.master.myapplication2.models

data class ProductImage(
    val url: String,
    val view: String
)

data class Product(
    val _id: String? = null,
    val name: String,
    val category: String,
    val price: Double,
    val description: String? = null,
    val images: List<ProductImage>
)
