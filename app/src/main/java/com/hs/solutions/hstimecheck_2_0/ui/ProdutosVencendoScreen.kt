//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.History
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import androidx.lifecycle.viewmodel.compose.viewModel
//import com.hs.solutions.hstimecheck_2_0.models.Produto
//import com.hs.solutions.hstimecheck_2_0.vencendo.ProdutosVencendoViewModel
//@Composable
//fun ProdutosVencendoScreen(
//    viewModel: ProdutosVencendoViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
//    onAbrirHistorico: (Produto) -> Unit
//) {
//    val produtos by viewModel.produtos.collectAsState()
//
//    LaunchedEffect(Unit) {
//        viewModel.carregar()
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Produtos Vencendo") }
//            )
//        }
//    ) { padding ->
//        if (produtos.isEmpty()) {
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(padding),
//                contentAlignment = Alignment.Center
//            ) {
//                Text("Nenhum produto vencendo")
//            }
//        } else {
//            LazyColumn(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(padding)
//            ) {
//                items(produtos) { produto ->
//                    ProdutoVencendoItem(
//                        produto = produto,
//                        onAprovar = { viewModel.enviarParaAprovacao(produto) },
//                        onTrabalharPreco = { viewModel.trabalharPreco(produto) },
//                        onExcluir = { viewModel.excluirValidade(produto) },
//                        onHistorico = { onAbrirHistorico(produto) }
//                    )
//                }
//            }
//        }
//    }
//}
