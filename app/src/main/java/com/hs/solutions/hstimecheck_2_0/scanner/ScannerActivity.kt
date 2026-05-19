package com.hs.solutions.hstimecheck_2_0.scanner

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
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
import com.hs.solutions.hstimecheck_2_0.R
import com.hs.solutions.hstimecheck_2_0.core.ProductLookupService
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
    private lateinit var cameraExecutor: ExecutorService

    private var camera: Camera? = null
    private var flashLigado = false

    private var readingLocked = false
    private var lastScanTime = 0L
    private val scanDelay = 700L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)

        previewView = findViewById(R.id.previewView)
        tvStatus = findViewById(R.id.tvStatusScanner)
        lookup = ProductLookupService(this)
        cameraExecutor = Executors.newSingleThreadExecutor()

        val btnFlash = findViewById<ImageButton>(R.id.btnFlash)
        btnFlash.setOnClickListener {
            alternarFlash()
            btnFlash.setImageResource(
                if (flashLigado) R.drawable.ic_flash_on
                else R.drawable.ic_flash_off
            )
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            Toast.makeText(this, "Permissão de câmera negada.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun alternarFlash() {
        val cam = camera ?: return

        if (!cam.cameraInfo.hasFlashUnit()) {
            Toast.makeText(this, "Flash não disponível neste aparelho", Toast.LENGTH_SHORT).show()
            return
        }

        flashLigado = !flashLigado
        cam.cameraControl.enableTorch(flashLigado)
    }

    @SuppressLint("UnsafeOptInUsageError")
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

                val barcodeClient = BarcodeScanning.getClient()

                analysisUseCase.setAnalyzer(cameraExecutor) { proxy ->
                    try {
                        if (readingLocked) {
                            proxy.close()
                            return@setAnalyzer
                        }

                        val now = System.currentTimeMillis()
                        if (now - lastScanTime < scanDelay) {
                            proxy.close()
                            return@setAnalyzer
                        }
                        lastScanTime = now

                        val mediaImage = proxy.image ?: run {
                            proxy.close()
                            return@setAnalyzer
                        }

                        val inputImage = InputImage.fromMediaImage(
                            mediaImage,
                            proxy.imageInfo.rotationDegrees
                        )

                        barcodeClient.process(inputImage)
                            .addOnSuccessListener(
                                ContextCompat.getMainExecutor(this)
                            ) { barcodes ->
                                val barcode = barcodes.firstOrNull()
                                val raw = barcode?.rawValue

                                if (!raw.isNullOrBlank() && !readingLocked) {
                                    readingLocked = true
                                    tvStatus.text = "Encontrado: $raw"
                                    handleFoundCode(raw)
                                }
                            }
                            .addOnFailureListener {
                                Log.w(TAG, "Erro MLKit", it)
                            }
                            .addOnCompleteListener {
                                proxy.close()
                            }

                    } catch (e: Exception) {
                        Log.e(TAG, "Analyzer error", e)
                        proxy.close()
                    }
                }

                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    analysisUseCase
                )

            } catch (e: Exception) {
                Log.e(TAG, "Erro ao iniciar câmera", e)
                Toast.makeText(
                    this,
                    "Erro ao iniciar câmera: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun handleFoundCode(code: String) {
        lifecycleScope.launch {
            val jsonItem = withContext(Dispatchers.IO) {
                lookup.buscarPorCodigoBarras(code)
            }

            val resultIntent = Intent().apply {
                putExtra("codigo_barras", code)
                putExtra("codigo_interno", jsonItem?.codigo?.toString() ?: "")
                putExtra(
                    "descricao",
                    jsonItem?.descricao?.trim()
                        ?: jsonItem?.complemento?.trim()
                        ?: ""
                )
            }

            ToneGenerator(AudioManager.STREAM_MUSIC, 100)
                .startTone(ToneGenerator.TONE_PROP_BEEP, 150)

            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
