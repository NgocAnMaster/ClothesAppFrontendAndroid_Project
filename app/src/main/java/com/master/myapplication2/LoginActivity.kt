package com.master.myapplication2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.master.myapplication2.databinding.ActivityLoginBinding
import com.master.myapplication2.models.User
import com.master.myapplication2.network.ApiClient
import com.master.myapplication2.network.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.core.content.edit
import com.master.myapplication2.models.LoginResponse
import com.master.myapplication2.utils.UserSessionManager

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        apiService = ApiClient.createService(ApiService::class.java)

        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Handle back button click
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

//        val btnSignUp = findViewById<Button>(R.id.btnSignUp)
//        val btnLogin = findViewById<Button>(R.id.btnLogin)

        // Login button
        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginUser(username, password)
        }

        // Go to signup
        binding.btnSignUp.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun loginUser(username: String, password: String) {
        val credentials = mapOf("username" to username, "password" to password)

        apiService.login(credentials).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val loginRes = response.body()
                    val userId = loginRes?.userId ?: ""
                    val username = loginRes?.username ?: ""
                    val name = loginRes?.name ?: ""
                    val email = loginRes?.email ?: ""

                    Toast.makeText(
                        this@LoginActivity,
                        "Welcome $name (id: $userId)",
                        Toast.LENGTH_SHORT
                    ).show()

                    UserSessionManager.saveUserSession(this@LoginActivity, userId, username, name, email)
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, "Invalid username or password", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
