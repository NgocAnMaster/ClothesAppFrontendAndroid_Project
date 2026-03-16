package com.master.myapplication2

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar

class EditImageActivity : AppCompatActivity() {

    private lateinit var editImageView: ImageView
    private lateinit var topAppBar: MaterialToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_edit_image) // reuse your layout

        // Find views
        editImageView = findViewById(R.id.editImageView)
        topAppBar = findViewById(R.id.topAppBar)

        // Get the image URI passed from CreateFragment (or wherever you launched this activity)
        val imageUriString = intent.getStringExtra("imageUri")
        val imageUri = imageUriString?.let { Uri.parse(it) }

//        Toast.makeText(this, "Image URI: $imageUri", Toast.LENGTH_SHORT).show()

        // Load the image into the ImageView
        imageUri?.let {
            editImageView.setImageURI(it)
        }

        // Toolbar navigation (back arrow)
        topAppBar.setNavigationOnClickListener {
            finish()
        }

        // Toolbar actions (camera + checkmark)
        topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_camera -> {
                    // TODO: implement "Retake photo" (open camera intent again)
                    val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivity(intent)
                    true
                }
                R.id.action_done -> {
                    // TODO: implement saving/exporting edited image
                    // For now, just finish and go back
                    val productId = intent.getStringExtra("productId")

                    val nextIntent = if (productId != null) {
                        // User came from ProductDetailActivity
                        Intent(this@EditImageActivity, ProcessingTryonImageActivity::class.java).apply {
                            putExtra("imageUri", imageUriString) // NOT editedImageUri
                            putExtra("productId", productId)
                        }
                    } else {
                        // User came from CreateFragment
                        Intent(this@EditImageActivity, SelectProductActivity::class.java).apply {
                            putExtra("imageUri", imageUriString)
                        }
                    }

                    startActivity(nextIntent)
                    true
                }
                else -> false
            }
        }
    }
}