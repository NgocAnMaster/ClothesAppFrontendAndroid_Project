package com.master.myapplication2

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // --- Search bar and icon ---
        val searchInput: EditText = findViewById(R.id.search_input)
        val searchIcon: ImageView = findViewById(R.id.search_icon)

        // Click anywhere on the search input or search icon -> open SearchProductActivity
        val openSearch = {
            val intent = Intent(this, SearchProductActivity::class.java)
            startActivity(intent)
        }

        searchInput.setOnClickListener { openSearch() }
        searchIcon.setOnClickListener { openSearch() }

        // --- Navigation setup ---
        val navController = findNavController(R.id.nav_host_fragment)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav.setupWithNavController(navController)
    }
}
