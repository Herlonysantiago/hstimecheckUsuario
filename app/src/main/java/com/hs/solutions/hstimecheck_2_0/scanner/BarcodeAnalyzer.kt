package com.hs.solutions.hstimecheck_2_0.scanner

import android.annotation.SuppressLint
import android.graphics.Rect
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

class BarcodeAnalyzerUltra(
    private val onBarcodeDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(
            Barcode.FORMAT_EAN_13,
            Barcode.FORMAT_EAN_8,
            Barcode.FORMAT_CODE_128,
            Barcode.FORMAT_ITF
        )
        .build()

    private val scanner = BarcodeScanning.getClient(options)

    @Volatile
    private var processing = false

    private var lastValue: String? = null

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {

        val mediaImage = imageProxy.image ?: run {
            imageProxy.close()
            return
        }

        // evita processar vários frames ao mesmo tempo
        if (processing) {
            imageProxy.close()
            return
        }
        processing = true

        val rotation = imageProxy.imageInfo.rotationDegrees
        val image = InputImage.fromMediaImage(mediaImage, rotation)

        // REGIÃO CENTRAL DE LEITURA (70% x 40%)
        val centerRegion = Rect(
            (image.width * 0.15).toInt(),
            (image.height * 0.30).toInt(),
            (image.width * 0.85).toInt(),
            (image.height * 0.70).toInt()
        )

        scanner.process(image)
            .addOnSuccessListener { barcodes ->

                // encontra um barcode dentro da área central
                val barcode = barcodes.firstOrNull { it.boundingBox?.let { box ->
                    box.intersect(centerRegion)
                } ?: false }

                val rawValue = barcode?.rawValue

                if (rawValue != null && rawValue != lastValue) {
                    lastValue = rawValue
                    onBarcodeDetected(rawValue)
                }
            }
            .addOnCompleteListener {
                processing = false
                imageProxy.close()
            }
    }
}
