package com.coderj45.jaysangmusicplayer.data

import android.content.Context
import android.content.SharedPreferences

class AndroidStorage(context: Context) : Storage {
    private val prefs: SharedPreferences = context.getSharedPreferences("music_player_prefs", Context.MODE_PRIVATE)

    override fun getString(key: String): String? {
        return prefs.getString(key, null)
    }

    override fun setString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }
}
