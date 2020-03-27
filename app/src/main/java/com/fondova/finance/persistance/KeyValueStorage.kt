package com.fondova.finance.persistance

import android.content.SharedPreferences

interface KeyValueStorage {
    fun set(key: String, value: String)
    fun get(key: String): String?
    fun delete(key: String)
    fun clearAll()
}

class SharedPreferencesStorage(val sharedPreferences: SharedPreferences): KeyValueStorage {

    override fun set(key: String, value: String) {
        with (sharedPreferences.edit()) {
            putString(key, value)
            commit()
        }
    }

    override fun get(key: String): String? {
        return sharedPreferences.getString(key, "")
    }

    override fun delete(key: String) {
        with (sharedPreferences.edit()) {
            remove(key)
            apply()
        }
    }

    override fun clearAll() {
        val editor = sharedPreferences.edit()
        for (key in sharedPreferences.all) {
            editor.remove(key.key)
        }
        editor.apply()
    }
}