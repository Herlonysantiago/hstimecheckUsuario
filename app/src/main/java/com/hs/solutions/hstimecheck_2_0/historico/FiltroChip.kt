package com.hs.solutions.hstimecheck_2_0.historico

import androidx.compose.material3.AssistChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding


@Composable
fun FiltroChip(texto: String, onClick: () -> Unit) {
    AssistChip(
        onClick = onClick,
        label = { Text(texto) },
        modifier = Modifier.padding(end = 6.dp)
    )
}
