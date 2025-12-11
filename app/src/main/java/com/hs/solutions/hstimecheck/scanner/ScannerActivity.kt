package com.hs.solutions.hstimecheck.scanner
import android.media.AudioManager
import android.media.ToneGenerator

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.hs.solutions.hstimecheck.R
import com.hs.solutions.hstimecheck.core.ProductLookupService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScannerActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "ScannerActivity"
        private const val REQUEST_CAMERA = 1001
    }

    private lateinit var previewView: PreviewView
    private lateinit var tvStatus: TextView
    private lateinit var lookup: ProductLookupService

    private var readingLocked = false
    private var lastScanTime = 0L
    private val scanDelay = 700L // debounce em ms

    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)

        previewView = findViewById(R.id.previewView)
        tvStatus = findViewById(R.id.tvStatusScanner)
        lookup = ProductLookupService(this)
        cameraExecutor = Executors.newSingleThreadExecutor()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            Toast.makeText(this, "Permissão de câmera negada.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun startCamera() {
        val providerFuture = ProcessCameraProvider.getInstance(this)
        providerFuture.addListener({
            try {
                val cameraProvider = providerFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val analysisUseCase = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                val barcodeClient = BarcodeScanning.getClient() // pode passar opções se quiser limitar formatos

                analysisUseCase.setAnalyzer(cameraExecutor) { proxy ->
                    try {
                        if (readingLocked) { proxy.close(); return@setAnalyzer }

                        val now = System.currentTimeMillis()
                        if (now - lastScanTime < scanDelay) { proxy.close(); return@setAnalyzer }
                        lastScanTime = now

                        val mediaImage = proxy.image
                        if (mediaImage == null) { proxy.close(); return@setAnalyzer }

                        val rotation = proxy.imageInfo.rotationDegrees
                        val inputImage = InputImage.fromMediaImage(mediaImage, rotation)

                        // Processa com ML Kit — callbacks em main executor para atualizar UI/flow
                        barcodeClient.process(inputImage)
                            .addOnSuccessListener(ContextCompat.getMainExecutor(this)) { barcodes ->
                                val barcode = barcodes.firstOrNull()
                                val raw = barcode?.rawValue

                                // Filtra por region central (opcional) para reduzir falsos positivos
                                val inCenter = barcode?.boundingBox?.let { box ->
                                    val centerRect = android.graphics.Rect(
                                        (inputImage.width * 0.15).toInt(),
                                        (inputImage.height * 0.30).toInt(),
                                        (inputImage.width * 0.85).toInt(),
                                        (inputImage.height * 0.70).toInt()
                                    )
                                    box.intersect(centerRect)
                                } ?: true

                                if (!raw.isNullOrBlank() && inCenter && !readingLocked) {
                                    readingLocked = true
                                    tvStatus.text = "Encontrado: $raw"
                                    handleFoundCode(raw)
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.w(TAG, "MLKit error", e)
                            }
                            .addOnCompleteListener {
                                proxy.close()
                            }

                    } catch (e: Exception) {
                        Log.e(TAG, "Analyzer error", e)
                        try { proxy.close() } catch (_: Exception) {}
                    }
                }

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysisUseCase)

            } catch (e: Exception) {
                Log.e(TAG, "Erro ao iniciar câmera", e)
                Toast.makeText(this, "Erro ao iniciar câmera: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun handleFoundCode(code: String) {
        lifecycleScope.launch {
            // lookup em IO (pode acessar JSON / DB / rede)
            val jsonItem = withContext(Dispatchers.IO) {
                lookup.buscarPorCodigoBarras(code)
            }

            // monta Intent de retorno
            val resultIntent = Intent().apply {
                putExtra("codigo_barras", code)
                putExtra("codigo_interno", jsonItem?.codigo?.toString() ?: "")
                putExtra("descricao", jsonItem?.descricao?.trim() ?: jsonItem?.complemento?.trim() ?: "")
            }
// ---- BEEP AO LER O CÓDIGO ----
            ToneGenerator(AudioManager.STREAM_MUSIC, 100)
                .startTone(ToneGenerator.TONE_PROP_BEEP, 150)

            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            cameraExecutor.shutdown()
        } catch (e: Exception) {
            Log.w(TAG, "Erro ao liberar executor", e)
        }
    }
}
