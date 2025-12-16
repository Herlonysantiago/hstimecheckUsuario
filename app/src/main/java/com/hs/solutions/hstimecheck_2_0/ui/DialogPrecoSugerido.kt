/*
package com.hs.solutions.hstimecheck.ui

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.hs.solutions.hstimecheck.models.Produto



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogPrecoSugerido(
    produto: Produto,
    onConfirmar: (Double) -> Unit,
    onCancelar: () -> Unit
) {

    var valorTexto by remember {
        mutableStateOf(
            produto.precoAtual?.let { "%.2f".format(it) } ?: ""
        )
    }

    val valorDouble = valorTexto.replace(",", ".").toDoubleOrNull()
    val valido = valorDouble != null && valorDouble > 0.0

    AlertDialog(
        onDismissRequest = onCancelar,
        title = {
            Text("Preço sugerido para aprovação")
        },
        text = {
            Column {

                Text(
                    text = produto.descricao,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Preço atual: ${
                        produto.precoAtual?.let { "R$ %.2f".format(it) } ?: "—"
                    }"
                )

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = valorTexto,
                    onValueChange = { valorTexto = it },
                    label = { Text("Preço sugerido") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    isError = valorTexto.isNotBlank() && !valido
                )

                if (valorTexto.isNotBlank() && !valido) {
                    Text(
                        text = "Informe um valor válido",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = valido,
                onClick = {
                    valorDouble?.let { onConfirmar(it) }
                }
            ) {
                Text("Enviar para aprovação")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text("Cancelar")
            }
        }
    )
}
*/
