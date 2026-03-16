package com.master.myapplication2

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView
import android.app.Activity
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.master.myapplication2.network.ApiClient
import com.master.myapplication2.network.ApiService
import com.master.myapplication2.utils.UserSessionManager
import kotlinx.coroutines.launch

class TryonImageViewerActivity : AppCompatActivity() {

    private lateinit var imageUrls: ArrayList<String>
    private lateinit var productIds: ArrayList<String>
    private lateinit var viewPager: ViewPager2

    private lateinit var btnDeleteTryon: Button
    private lateinit var tryonIds: ArrayList<String>
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tryon_image_viewer)

        imageUrls = intent.getStringArrayListExtra("images") ?: arrayListOf()
        productIds = intent.getStringArrayListExtra("productIds") ?: arrayListOf()
        tryonIds = intent.getStringArrayListExtra("tryonIds") ?: arrayListOf()
        userId = intent.getStringExtra("userId") ?: ""
        val startPosition = intent.getIntExtra("position", 0)

        viewPager = findViewById(R.id.viewPagerTryonImages)
        val backBtn = findViewById<View>(R.id.btnBack)
        val btnViewProduct = findViewById<View>(R.id.btnViewProduct)
        val btnDownload = findViewById<View>(R.id.btnDownload)

        btnDeleteTryon = findViewById(R.id.btnDeleteTryon)

        // --- ViewPager setup ---
        viewPager.adapter = object : RecyclerView.Adapter<ImageViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
                val photoView = PhotoView(parent.context)
                photoView.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                return ImageViewHolder(photoView)
            }

            override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
                Glide.with(holder.itemView.context)
                    .load(imageUrls[position])
                    .into(holder.photoView)
            }

            override fun getItemCount() = imageUrls.size
        }

        viewPager.setCurrentItem(startPosition, false)

        // --- Button actions ---
        backBtn.setOnClickListener { finish() }

        btnViewProduct.setOnClickListener {
            val currentIndex = viewPager.currentItem
            val productId = intent.getStringArrayListExtra("productIds")?.getOrNull(currentIndex)
            if (!productId.isNullOrEmpty()) {
                val intent = Intent(this, ProductDetailActivity::class.java)
                intent.putExtra("productId", productId)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Product info unavailable", Toast.LENGTH_SHORT).show()
            }
        }

        btnDownload.setOnClickListener {
            val currentIndex = viewPager.currentItem
            val imageUrl = imageUrls.getOrNull(currentIndex)
            if (imageUrl != null) {
                downloadImage(imageUrl)
            } else {
                Toast.makeText(this, "Image not found", Toast.LENGTH_SHORT).show()
            }
        }

        btnDeleteTryon.setOnClickListener {
            showDeleteConfirmation()
        }
    }

    private fun downloadImage(url: String) {
        try {
            val request = DownloadManager.Request(Uri.parse(url))
            request.setAllowedNetworkTypes(
                DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE
            )
            request.setTitle("Downloading Try-on Image")
            request.setDescription("Saving image to Downloads folder")
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                Uri.parse(url).lastPathSegment ?: "tryon_image.jpg"
            )

            val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            dm.enqueue(request)

            Toast.makeText(this, "Downloading image...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Download failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private class ImageViewHolder(val photoView: PhotoView) :
        RecyclerView.ViewHolder(photoView)

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Delete try-on image")
            .setMessage("Are you sure you want to delete this try-on image?")
            .setPositiveButton("Yes") { _, _ ->
                deleteTryon()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun deleteTryon() {
        val currentIndex = viewPager.currentItem
        val tryonId = tryonIds.getOrNull(currentIndex)

        if (userId.isEmpty()) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_LONG).show()
            return
        }

        if (tryonId.isNullOrEmpty()) {
            showDeleteFailed("Try-on ID not found")
            return
        }

        btnDeleteTryon.isEnabled = false
        btnDeleteTryon.text = "Deleting..."

        lifecycleScope.launch {
            try {
                val api = ApiClient.createService(ApiService::class.java)
                val response = api.deleteTryon(tryonId, userId)

                if (response.isSuccessful) {
                    Toast.makeText(
                        this@TryonImageViewerActivity,
                        "Delete success",
                        Toast.LENGTH_SHORT
                    ).show()

                    setResult(Activity.RESULT_OK)
                    finish()
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                    showDeleteFailed(errorMsg)
                }
            } catch (e: Exception) {
                showDeleteFailed(e.message ?: "Network error")
            }
        }
    }

    private fun showDeleteFailed(reason: String) {
        btnDeleteTryon.isEnabled = true
        btnDeleteTryon.text = "Delete"

        Toast.makeText(
            this,
            "Delete failed: $reason",
            Toast.LENGTH_LONG
        ).show()
    }

}
