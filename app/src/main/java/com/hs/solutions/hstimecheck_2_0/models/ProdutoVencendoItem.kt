//import androidx.compose.foundation.layout.*
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.History
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import com.hs.solutions.hstimecheck_2_0.models.Produto
//import java.time.LocalDate
//import java.time.temporal.ChronoUnit
//
//@Composable
//fun ProdutoVencendoItem(
//    produto: Produto,
//    onAprovar: () -> Unit,
//    onTrabalharPreco: () -> Unit,
//    onExcluir: () -> Unit,
//    onHistorico: () -> Unit
//) {
//    val dias = ChronoUnit.DAYS.between(LocalDate.now(), produto.validade)
//
//    Card(
//        modifier = Modifier
//            .padding(8.dp)
//            .fillMaxWidth(),
//        colors = CardDefaults.cardColors(
//            containerColor = when {
//                dias <= 1 -> Color(0xFFFFE0B2) // laranja
//                else -> Color(0xFFFFF8E1) // amarelo
//            }
//        )
//    ) {
//        Column(modifier = Modifier.padding(12.dp)) {
//            Text(produto.nome, fontWeight = FontWeight.Bold)
//            Text("Validade: ${produto.validade}")
//            Text("Dias restantes: $dias")
//            Text("Estoque: ${produto.estoque}")
//
//            Spacer(Modifier.height(8.dp))
//
//            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//                Button(onClick = onAprovar) {
//                    Text("Aprovação")
//                }
//                OutlinedButton(onClick = onTrabalharPreco) {
//                    Text("Preço")
//                }
//                OutlinedButton(onClick = onExcluir) {
//                    Text("Excluir")
//                }
//                IconButton(onClick = onHistorico) {
//                    Icon(Icons.Default.History, contentDescription = null)
//                }
//            }
//        }
//    }
//}
//
