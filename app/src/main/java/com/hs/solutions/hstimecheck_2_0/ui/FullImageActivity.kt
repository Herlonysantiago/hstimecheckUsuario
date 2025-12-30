package com.hs.solutions.hstimecheck_2_0.ui

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.hs.solutions.hstimecheck_2_0.core.BarcodeUtils
import java.io.File

class FullImageActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isBarcode = intent.getBooleanExtra("isBarcode", false)
        val barcodeContent = intent.getStringExtra("barcodeContent")
        val fotoUrl = intent.getStringExtra("fotoUrl")

        setContent {
            MaterialTheme {
                FullImageScreen(
                    isBarcode = isBarcode,
                    barcodeContent = barcodeContent,
                    fotoUrl = fotoUrl,
                    onClose = { finish() }
                )
            }
        }
    }
}

@Composable
fun FullImageScreen(
    isBarcode: Boolean,
    barcodeContent: String?,
    fotoUrl: String?,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable { onClose() },
        contentAlignment = Alignment.Center
    ) {

        if (isBarcode && !barcodeContent.isNullOrBlank()) {

            val bitmap = remember(barcodeContent) {
                BarcodeUtils.gerarCode128(
                    valor = barcodeContent,
                    largura = 1400,
                    altura = 500
                )
            }

            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Código de barras",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )

        } else if (!fotoUrl.isNullOrBlank()) {

            AsyncImage(
                model = resolverImagem(fotoUrl),
                contentDescription = "Imagem",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * Resolve corretamente:
 * - URL (internet)
 * - content:// (galeria)
 * - File path (câmera)
 */
fun resolverImagem(url: String): Any =
    when {
        url.startsWith("http", true) -> url
        url.startsWith("content://", true) -> Uri.parse(url)
        else -> File(url)
    }
