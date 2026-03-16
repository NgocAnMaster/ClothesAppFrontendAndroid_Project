package com.master.myapplication2.utils

import android.content.Context
import android.content.SharedPreferences

object UserSessionManager {
    private const val PREFS_NAME = "user_session"
    private const val KEY_USER_ID = "userId"
    private const val KEY_NAME = "name"
    private const val KEY_USERNAME = "username"
    private const val KEY_EMAIL = "email"
    private const val KEY_TOKEN = "token"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveUserSession(context: Context, userId: String, username: String, name: String, email: String) {
        getPrefs(context).edit().apply {
            putString(KEY_USER_ID, userId)
            putString(KEY_NAME, name)
            putString(KEY_USERNAME, username)
            putString(KEY_EMAIL, email)
            apply()
        }
    }

    fun isLoggedIn(context: Context): Boolean {
        return getPrefs(context).getString(KEY_USER_ID, null) != null
    }

    fun getUserId(context: Context): String {
        return getPrefs(context).getString(KEY_USER_ID, "") ?: ""
    }

    fun getName(context: Context): String {
        return getPrefs(context).getString(KEY_NAME, "") ?: ""
    }

    fun getUsername(context: Context): String {
        return getPrefs(context).getString(KEY_USERNAME, "") ?: ""
    }

    fun getEmail(context: Context): String {
        return getPrefs(context).getString(KEY_EMAIL, "") ?: ""
    }

    fun clearSession(context: Context) {
        getPrefs(context).edit().clear().apply()
    }
}
