package com.hs.solutions.hstimecheck_2_0.trabalhando_preco

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hs.solutions.hstimecheck_2_0.models.Produto

@Composable
fun ItemTrabalhandoPreco(
    produto: Produto,
    selecionado: Boolean,
    onSelecionar: () -> Unit,
    onEnviarParaAprovacao: () -> Unit,
    onMarcarPrecoEmNegociacao: (String?) -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selecionado)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        onClick = onSelecionar
    ) {
        Column(Modifier.padding(12.dp)) {

            // =========================
            // DADOS PRINCIPAIS
            // =========================
            Text(
                " ${produto.codigoBarras}",
                fontWeight = FontWeight.Bold
            )

            Text(
                " ${produto.codigoInterno ?: "—"} - ${produto.descricao}"
            )

            Text(
                "Validade: ${produto.validadeAtual ?: "—"}",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(Modifier.height(6.dp))

            Text(
                "Preço atual: R$ ${produto.precoAtual ?: 0.0}",
                fontWeight = FontWeight.SemiBold
            )

            // =========================
            // BADGES DE STATUS
            // =========================
            Spacer(Modifier.height(6.dp))

            Row {
                Text(
                    "Trabalhando preço",
                    color = Color(0xFFD32F2F),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 8.dp)
                )

                if (produto.precoEmNegociacao) {
                    Text(
                        "Preço em negociação",
                        color = Color(0xFFFF9800),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // =========================
            // AÇÕES
            // =========================

            // 🔹 1. PEDIR MELHOR PREÇO (WHATSAPP)
            Button(
                onClick = {
                    val mensagem = """
                        Olá,
                        
                        Estou com o produto:
                        ${produto.descricao}
                         ${produto.codigoInterno}
                        
                        Preço atual: R$ ${produto.precoAtual ?: 0.0}
                        validade : ${produto.validadeAtual}
                        Estoque : ${produto.quantidadeAtual}
                        Preciso avaliar um melhor preço.
                    """.trimIndent()

                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(
                            "https://wa.me/?text=${Uri.encode(mensagem)}"
                        )
                    )

                    context.startActivity(intent)

                    // marca como em negociação + histórico
                    onMarcarPrecoEmNegociacao("Solicitação de melhor preço via WhatsApp")
                }
            ) {
                Text("Pedir melhor preço (WhatsApp)")
            }

            Spacer(Modifier.height(8.dp))

            // 🔹 2. ENVIAR PARA CLIENTES
            OutlinedButton(
                onClick = {
                    val mensagem = """
                        Oferta especial:
                        
                        ${produto.descricao}
                        Preço: R$ ${produto.precoAtual ?: 0.0}
                    """.trimIndent()

                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, mensagem)
                    }

                    context.startActivity(
                        Intent.createChooser(intent, "Enviar para clientes")
                    )
                }
            ) {
                Text("Enviar para clientes")
            }

            Spacer(Modifier.height(8.dp))

            // 🔹 3. ENVIAR PARA APROVAÇÃO (TELA 63)
            OutlinedButton(
                onClick = {
                    onEnviarParaAprovacao()

                    onMarcarPrecoEmNegociacao(
                        "Preço enviado para comprador (tela 63)"
                    )
                }
            ) {
                Text("Enviar para comprador (aprovação)")
            }
        }
    }
}
