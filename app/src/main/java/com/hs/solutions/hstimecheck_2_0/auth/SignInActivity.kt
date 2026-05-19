package com.hs.solutions.hstimecheck_2_0.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.hs.solutions.hstimecheck_2_0.R
import com.hs.solutions.hstimecheck_2_0.alerts.agendarAlertas
import com.hs.solutions.hstimecheck_2_0.core.AppContainer
import com.hs.solutions.hstimecheck_2_0.ui.TelaPrincipalActivity
import com.hs.solutions.hstimecheck_2_0.ui.theme.HsTimeCheckTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SignInActivity : ComponentActivity() {

    private val firebaseAuth = FirebaseAuth.getInstance()

    private val googleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        lifecycleScope.launch {
            try {
                val account = task.await()
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                firebaseAuth.signInWithCredential(credential).await()
                abrirApp()
            } catch (e: Exception) {
                Toast.makeText(
                    this@SignInActivity,
                    "Nao foi possivel entrar com Google: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (AuthSession.isSignedIn()) {
            abrirApp()
            return
        }

        setContent {
            HsTimeCheckTheme {
                SignInScreen(
                    onGoogleClick = { iniciarLoginGoogle() }
                )
            }
        }
    }

    private fun iniciarLoginGoogle() {
        val webClientId = getWebClientId()
        if (webClientId.isBlank() || webClientId.startsWith("COLOQUE_")) {
            Toast.makeText(
                this,
                "Configure o Web Client ID do Google no Firebase antes de entrar.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()

        val client = GoogleSignIn.getClient(this, options)
        googleLauncher.launch(client.signInIntent)
    }

    private fun getWebClientId(): String {
        val generatedId = resources.getIdentifier("default_web_client_id", "string", packageName)
        if (generatedId != 0) {
            return getString(generatedId)
        }
        return getString(R.string.google_web_client_id)
    }

    private fun abrirApp() {
        AppContainer.init(applicationContext)
        agendarAlertas(applicationContext)
        startActivity(Intent(this, TelaPrincipalActivity::class.java))
        finish()
    }
}

@Composable
private fun SignInScreen(onGoogleClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FC))
            .padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = null,
            modifier = Modifier.size(96.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "HS TimeCheck",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF12324A),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Entre com sua conta Google para carregar uma base separada e segura.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF44515C),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onGoogleClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
        ) {
            Text("Entrar com Google")
        }
    }
}
