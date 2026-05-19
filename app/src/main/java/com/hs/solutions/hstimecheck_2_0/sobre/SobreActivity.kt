package com.hs.solutions.hstimecheck_2_0.sobre

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

class SobreActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                SobreScreen()
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SobreScreen() {

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sobre") },
                navigationIcon = {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // 🔹 TÍTULO
            Text(
                text = "HS TimeCheck ",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            // 🔹 DESCRIÇÃO
            Text(
                text = """
O HS TimeCheck é um sistema desenvolvido para auxiliar no controle de produtos, validades e fluxos operacionais no varejo.

Ele centraliza informações de estoque, vencimentos, aprovações comerciais e histórico de ações, garantindo mais segurança, organização e rastreabilidade no dia a dia da loja.
                """.trimIndent(),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(20.dp))
            Divider()
            Spacer(Modifier.height(16.dp))

            // 🔹 FUNCIONALIDADES
            Text(
                text = "Principais Funcionalidades",
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            FuncionalidadeItem("• Controle de validade de produtos")
            FuncionalidadeItem("• Produtos vencendo e vencidos")
            FuncionalidadeItem("• Aprovação comercial")
            FuncionalidadeItem("• Trabalho de preço / queima de estoque")
            FuncionalidadeItem("• Verificação de estoque")
            FuncionalidadeItem("• Histórico completo de ações")
            FuncionalidadeItem("• Cadastro com código de barras e fotos")

            Spacer(Modifier.height(20.dp))
            Divider()
            Spacer(Modifier.height(16.dp))

            // 🔹 VERSÃO
            Text(
                text = "Versão",
                fontWeight = FontWeight.Bold
            )
            Text("2.0")

            Spacer(Modifier.height(20.dp))
            Divider()
            Spacer(Modifier.height(16.dp))

            // 🔹 CRÉDITOS
            Text(
                text = "Créditos",
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = """
Base de dados de produtos:
Open Food Facts (https://world.openfoodfacts.org)

Apoio técnico e orientação:
ChatGPT – OpenAI

Projeto desenvolvido para controle operacional e gestão de validade no varejo.
                """.trimIndent(),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            // 🔹 FRASE FINAL
            Text(
                text = "Organização, controle e rastreabilidade em um único lugar.",
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun FuncionalidadeItem(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.fillMaxWidth()
    )
}
