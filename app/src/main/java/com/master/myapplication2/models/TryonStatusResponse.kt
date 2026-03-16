package com.master.myapplication2.models

data class TryonStatusResponse(
    val success: Boolean,
    val status: String,
    val outputPath: String?,
    val error: String?
)
