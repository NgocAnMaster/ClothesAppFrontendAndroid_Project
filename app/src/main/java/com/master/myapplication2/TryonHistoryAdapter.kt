package com.master.myapplication2

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.master.myapplication2.models.Product
import com.master.myapplication2.utils.UserSessionManager

class TryonHistoryAdapter(
    private var items: List<TryonImageWithProduct>,
    private val onItemClick: (position: Int) -> Unit
) : RecyclerView.Adapter<TryonHistoryAdapter.ViewHolder>() {

    data class TryonImageWithProduct(
        val tryonId: String,
        val imageUrl: String,
        val productName: String,
        val createdAgo: String,
        val productId: Product? // ✅ now matches TryonImage.productId type
    )

    val currentItems: List<TryonImageWithProduct>
        get() = items

    fun updateList(newItems: List<TryonImageWithProduct>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageTryon)
        val textTitle: TextView = itemView.findViewById(R.id.textProductName)
        val textDate: TextView = itemView.findViewById(R.id.textCreatedAgo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tryon_image, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val tryonIds = ArrayList(items.map { it.tryonId })

        // ✅ Build full image URL with /api/images prefix
        val fullImageUrl = when {
            item.imageUrl.isNullOrBlank() -> null
            item.imageUrl.startsWith("http") -> item.imageUrl
            item.imageUrl.startsWith("/api/") ->
                BuildConfig.BASE_URL.trimEnd('/') + item.imageUrl
            else ->
                BuildConfig.BASE_URL.trimEnd('/') + "/api" + item.imageUrl
        }

        Log.d("TryonHistoryAdapter", "Loading image: $fullImageUrl")

        Glide.with(holder.itemView.context)
            .load(fullImageUrl ?: R.drawable.placeholder_image)
            .placeholder(R.drawable.placeholder_image)
            .error(R.drawable.placeholder_image)
            .centerCrop()
            .into(holder.imageView)

        holder.textTitle.text = item.productName
        holder.textDate.text = item.createdAgo

        // ✅ Handle click → open TryonImageViewerActivity
        holder.itemView.setOnClickListener {
            onItemClick(position)

            val context = holder.itemView.context

            val imageUrls = ArrayList(
                items.map {
                    when {
                        it.imageUrl.startsWith("http") -> it.imageUrl
                        it.imageUrl.startsWith("/api/") ->
                            BuildConfig.BASE_URL.trimEnd('/') + it.imageUrl
                        else ->
                            BuildConfig.BASE_URL.trimEnd('/') + "/api" + it.imageUrl
                    }
                }
            )

            // Optionally, create product links if available (dummy placeholders for now)
            val productLinks = items.map {
                it.productId?._id?.let { id ->
                    BuildConfig.BASE_URL.trimEnd('/') + "/api/products/$id"
                } ?: ""
            }

            val productIds = ArrayList(items.map { it.productId?._id ?: "" })
            val tryonIds = ArrayList(items.map { it.tryonId })

            val userId = UserSessionManager.getUserId(context)
            Log.d("TryonHistoryAdapter", "Passing userId=$userId")

            val intent = Intent(context, TryonImageViewerActivity::class.java)
            intent.putStringArrayListExtra("images", ArrayList(imageUrls))
            intent.putStringArrayListExtra("productIds", ArrayList(productIds))
            intent.putStringArrayListExtra("productLinks", ArrayList(productLinks))
            intent.putStringArrayListExtra("tryonIds", tryonIds)
            intent.putExtra("userId", userId)
            intent.putExtra("position", position)
//            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = items.size

//    fun updateList(newItems: List<TryonImageWithProduct>) {
//        items = newItems
//        notifyDataSetChanged()
//    }
}
