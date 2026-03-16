package com.master.myapplication2

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ImageAdapter(
    private var imageUrls: List<String>
) : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product_image, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = imageUrls.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val context = holder.itemView.context
        val url = imageUrls[position]
        Glide.with(context)
            .load(url)
            .centerCrop()
            .into(holder.imageView)

        // 👇 Launch ProductImageViewerActivity on click
        holder.itemView.setOnClickListener {
            val intent = Intent(context, ProductImageViewerActivity::class.java)
            intent.putStringArrayListExtra("images", ArrayList(imageUrls))
            intent.putExtra("position", position)
            context.startActivity(intent)
        }
    }

    fun updateList(newUrls: List<String>) {
        imageUrls = newUrls
        notifyDataSetChanged()
    }
}
