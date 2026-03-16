package com.master.myapplication2

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.master.myapplication2.models.Product
import com.master.myapplication2.network.ApiClient
import com.master.myapplication2.network.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.app.AlertDialog
import android.content.Intent
import android.provider.MediaStore

class ProductDetailActivity : AppCompatActivity() {

    private lateinit var imageCover: ImageView
    private lateinit var textTitle: TextView
    private lateinit var textCategory: TextView
    private lateinit var textPrice: TextView
    private lateinit var textDescription: TextView
    private lateinit var recyclerImages: RecyclerView
    private lateinit var btnTryIt: Button
    private lateinit var btnBack: ImageView
    private lateinit var adapter: ImageAdapter
    private var existingImageUri: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        // Initialize views
        imageCover = findViewById(R.id.imageCover)
        textTitle = findViewById(R.id.textTitle)
        textCategory = findViewById(R.id.textCategory)
        textPrice = findViewById(R.id.textPrice)
        textDescription = findViewById(R.id.textDescription)
        recyclerImages = findViewById(R.id.recyclerImages)
        btnTryIt = findViewById(R.id.btnTryIt)
        btnBack = findViewById(R.id.btnBack)

        val productId = intent.getStringExtra("productId")
        if (productId == null) {
            finish()
            return
        }

        existingImageUri = intent.getStringExtra("existingImageUri")

        recyclerImages.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        adapter = ImageAdapter(emptyList())
        recyclerImages.adapter = adapter

        fetchProductDetail(productId)

        btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        btnTryIt.setOnClickListener {

            // If user ALREADY selected image → skip dialog
            existingImageUri?.let {
                val processImageIntent = Intent(this, ProcessingTryonImageActivity::class.java)
                processImageIntent.putExtra("imageUri", it)
                processImageIntent.putExtra("productId", productId)
                startActivity(processImageIntent)
                return@setOnClickListener
            }

            // Otherwise show the dialog
            val options = arrayOf("Take a photo", "Choose from gallery")
            AlertDialog.Builder(this)
                .setTitle("Select Image")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> openCamera()
                        1 -> openGallery()
                    }
                }
                .show()
        }
    }

    private val REQUEST_IMAGE_CAPTURE = 100
    private val REQUEST_IMAGE_PICK = 101

    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }

//    private fun openGallery() {
//        val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//        startActivityForResult(pickIntent, REQUEST_IMAGE_PICK)
//    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }

        startActivityForResult(
            Intent.createChooser(intent, "Select image"),
            REQUEST_IMAGE_PICK
        )
    }

    private fun openExistingImage() {
        // This path assumes you came from CreateFragment → SelectProductActivity
        val intent = Intent(this, ProcessingTryonImageActivity::class.java)
        val productId = intent.getStringExtra("productId") // keep passing product
        intent.putExtra("productId", productId)
        startActivity(intent)
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    val photo = data?.extras?.get("data")
                    val uri = com.master.myapplication2.utils.ImageUtils.saveBitmapToCache(this, photo)
                    val editIntent = Intent(this, EditImageActivity::class.java)
                    editIntent.putExtra("imageUri", uri.toString())
                    editIntent.putExtra("productId", intent.getStringExtra("productId"))
                    startActivity(editIntent)
                }
                REQUEST_IMAGE_PICK -> {
                    val imageUri = data?.data
                    if (imageUri != null) {
                        val editIntent = Intent(this, EditImageActivity::class.java)
                        editIntent.putExtra("imageUri", imageUri.toString())
                        editIntent.putExtra("productId", intent.getStringExtra("productId"))
                        startActivity(editIntent)
                    }
                }
            }
        }
    }

    private fun fetchProductDetail(id: String) {
        val api = ApiClient.createService(ApiService::class.java)
        val call = api.getProductById(id)

        call.enqueue(object : Callback<Product> {
            override fun onResponse(call: Call<Product>, response: Response<Product>) {
                if (response.isSuccessful && response.body() != null) {
                    val product = response.body()!!
                    bindProductData(product)
                } else {
                    Toast.makeText(this@ProductDetailActivity, "Product not found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Product>, t: Throwable) {
                Toast.makeText(this@ProductDetailActivity, "Failed to load product", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun bindProductData(product: Product) {
        textTitle.text = product.name
        textCategory.text = product.category
        textPrice.text = String.format("$%.2f", product.price)
        textDescription.text = product.description

        if (!product.images.isNullOrEmpty()) {
            // ✅ Use full URL for Glide
            val coverUrl = BuildConfig.BASE_URL + "api" + product.images[0].url
            Glide.with(this)
                .load(coverUrl)
                .centerCrop()
                .into(imageCover)

            // ✅ Pass full URLs to adapter
            val imageUrls = product.images.map { BuildConfig.BASE_URL + "api" + it.url }
            adapter.updateList(imageUrls)
        }
    }
}
