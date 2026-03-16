package com.master.myapplication2.models

data class LoginResponse(
    val message: String,
    val userId: String,
    val name: String,
    val username: String,
    val email: String
)