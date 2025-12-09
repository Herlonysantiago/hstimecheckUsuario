package com.hs.solutions.hstimecheck.scanner

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.hs.solutions.hstimecheck.R
import com.hs.solutions.hstimecheck.core.AppContainer
import com.hs.solutions.hstimecheck.core.ProductLookupService
import com.hs.solutions.hstimecheck.detail.ProdutoDetalheActivity
import com.hs.solutions.hstimecheck.cadastro.CadastroProdutoActivity
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScannerActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var tvStatus: TextView
    private lateinit var btnClose: ImageButton

    private val productService = AppContainer.productService
    private val lookup by lazy { ProductLookupService(this) }

    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var alreadyScanned = false

    // ------------------- PERMISSÃO --------------------
    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) startCamera()
            else {
                Toast.makeText(this, "Permissão da câmera negada.", Toast.LENGTH_LONG).show()
                finish()
            }
        }

    // -------------------- CADASTRO CALLBACK ----------------------
    private val cadastroLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val id = result.data?.getStringExtra("produto_id")
                if (id != null) {
                    abrirDetalhes(id)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)

        previewView = findViewById(R.id.previewView)
        tvStatus = findViewById(R.id.tvStatusScanner)
        btnClose = findViewById(R.id.btnClose)

        btnClose.setOnClickListener { finish() }

        // Permissão
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // -------------------- CÂMERA -------------------------
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(previewView.surfaceProvider)

            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            analysis.setAnalyzer(cameraExecutor, BarcodeAnalyzer { code ->
                runOnUiThread { onBarcodeDetected(code) }
            })

            val selector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, selector, preview, analysis)
            } catch (e: Exception) {
                Toast.makeText(this, "Erro ao iniciar câmera: ${e.message}", Toast.LENGTH_LONG).show()
            }

        }, ContextCompat.getMainExecutor(this))
    }

    // -------------------- BARCODE DETECTADO -------------------------
    private fun onBarcodeDetected(code: String) {
        if (alreadyScanned) return
        alreadyScanned = true

        tvStatus.text = "Código lido: $code"

        lifecycleScope.launch {
            processBarcode(code)
        }

        previewView.postDelayed({ alreadyScanned = false }, 1500)
    }

    // -------------------- PROCESSAMENTO -----------------------
    private suspend fun processBarcode(code: String) {

        // 1) VERIFICA NO SISTEMA
        val existente = productService.produtos.value.find { it.codigoBarras == code }

        if (existente != null) {
            abrirDetalhes(existente.id)
            return
        }

        // 2) NÃO EXISTE — ENVIAR PARA CADASTRO
        abrirCadastro(code)
    }

    // -------------------- ABRIR TELA DE CADASTRO -------------------
    private fun abrirCadastro(code: String) {
        val intent = Intent(this, CadastroProdutoActivity::class.java)
        intent.putExtra("codigo_barras", code)
        cadastroLauncher.launch(intent)
    }

    // -------------------- ABRIR DETALHES -------------------
    private fun abrirDetalhes(id: String) {
        val intent = Intent(this, ProdutoDetalheActivity::class.java)
        intent.putExtra("produto_id", id)
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
