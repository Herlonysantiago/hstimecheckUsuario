package com.hs.solutions.hstimecheck_2_0.historico

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
@Composable
fun HistoricoCard(item: HistoricoViewItem) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Column(Modifier.padding(12.dp)) {

            // 🔹 TÍTULO
            Text(
                text = item.titulo,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(4.dp))

            // 🔹 CÓDIGOS
            Text(
                text = buildString {
                    item.codigoInterno?.let { append("CI: $it  ") }
                    item.codigoBarras?.let { append("CB: $it") }
                },
                style = MaterialTheme.typography.labelSmall
            )

            // 🔹 VALIDADE
            item.validade?.let {
                Text(
                    text = "Validade: $it",
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Spacer(Modifier.height(6.dp))

            // 🔹 DESCRIÇÃO
            Text(
                text = item.descricao,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(8.dp))

            // 🔹 DATA
            Text(
                text = item.dataEvento,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
