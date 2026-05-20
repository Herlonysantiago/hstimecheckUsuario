package com.hs.solutions.hstimecheck_2_0.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.NoCredentialException
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
import androidx.compose.material3.OutlinedButton
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
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (AuthSession.hasActiveSession(applicationContext)) {
            abrirApp()
            return
        }

        setContent {
            HsTimeCheckTheme {
                SignInScreen(
                    onGoogleClick = { iniciarLoginGoogle() },
                    onOfflineClick = { iniciarModoOffline() }
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

        lifecycleScope.launch {
            try {
                autenticarComCredentialManager(webClientId, filtrarContasAutorizadas = true)
            } catch (_: NoCredentialException) {
                autenticarComCredentialManager(webClientId, filtrarContasAutorizadas = false)
            } catch (e: Exception) {
                Toast.makeText(
                    this@SignInActivity,
                    "Nao foi possivel entrar com Google: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private suspend fun autenticarComCredentialManager(
        webClientId: String,
        filtrarContasAutorizadas: Boolean
    ) {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(webClientId)
            .setFilterByAuthorizedAccounts(filtrarContasAutorizadas)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val result = CredentialManager.create(this).getCredential(
            context = this,
            request = request
        )

        val credential = result.credential
        if (credential !is CustomCredential ||
            credential.type != TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            throw IllegalStateException("Credencial Google invalida.")
        }

        val googleCredential = try {
            GoogleIdTokenCredential.createFrom(credential.data)
        } catch (e: GoogleIdTokenParsingException) {
            throw IllegalStateException("Token Google invalido.", e)
        }

        val firebaseCredential = GoogleAuthProvider.getCredential(
            googleCredential.idToken,
            null
        )
        firebaseAuth.signInWithCredential(firebaseCredential).await()
        AuthSession.disableOfflineMode(applicationContext)
        AppContainer.reset()
        abrirApp()
    }

    private fun iniciarModoOffline() {
        AuthSession.enableOfflineMode(applicationContext)
        AppContainer.reset()
        abrirApp()
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
private fun SignInScreen(
    onGoogleClick: () -> Unit,
    onOfflineClick: () -> Unit
) {
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
            text = "Entre com Google para sincronizar ou use somente no celular em modo offline.",
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

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onOfflineClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text("Usar sem conta (offline)")
        }
    }
}
