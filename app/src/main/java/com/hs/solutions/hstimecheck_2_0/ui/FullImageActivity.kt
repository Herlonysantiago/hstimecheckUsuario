package com.hs.solutions.hstimecheck_2_0.ui

import android.net.Uri
import android.os.Bundle
import java.io.File
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

class FullImageActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fotoUrl = intent.getStringExtra("fotoUrl")

        setContent {
            MaterialTheme {
                Box(Modifier.fillMaxSize()) {

                    fotoUrl?.let { urlSeguro ->

                        AsyncImage(
                            model = resolverImagem(urlSeguro),
                            contentDescription = "Foto",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )

                    }
                }
            }
        }
    }
}

/**
 * Resolve corretamente:
 * - URL (internet)
 * - content:// (galeria)
 * - File path (câmera)
 */
fun resolverImagem(url: String): Any {
    return when {
        url.startsWith("http", true) -> url
        url.startsWith("content://", true) -> Uri.parse(url)
        else -> File(url)
    }
}
