package com.master.myapplication2

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.master.myapplication2.models.TryonRunResponse
import com.master.myapplication2.models.TryonStatusResponse
import com.master.myapplication2.network.ApiClient
import com.master.myapplication2.network.ApiService
import com.master.myapplication2.utils.ImageUtils
import com.master.myapplication2.utils.UserSessionManager
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class ProcessingTryonImageActivity : AppCompatActivity() {

    private lateinit var api: ApiService
    private var runCall: Call<TryonRunResponse>? = null
    private var isCancelled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_processing_tryon_image)

        api = ApiClient.createService(ApiService::class.java)

        val btnBack = findViewById<View>(R.id.btnBack)
        val btnCancel = findViewById<Button>(R.id.btnCancel)
        val btnRetry = findViewById<Button>(R.id.btnRetry)

        btnBack.setOnClickListener { confirmCancel() }
        btnCancel.setOnClickListener { confirmCancel() }
        btnRetry.setOnClickListener { startTryon() }

        // ✅ Proper back handling (gesture + button)
        onBackPressedDispatcher.addCallback(this) {
            confirmCancel()
        }

        Log.d("Tryon", "imageUri = ${intent.getStringExtra("imageUri")}")
        Toast.makeText(this, "imageUri = ${intent.getStringExtra("imageUri")}", Toast.LENGTH_SHORT).show()
        startTryon()
    }

    private fun confirmCancel() {
        AlertDialog.Builder(this)
            .setTitle("Cancel try-on?")
            .setMessage("Processing will be stopped. Are you sure?")
            .setPositiveButton("Yes") { _, _ ->
                isCancelled = true
                runCall?.cancel()
                finish()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun startTryon() {
        showProcessing()
        runCall?.cancel()

        val imageUriString = intent.getStringExtra("imageUri")
        if (imageUriString.isNullOrBlank()) {
            showError("Image not found. Please try again.")
            return
        }

        val imagePath = intent.getStringExtra("imageUri") ?: return
        // OR val imageUriString = intent.getStringExtra("imageUri") ?: return
        val productId = intent.getStringExtra("productId") ?: return
        val userId = UserSessionManager.getUserId(this)

//        if (!imagePath.startsWith("file://")) {
//            showError("Invalid image source")
//            return
//        }
//
//        val file = File(imagePath.removePrefix("file://"))

        val imageUri = Uri.parse(imagePath)
        val file = ImageUtils.copyUriToCache(this, imageUri)

        if (!file.exists()) {
            showError("Image file not found")
            return
        }

        val requestFile = file.asRequestBody("image/*".toMediaType())
        val imagePart =
            MultipartBody.Part.createFormData("userImage", file.name, requestFile)

        val productBody =
            productId.toRequestBody("text/plain".toMediaType())

        val userBody =
            userId?.toRequestBody("text/plain".toMediaType())

        runCall = api.runTryon(imagePart, productBody, userBody)
        runCall!!.enqueue(object : Callback<TryonRunResponse> {

            override fun onResponse(
                call: Call<TryonRunResponse>,
                response: Response<TryonRunResponse>
            ) {
                if (!response.isSuccessful || response.body()?.success != true) {
                    showError("Try-on failed")
                    return
                }

//                val imageUrl = response.body()?.outputPath ?: return
//                val tryonId = response.body()?.tryon?._id
                val tryonId = response.body()?.tryonId

                if (tryonId.isNullOrBlank()) {
                    showError("Invalid try-on response")
                    return
                }

                startPollingTryonStatus(tryonId, productId)

//                val fullUrl =
//                    if (imageUrl.startsWith("http"))
//                        imageUrl
//                    else
//                        BuildConfig.BASE_URL.trimEnd('/') + imageUrl
//
//                openViewer(fullUrl, tryonId)
            }

            override fun onFailure(call: Call<TryonRunResponse>, t: Throwable) {
                if (call.isCanceled) return
                showError(t.message ?: "Network error")
            }
        })
    }

    private fun startPollingTryonStatus(tryonIdParam: String, productId: String?) {
        val handler = android.os.Handler(mainLooper)

        lateinit var pollRunnable: Runnable

        pollRunnable = object : Runnable {
            override fun run() {
                if (isCancelled) return

                api.getTryonStatus(tryonIdParam)
                    .enqueue(object : Callback<TryonStatusResponse> {

                        override fun onResponse(
                            call: Call<TryonStatusResponse>,
                            response: Response<TryonStatusResponse>
                        ) {
                            val body = response.body() ?: return
                            if (isCancelled) return

                            when (body.status) {
                                "processing" -> {
                                    handler.postDelayed(pollRunnable, 5000)
                                }
                                "done" -> {
                                    val imageUrl = body.outputPath ?: run {
                                        showError("Result missing")
                                        return
                                    }

                                    val fullUrl =
                                        if (imageUrl.startsWith("http"))
                                            imageUrl
                                        else
                                            BuildConfig.BASE_URL.trimEnd('/') + imageUrl

                                    openViewer(fullUrl, tryonIdParam, productId)
                                }
                                "failed" -> {
                                    showError(body.error ?: "Try-on failed")
                                }
                            }
                        }

                        override fun onFailure(call: Call<TryonStatusResponse>, t: Throwable) {
                            if (!isCancelled) {
                                handler.postDelayed(pollRunnable, 5000)
                            }
                        }
                    })
            }
        }

        handler.post(pollRunnable)
    }

    private fun openViewer(imageUrl: String, tryonId: String?, productId: String?) {
        val intent = Intent(this, TryonImageViewerActivity::class.java)
        intent.putStringArrayListExtra("images", arrayListOf(imageUrl))
        intent.putStringArrayListExtra("productIds", arrayListOf(productId ?: ""))
        intent.putStringArrayListExtra("tryonIds", arrayListOf(tryonId ?: ""))
        intent.putExtra("userId", UserSessionManager.getUserId(this))
        intent.putExtra("position", 0)
        startActivity(intent)
        finish()
    }

    private fun showProcessing() {
        findViewById<View>(R.id.layoutProcessing).visibility = View.VISIBLE
        findViewById<View>(R.id.layoutError).visibility = View.GONE
    }

    private fun showError(msg: String) {
        findViewById<View>(R.id.layoutProcessing).visibility = View.GONE
        findViewById<View>(R.id.layoutError).visibility = View.VISIBLE
        findViewById<TextView>(R.id.textError).text = msg
    }
}

