package com.master.myapplication2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.master.myapplication2.utils.UserSessionManager

class AccountFragment : Fragment() {

    private lateinit var layoutLoggedOut: View
    private lateinit var layoutLoggedIn: View
    private lateinit var tvUsername: TextView
    private lateinit var tvEmail: TextView
    private lateinit var btnLogout: TextView

    private val prefsName = "user_session"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_account, container, false)

        layoutLoggedOut = view.findViewById(R.id.layout_logged_out)
        layoutLoggedIn = view.findViewById(R.id.layout_logged_in)
        tvUsername = view.findViewById(R.id.tv_username)
        tvEmail = view.findViewById(R.id.tv_email)
        btnLogout = view.findViewById(R.id.btn_logout)

        updateUI()

        layoutLoggedOut.setOnClickListener {
            // Navigate to login screen
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
        }

        btnLogout.setOnClickListener {
            logoutUser()
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        // Refresh user info in case it changed after login
        updateUI()
    }

    private fun updateUI() {
        if (UserSessionManager.isLoggedIn(requireContext())) {
            layoutLoggedOut.visibility = View.GONE
            layoutLoggedIn.visibility = View.VISIBLE
            val username = UserSessionManager.getUsername(requireContext())
            val name = UserSessionManager.getName(requireContext())
            tvUsername.text = "$name ($username)"
            tvEmail.text = UserSessionManager.getEmail(requireContext())
        } else {
            layoutLoggedOut.visibility = View.VISIBLE
            layoutLoggedIn.visibility = View.GONE
        }
    }

    private fun logoutUser() {
        UserSessionManager.clearSession(requireContext())
        updateUI()
    }

}
