package com.hs.solutions.hstimecheck.ui

// FOTO / COIL
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.Image
import androidx.compose.ui.Alignment

// ANDROID
import android.app.Activity
import android.content.Intent
import android.os.Bundle

// COMPOSE
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// APP
import com.hs.solutions.hstimecheck.core.AppContainer
import com.hs.solutions.hstimecheck.core.ProductService
import com.hs.solutions.hstimecheck.cadastro.CadastroProdutoActivity
import com.hs.solutions.hstimecheck.models.Produto
import com.hs.solutions.hstimecheck.models.StatusProduto

// CORROTINAS
import kotlinx.coroutines.launch

import java.text.SimpleDateFormat
import java.util.*
import com.hs.solutions.hstimecheck.aprovacao.AprovacaoComercialActivity

class TelaPrincipalActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppContainer.init(this)
        val service = AppContainer.productService

        setContent {
            MaterialTheme {
                TelaPrincipal(service)
            }
        }
    }
}

/* =============================================================
   CÁLCULO DE DIAS RESTANTES (espera validade no formato yyyy-MM-dd)
   ============================================================= */
fun getDiasRestantes(validade: String?): Int {
    if (validade.isNullOrBlank()) return Int.MAX_VALUE // usaremos MAX_VALUE para "Sem validade"
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val validadeDate = sdf.parse(validade) ?: return Int.MAX_VALUE
        val currentDate = Date()
        val diffInMillis = validadeDate.time - currentDate.time
        (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
    } catch (_: Exception) {
        Int.MAX_VALUE
    }
}

@Composable
fun SectionHeader(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Divider(modifier = Modifier.weight(1f))
        Text(
            "  $text  ",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        Divider(modifier = Modifier.weight(1f))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawerMenu(
    onDashboard: () -> Unit = {},
    onProdutos: () -> Unit = {},
    onImportacao: () -> Unit = {},
    onExportacao: () -> Unit = {},
    onAprovacao: () -> Unit = {},
    onGerencial: () -> Unit = {},
    onQueimaPreco: () -> Unit = {},
    onEstoque: () -> Unit = {},
    onVencimentos: () -> Unit = {},
    onHistorico: () -> Unit = {},
    onConfiguracoes: () -> Unit = {},
    onSobre: () -> Unit = {},
    onCreditos: () -> Unit = {},
) {

    ModalDrawerSheet {

        Text(
            text = "Menu",
            modifier = Modifier.padding(16.dp),
            fontWeight = FontWeight.Bold
        )

        NavigationDrawerItem(
            label = { Text("Dashboard") },
            icon = { Icon(Icons.Default.Home, null) },
            selected = false,
            onClick = onDashboard
        )

        NavigationDrawerItem(
            label = { Text("Produtos") },
            selected = false,
            icon = { Icon(Icons.Default.Inventory, null) },
            onClick = onProdutos
        )

        SectionHeader("FLUXOS")

        NavigationDrawerItem(
            label = { Text("Aprovação Comercial") },
            icon = { Icon(Icons.Default.ThumbUp, null) },
            selected = false,
            onClick = onAprovacao
        )

        NavigationDrawerItem(
            label = { Text("Gerencial (Pendências)") },
            selected = false,
            icon = { Icon(Icons.Default.FactCheck, null) },
            onClick = onGerencial
        )

        NavigationDrawerItem(
            label = { Text("Trabalhando Preço / Queima de Estoque") },
            icon = { Icon(Icons.Default.LocalFireDepartment, null) },
            selected = false,
            onClick = onQueimaPreco
        )

        NavigationDrawerItem(
            label = { Text("Verificação de Estoque") },
            icon = { Icon(Icons.Default.BarChart, null) },
            selected = false,
            onClick = onEstoque
        )

        NavigationDrawerItem(
            label = { Text("Produtos Vencendo / Vencidos") },
            icon = { Icon(Icons.Default.AccessTime, null) },
            selected = false,
            onClick = onVencimentos
        )

        SectionHeader("DADOS")

        NavigationDrawerItem(
            label = { Text("Importar Planilha") },
            icon = { Icon(Icons.Default.FileUpload, null) },
            selected = false,
            onClick = onImportacao
        )

        NavigationDrawerItem(
            label = { Text("Exportar Dados") },
            icon = { Icon(Icons.Default.FileDownload, null) },
            selected = false,
            onClick = onExportacao
        )

        SectionHeader("RELATÓRIOS")

        NavigationDrawerItem(
            label = { Text("Histórico Geral") },
            icon = { Icon(Icons.Default.History, null) },
            selected = false,
            onClick = onHistorico
        )

        SectionHeader("SISTEMA")

        NavigationDrawerItem(
            label = { Text("Configurações") },
            icon = { Icon(Icons.Default.Settings, null) },
            selected = false,
            onClick = onConfiguracoes
        )

        NavigationDrawerItem(
            label = { Text("Sobre") },
            icon = { Icon(Icons.Default.Info, null) },
            selected = false,
            onClick = onSobre
        )

        NavigationDrawerItem(
            label = { Text("Créditos") },
            icon = { Icon(Icons.Default.Star, null) },
            selected = false,
            onClick = onCreditos
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaPrincipal(service: ProductService) {

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var query by remember { mutableStateOf("") }
    val produtos by service.produtos.collectAsState()

    var selectionMode by remember { mutableStateOf(false) }
    val selectedIds = remember { mutableStateListOf<String>() }

    val launcherCadastro =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                scope.launch { service.carregar() }
            }
        }

    LaunchedEffect(Unit) { service.carregar() }

    // listaFiltrada = TODOS produtos (não filtramos por validade) — mantém seu comportamento anterior de busca
    val listaFiltrada = produtos.filter {
        query.isBlank() ||
                it.descricao.contains(query, ignoreCase = true) ||
                it.codigoBarras.contains(query)
    }

    // AGRUPAMENTO: key = diasRestantes (Int), Int.MAX_VALUE => sem validade
    val grupos: Map<Int, List<Produto>> = listaFiltrada.groupBy { getDiasRestantes(it.validadeAtual) }

    // ordena chaves: vencidos (negativos) → 0 → positivos ascendentes → Int.MAX_VALUE (sem validade) por último
    val chavesOrdenadas = grupos.keys.sortedWith(compareBy { k ->
        when {
            k == Int.MAX_VALUE -> Int.MAX_VALUE
            else -> k
        }
    })

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerMenu(
                onDashboard = {
                    context.startActivity(
                        Intent(context, PainelOperacionalActivity::class.java)
                    )
                },

                        onAprovacao = {
                    context.startActivity(
                        Intent(context, AprovacaoComercialActivity::class.java)
                    )
                }
            )
        }
    )
    {

        Scaffold(
            topBar = {
                if (selectionMode) {
                    TopAppBar(
                        title = { Text("${selectedIds.size} selecionado(s)") },
                        navigationIcon = {
                            IconButton(onClick = {
                                selectionMode = false
                                selectedIds.clear()
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Cancelar seleção")
                            }
                        },
                        actions = {

                            // ENVIAR PARA APROVAÇÃO
                            IconButton(onClick = {
                                scope.launch {
                                    selectedIds.forEach { id ->
                                        val p = produtos.find { it.id == id }
                                        if (p != null) {
                                            service.mudarStatus(p, StatusProduto.AGUARDANDO_APROVACAO)
                                        }
                                    }
                                    selectionMode = false
                                    selectedIds.clear()
                                }
                            }) {
                                Icon(Icons.Default.ThumbUp, contentDescription = "Aprovação")
                            }

                            // ENVIAR PARA TRABALHANDO PREÇO
                            IconButton(onClick = {
                                scope.launch {
                                    selectedIds.forEach { id ->
                                        val p = produtos.find { it.id == id }
                                        if (p != null) {
                                            service.mudarStatus(p, StatusProduto.TRABALHANDO_PRECO)
                                        }
                                    }
                                    selectionMode = false
                                    selectedIds.clear()
                                }
                            }) {
                                Icon(Icons.Default.LocalFireDepartment, contentDescription = "Preço")
                            }

                            // VERIFICAÇÃO DE ESTOQUE
                            IconButton(onClick = {
                                scope.launch {
                                    selectedIds.forEach { id ->
                                        val p = produtos.find { it.id == id }
                                        if (p != null) {
                                            service.mudarStatus(p, StatusProduto.VERIFICACAO_ESTOQUE)
                                        }
                                    }
                                    selectionMode = false
                                    selectedIds.clear()
                                }
                            }) {
                                Icon(Icons.Default.Inventory, contentDescription = "Estoque")
                            }
                        }
                    )
                } else {
                    TopAppBar(
                        title = { Text("HS TimeCheck") },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        }
                    )
                }
            },


                    floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        launcherCadastro.launch(Intent(context, CadastroProdutoActivity::class.java))
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Adicionar Produto")
                }
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
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    singleLine = true,
                    placeholder = { Text("Buscar produto...") }
                )

                LazyColumn(Modifier.fillMaxSize()) {

                    chavesOrdenadas.forEach { chave ->

                        val grupo = grupos[chave] ?: emptyList()

                        val label = when {
                            chave == Int.MAX_VALUE -> "Sem validade"
                            chave < 0 -> "Vencido"
                            chave == 0 -> "Hoje"
                            chave == 1 -> "1 dia restante"
                            else -> "$chave dias restantes"
                        }

                        // CABEÇALHO DO GRUPO
                        item {
                            SectionHeader(label)
                        }

                        // LISTA DO GRUPO
                        items(grupo) { produto ->

                            val isSelected = produto.id in selectedIds

                            ProdutoItem(
                                produto = produto,
                                isSelected = isSelected,
                                onClick = {
                                    if (selectionMode) {
                                        if (isSelected) selectedIds.remove(produto.id)
                                        else selectedIds.add(produto.id)
                                        if (selectedIds.isEmpty()) selectionMode = false
                                    } else {
                                        val intent = Intent(context, CadastroProdutoActivity::class.java)
                                        intent.putExtra("produto_id", produto.id)
                                        context.startActivity(intent)
                                    }
                                },
                                onLongPress = {
                                    if (!selectionMode) {
                                        selectionMode = true
                                        selectedIds.clear()
                                    }
                                    selectedIds.add(produto.id)
                                },
                                onDoubleClick = {
                                    scope.launch {
                                        service.mudarStatus(produto, StatusProduto.TRABALHANDO_PRECO)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProdutoItem(
    produto: Produto,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    onDoubleClick: () -> Unit
) {
    var lastTapTime by remember { mutableStateOf(0L) }

    // dias para cor de fundo (tratamento: Int.MAX_VALUE = sem validade -> verde neutro)
    val dias = getDiasRestantes(produto.validadeAtual)
    val corFundo = when {
        dias == Int.MAX_VALUE -> Color(0xFFF0F0F0)
        dias <= 0 -> Color.Red.copy(alpha = 0.10f)
        dias <= 2 -> Color(0xFFFFE0E0) // urgente
        dias <= 5 -> Color.Yellow.copy(alpha = 0.12f)
        else -> Color.Green.copy(alpha = 0.08f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp, 6.dp)
            .pointerInput(produto.id) {
                detectTapGestures(
                    onLongPress = { onLongPress() },
                    onTap = {
                        val now = System.currentTimeMillis()
                        if (now - lastTapTime < 200) onDoubleClick()
                        else onClick()
                        lastTapTime = now
                    }
                )
            },
        colors = if (isSelected)
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
        else
            CardDefaults.cardColors(containerColor = corFundo)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            val context = LocalContext.current

            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, Color(0x33000000), RoundedCornerShape(10.dp))
                    .pointerInput(produto.id) {
                        detectTapGestures(
                            onTap = {
                                if (!produto.fotoUrl.isNullOrBlank()) {
                                    val intent = Intent(context, FullImageActivity::class.java)
                                    intent.putExtra("fotoUrl", produto.fotoUrl)
                                    context.startActivity(intent)
                                }
                            }
                        )
                    }
            ) {
                if (!produto.fotoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = produto.fotoUrl,
                        contentDescription = "Foto",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.align(Alignment.Center).size(40.dp)
                    )
                }
            }


            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {

                Text(produto.descricao, fontWeight = FontWeight.Bold)

                Text("Código Barras: ${produto.codigoBarras}")
                Text("Código Interno: ${produto.codigoInterno ?: "—"}")

                val total = produto.quantidadeAtual ?: 0
                val qpc = produto.quantidadePorCaixa ?: 0

                val quantidadeTexto =
                    if (qpc > 0 && total > 0) {
                        val cx = total / qpc
                        val un = total % qpc
                        "Qtd: ${cx} cx • ${un} un (cx de $qpc)"
                    } else "Qtd: ${total} un"

                Text(quantidadeTexto)

                Text("Validade: ${produto.validadeAtual ?: "—"}")
            }
        }
    }
}
