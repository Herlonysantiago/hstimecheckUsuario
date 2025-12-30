package com.hs.solutions.hstimecheck_2_0.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.hs.solutions.hstimecheck_2_0.core.BarcodeUtils
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class BarcodeFullscreenActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val codigo = intent.getStringExtra("codigo_barras") ?: return

        setContent {
            BarcodeFullscreenScreen(
                codigo = codigo,
                onClose = { finish() }
            )
        }
    }
}
@Composable
fun BarcodeFullscreenScreen(
    codigo: String,
    onClose: () -> Unit
) {
    val bitmap: Bitmap = remember(codigo) {
        BarcodeUtils.gerarCode128(
            valor = codigo,
            largura = 1400,
            altura = 500
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable { onClose() },
        contentAlignment = Alignment.Center
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Código de barras CODE 128",
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentScale = ContentScale.Fit
        )
    }
}