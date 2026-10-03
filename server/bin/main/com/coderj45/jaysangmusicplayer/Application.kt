package com.coderj45.jaysangmusicplayer

import com.coderj45.jaysangmusicplayer.model.Song
import com.coderj45.jaysangmusicplayer.model.mockSongs
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }
    
    routing {
        get("/") {
            call.respondText("JaySang Music Server is running!")
        }
        
        get("/songs") {
            // In a real app, this would scan a directory. For now, we'll return mockSongs
            // but with URLs pointing to our stream endpoint.
            val songsWithUrls = mockSongs.map { song ->
                song.copy(albumArtUrl = "http://10.0.2.2:8080/stream/${song.id}") // Placeholder for streaming URL
            }
            call.respond(songsWithUrls)
        }
        
        get("/stream/{id}") {
            // For now, redirect to the test URL we used before.
            // In Phase 2 extension, we would serve local files.
            call.respondRedirect("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3")
        }
    }
}
