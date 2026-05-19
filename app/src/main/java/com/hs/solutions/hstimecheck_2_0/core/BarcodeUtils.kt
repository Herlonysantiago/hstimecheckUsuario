package com.hs.solutions.hstimecheck_2_0.core

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix

object BarcodeUtils {
    fun extrairCodigoInternoBalanca(barcode: String): String {
        if (!barcode.startsWith("20") || barcode.length != 13) return barcode
        val fatiaCodigo = barcode.substring(2, 7)
        return fatiaCodigo.toLongOrNull()?.toString() ?: fatiaCodigo
    }

    fun gerarCode128(
        valor: String,
        largura: Int = 900,
        altura: Int = 300
    ): Bitmap {
        val matrix: BitMatrix = MultiFormatWriter().encode(
            valor,
            BarcodeFormat.CODE_128,
            largura,
            altura
        )

        val bmp = Bitmap.createBitmap(largura, altura, Bitmap.Config.ARGB_8888)
        for (x in 0 until largura) {
            for (y in 0 until altura) {
                bmp.setPixel(
                    x,
                    y,
                    if (matrix[x, y]) Color.BLACK else Color.WHITE
                )
            }
        }
        return bmp
    }
}