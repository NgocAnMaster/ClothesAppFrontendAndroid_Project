package com.master.myapplication2

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView

class ProductImageViewerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_image_viewer)

        val imageUrls = intent.getStringArrayListExtra("images") ?: arrayListOf()
        val startPosition = intent.getIntExtra("position", 0)

        val viewPager = findViewById<ViewPager2>(R.id.viewPagerProductImages)
        val backBtn = findViewById<View>(R.id.btnBack)

        viewPager.adapter = object : RecyclerView.Adapter<ImageViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
                val photoView = PhotoView(parent.context)
                photoView.layoutParams =
                    ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                return ImageViewHolder(photoView)
            }

            override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
                val url = imageUrls[position]
                Glide.with(holder.itemView.context)
                    .load(url)
                    .into(holder.photoView)
            }

            override fun getItemCount() = imageUrls.size
        }

        viewPager.setCurrentItem(startPosition, false)

        backBtn.setOnClickListener { finish() }
    }

    private class ImageViewHolder(val photoView: PhotoView) : RecyclerView.ViewHolder(photoView)
}
