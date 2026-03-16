package com.master.myapplication2.network

import com.master.myapplication2.models.DeleteResponse
import com.master.myapplication2.models.LoginResponse
import com.master.myapplication2.models.Product
import com.master.myapplication2.models.TryonImage
import com.master.myapplication2.models.TryonRunResponse
import com.master.myapplication2.models.TryonStatusResponse
import com.master.myapplication2.models.User
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ===== Authentication =====
    @POST("api/auth/signup")
    fun signUp(@Body user: User): Call<User>

    @POST("api/auth/login")
    fun login(@Body credentials: Map<String, String>): Call<LoginResponse>

    // ===== Products =====
    @GET("api/products")
    fun getProducts(): Call<List<Product>>

    // ===== Try-on Images =====
    @GET("api/tryons/{userId}")
    fun getUserTryons(@Path("userId") userId: String): Call<List<Map<String, Any>>>

    @GET("api/products/{id}")
    fun getProductById(@Path("id") id: String): Call<Product>

    // ===== Try-on Images =====
    @GET("api/tryons/{userId}")
    fun getTryonImagesByUser(@Path("userId") userId: String): Call<List<TryonImage>>

    @DELETE("api/tryons/{tryonId}/{userId}")
    suspend fun deleteTryon(
        @Path("tryonId") tryonId: String,
        @Path("userId") userId: String
    ): Response<DeleteResponse>

    @Multipart
    @POST("api/tryon/run")
    fun runTryon(
        @Part userImage: MultipartBody.Part,
        @Part("productId") productId: RequestBody,
        @Part("userId") userId: RequestBody?
    ): Call<TryonRunResponse>

    @GET("/api/tryon/{id}/status")
    fun getTryonStatus(
        @Path("id") tryonId: String
    ): Call<TryonStatusResponse>

}
