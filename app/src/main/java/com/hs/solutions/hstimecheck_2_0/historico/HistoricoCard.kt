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
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Column(Modifier.padding(12.dp)) {

            Text(
                item.evento,
                fontWeight = FontWeight.Bold
            )

            Text(
                item.descricaoProduto,
                style = MaterialTheme.typography.bodySmall
            )

            Text(
                "Código: ${item.codigoBarras}",
                style = MaterialTheme.typography.bodySmall
            )

            item.detalhe?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text(
                item.dataEvento,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
