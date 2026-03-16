package com.master.myapplication2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.master.myapplication2.models.Product
import com.master.myapplication2.BuildConfig

class ProductAdapter(
    private var products: List<Product>,
    private val onClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleView: TextView = itemView.findViewById(R.id.productTitle)
        private val updatedView: TextView = itemView.findViewById(R.id.productUpdated)
        private val imageView: ImageView = itemView.findViewById(R.id.productImage)

        fun bind(product: Product) {
            titleView.text = product.name
            updatedView.text = "$${String.format("%.2f", product.price)}"

            val coverImage = product.images.firstOrNull()?.url
            if (coverImage != null) {
                val imageUrl =
                    "${BuildConfig.BASE_URL}api$coverImage"

                Glide.with(itemView.context)
                    .load(imageUrl)
                    .centerCrop()
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(imageView)
            } else {
                imageView.setImageResource(R.drawable.ic_launcher_background)
            }

            itemView.setOnClickListener { onClick(product) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount(): Int = products.size

    fun updateList(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged()
    }
}
