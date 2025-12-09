package com.hs.solutions.hstimecheck.scanner

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

class BarcodeAnalyzer(
    private val onBarcodeDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    // Scanner configurado para todos os formatos (EAN, QR, Code128, ITF, etc.)
    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
        .build()

    private val scanner = BarcodeScanning.getClient(options)

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: run {
            imageProxy.close()
            return
        }

        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                // pega o primeiro valor encontrado
                val rawValue = barcodes.firstNotNullOfOrNull { it.rawValue }

                if (rawValue != null) {
                    onBarcodeDetected(rawValue)
                }
            }
            .addOnFailureListener {
                // erro silencioso para não travar leitura
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}
