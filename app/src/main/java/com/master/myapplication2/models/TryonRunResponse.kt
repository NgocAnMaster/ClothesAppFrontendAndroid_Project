package com.master.myapplication2.models

data class TryonRunResponse(
    val success: Boolean,
//    val tryon: TryonImage?,
//    val outputPath: String?
    val tryonId: String,
    val status: String
)