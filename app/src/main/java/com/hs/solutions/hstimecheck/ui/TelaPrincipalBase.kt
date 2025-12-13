package com.hs.solutions.hstimecheck.ui

// COMPOSE
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// APP
import com.hs.solutions.hstimecheck.core.ProductService
import com.hs.solutions.hstimecheck.models.*

/* =============================================================
   FUNÇÃO AUXILIAR — define qual grupo a lista abre
   ============================================================= */
fun grupoInicialParaFiltro(
    filtro: FiltroLista,
    chavesOrdenadas: List<Int>
): Int {
    return when (filtro) {

        FiltroLista.ESSA_SEMANA ->
            chavesOrdenadas.indexOfFirst { it in 0..7 }

        FiltroLista.ATE_15_DIAS ->
            chavesOrdenadas.indexOfFirst { it in 0..15 }

        FiltroLista.QUEIMANDO_ESTOQUE,
        FiltroLista.AGUARDANDO_APROVACAO ->
            0

        else -> 0
    }.coerceAtLeast(0)
}

/* =============================================================
   TELA BASE (REUTILIZA SEU LAYOUT E COMPONENTES)
   ============================================================= */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaPrincipalBase(
    service: ProductService,
    filtro: FiltroLista
) {

    val listState = rememberLazyListState()
    var query by remember { mutableStateOf("") }
    val produtos by service.produtos.collectAsState()

    LaunchedEffect(Unit) {
        service.carregar()
    }

    /* ================= BUSCA + FILTRO ================= */

    val listaFiltrada = produtos.filter { p ->

        val passaBusca =
            query.isBlank() ||
                    p.descricao.contains(query, true) ||
                    p.codigoBarras.contains(query)

        val passaFiltro = when (filtro) {
            FiltroLista.TODOS -> true
            FiltroLista.ESSA_SEMANA ->
                getDiasRestantes(p.validadeAtual) in 0..7
            FiltroLista.ATE_15_DIAS ->
                getDiasRestantes(p.validadeAtual) in 0..15
            FiltroLista.QUEIMANDO_ESTOQUE ->
                p.status == StatusProduto.TRABALHANDO_PRECO
            FiltroLista.AGUARDANDO_APROVACAO ->
                p.status == StatusProduto.AGUARDANDO_APROVACAO
        }

        passaBusca && passaFiltro
    }

    /* ================= AGRUPAMENTO ================= */

    val grupos = listaFiltrada.groupBy { getDiasRestantes(it.validadeAtual) }
    val chavesOrdenadas = grupos.keys.sorted()

    /* ================= SCROLL AUTOMÁTICO ================= */

    LaunchedEffect(filtro, chavesOrdenadas) {

        val indiceGrupo = grupoInicialParaFiltro(
            filtro = filtro,
            chavesOrdenadas = chavesOrdenadas
        )

        var indexNaLista = 0
        for (i in 0 until indiceGrupo) {
            indexNaLista += 1 // header
            indexNaLista += grupos[chavesOrdenadas[i]]?.size ?: 0
        }

        if (indexNaLista >= 0) {
            listState.scrollToItem(indexNaLista)
        }
    }

    /* ================= UI ================= */

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("HS TimeCheck – ${filtro.name.replace('_', ' ')}")
                }
            )
        }
    ) { padding ->

        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                leadingIcon = { Icon(Icons.Default.Search, null) },
                placeholder = { Text("Buscar produto...") },
                singleLine = true
            )

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {

                chavesOrdenadas.forEach { chave ->

                    val grupo = grupos[chave] ?: emptyList()

                    val label = when {
                        chave == Int.MAX_VALUE -> "Sem validade"
                        chave < 0 -> "Vencido"
                        chave == 0 -> "Hoje"
                        chave == 1 -> "1 dia restante"
                        else -> "$chave dias restantes"
                    }

                    item {
                        SectionHeader(label)
                    }

                    items(grupo) { produto ->
                        ProdutoItem(
                            produto = produto,
                            isSelected = false,
                            onClick = {},
                            onLongPress = {},
                            onDoubleClick = {}
                        )
                    }
                }
            }
        }
    }
}
