package com.master.myapplication2

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.master.myapplication2.network.ApiClient
import com.master.myapplication2.models.Product
import com.master.myapplication2.network.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SelectProductActivity : AppCompatActivity() {

    private lateinit var adapter: ProductAdapter
    private var allProducts: List<Product> = emptyList()
    private val apiService = ApiClient.createService(ApiService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_product)

        // Setup toolbar with back button
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Setup RecyclerView
        val recyclerView: RecyclerView = findViewById(R.id.recyclerProducts)
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        adapter = ProductAdapter(allProducts) { product ->
            val intent = Intent(this, ProductDetailActivity::class.java)
            intent.putExtra("productId", product._id)

            // forward the user's original image
            val imageUri = this.intent.getStringExtra("imageUri")

            Toast.makeText(this, "Image: $imageUri", Toast.LENGTH_SHORT).show()

            if (imageUri != null) {
                intent.putExtra("existingImageUri", imageUri)
            }
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        // Load data from backend
        fetchProducts()
    }

    private fun fetchProducts() {
        apiService.getProducts().enqueue(object : Callback<List<Product>> {
            override fun onResponse(
                call: Call<List<Product>>,
                response: Response<List<Product>>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        allProducts = it
                        adapter.updateList(allProducts)
                    }
                }
            }

            override fun onFailure(call: Call<List<Product>>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_select_product, menu)

        val searchItem = menu?.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as? SearchView

        searchView?.queryHint = "Search products"
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                val filtered = allProducts.filter {
                    it.name.contains(newText ?: "", ignoreCase = true)
                }
                adapter.updateList(filtered)
                return true
            }
        })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> { // back button
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
