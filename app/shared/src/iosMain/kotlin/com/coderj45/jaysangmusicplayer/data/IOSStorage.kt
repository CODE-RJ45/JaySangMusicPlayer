package com.coderj45.jaysangmusicplayer.data

import platform.Foundation.NSUserDefaults

class IOSStorage : Storage {
    private val defaults = NSUserDefaults.standardUserDefaults

    override fun getString(key: String): String? {
        return defaults.stringForKey(key)
    }

    override fun setString(key: String, value: String) {
        defaults.setObject(value, key)
    }
}
