package com.master.myapplication2.models

import com.google.gson.annotations.SerializedName

data class TryonImage(
    val _id: String,
    val userId: String,
    val productId: Product?, // ✅ correct type
    @SerializedName("resultImageUrl") val imageUrl: String?, // ✅ mapped field
    val createdAt: String
)
