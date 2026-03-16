package com.master.myapplication2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.master.myapplication2.models.Product
import com.master.myapplication2.network.ApiClient
import com.master.myapplication2.network.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchProductActivity : AppCompatActivity() {

    private lateinit var adapter: ProductAdapter
    private var allProducts: List<Product> = emptyList()
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_product)

        // --- Views ---
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        val searchInput: EditText = findViewById(R.id.searchInput)
        val btnBack: ImageView = findViewById(R.id.btnBack)
        recyclerView = findViewById(R.id.recyclerProducts)

        // --- Toolbar setup ---
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // --- RecyclerView setup ---
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        adapter = ProductAdapter(emptyList()) { product ->
            val intent = Intent(this, ProductDetailActivity::class.java)
            intent.putExtra("productId", product._id)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        // --- Focus search input automatically ---
        searchInput.requestFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(searchInput, InputMethodManager.SHOW_IMPLICIT)

        // --- Back button behavior ---
        btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // --- Fetch all products from backend ---
        fetchAllProducts()

        // --- Search logic ---
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim().lowercase()
                if (query.isEmpty()) {
                    adapter.updateList(emptyList()) // show nothing when query empty
                } else {
                    val filtered = allProducts.filter {
                        it.name.lowercase().contains(query) ||
                                (it.description?.lowercase()?.contains(query) ?: false) ||
                                (it.category?.lowercase()?.contains(query) ?: false)
                    }
                    adapter.updateList(filtered)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun fetchAllProducts() {
        val api = ApiClient.createService(ApiService::class.java)
        val call = api.getProducts()

        call.enqueue(object : Callback<List<Product>> {
            override fun onResponse(
                call: Call<List<Product>>,
                response: Response<List<Product>>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    allProducts = response.body()!!
                } else {
                    allProducts = emptyList()
                }
            }

            override fun onFailure(call: Call<List<Product>>, t: Throwable) {
                allProducts = emptyList()
            }
        })
    }
}
