package com.cleon.lokerku.awan.helper.preference

import android.content.Context
import android.content.SharedPreferences

class UserPreference(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("user_pref", Context.MODE_PRIVATE)

    fun saveUsername(username: String) {
        sharedPreferences.edit().putString("username", username).apply()
    }

    fun getUsername(): String? {
        return sharedPreferences.getString("username", null)
    }

    fun deleteUsername() {
        sharedPreferences.edit().remove("username").apply()
    }
}
