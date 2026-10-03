package com.coderj45.jaysangmusicplayer

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform