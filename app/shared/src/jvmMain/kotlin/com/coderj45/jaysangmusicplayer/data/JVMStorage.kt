package com.coderj45.jaysangmusicplayer.data

import java.util.prefs.Preferences

class JVMStorage : Storage {
    private val prefs = Preferences.userNodeForPackage(JVMStorage::class.java)

    override fun getString(key: String): String? {
        return prefs.get(key, null)
    }

    override fun setString(key: String, value: String) {
        prefs.put(key, value)
    }
}
