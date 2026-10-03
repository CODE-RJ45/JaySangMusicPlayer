package com.coderj45.jaysangmusicplayer.data

interface Storage {
    fun getString(key: String): String?
    fun setString(key: String, value: String)
}
