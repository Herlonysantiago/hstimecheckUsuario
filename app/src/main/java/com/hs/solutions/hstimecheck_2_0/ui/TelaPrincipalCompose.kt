package com.hs.solutions.hstimecheck_2_0.ui

// ================= ANDROID =================
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import java.io.File
import android.content.Context
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.launch
import android.widget.Toast

// ================= COMPOSE =================
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.rememberCoroutineScope

// ================= COIL =================
import coil.compose.AsyncImage

// ================= APP =================
import com.hs.solutions.hstimecheck_2_0.configuracoes.ConfiguracoesSistemaActivity

import com.hs.solutions.hstimecheck_2_0.R
import com.hs.solutions.hstimecheck_2_0.core.AppContainer
import com.hs.solutions.hstimecheck_2_0.core.AppPreferences
import com.hs.solutions.hstimecheck_2_0.core.ProductService
import com.hs.solutions.hstimecheck_2_0.cadastro.CadastroProdutoActivity
import com.hs.solutions.hstimecheck_2_0.aprovacao.AprovacaoComercialActivity
import com.hs.solutions.hstimecheck_2_0.models.Produto
import com.hs.solutions.hstimecheck_2_0.models.StatusProduto
import com.hs.solutions.hstimecheck_2_0.historico.HistoricoGeralActivity
import com.hs.solutions.hstimecheck_2_0.ui.FullImageActivity
// ================= CORROTINAS =================
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import android.net.Uri
import com.hs.solutions.hstimecheck_2_0.estoque.VerificacaoEstoqueActivity
import com.hs.solutions.hstimecheck_2_0.sobre.SobreActivity
import com.hs.solutions.hstimecheck_2_0.trabalhando_preco.TrabalhandoPrecoActivity
import com.hs.solutions.hstimecheck_2_0.trabalhando_preco.TrabalhandoPrecoScreen
import com.hs.solutions.hstimecheck_2_0.ui.verificacaoqualidade.VerificacaoQualidadeProdutoActivity
import com.hs.solutions.hstimecheck_2_0.vencendo.ProdutosVencendoActivity
import com.hs.solutions.hstimecheck_2_0.utils.enviarProdutos
import com.hs.solutions.hstimecheck_2_0.venda.VendaProdutoActivity
// =======================================================
// ACTIVITY PRINCIPAL (ÚNICO onCreate — CORRETO)
// =======================================================
class TelaPrincipalActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppContainer.init(this)
        val service = AppContainer.productService

        setContent {
            MaterialTheme {
                AplicarConfiguracoesGlobais()
                TelaPrincipal(service)
            }
        }
    }
}






// =======================================================
// CONFIGURAÇÕES GLOBAIS (DataStore)
// =======================================================
@Composable
fun AplicarConfiguracoesGlobais() {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        AppPreferences.read(context, AppPreferences.MODO_ONLINE, true).collect { }
    }

    LaunchedEffect(Unit) {
        AppPreferences.read(context, AppPreferences.ALERTA_VALIDADE, true).collect { }
    }

    LaunchedEffect(Unit) {
        AppPreferences.read(context, AppPreferences.ALERTA_APROVACAO, true).collect { }
    }
}


// =======================================================
// FUNÇÕES AUXILIARES (SUAS)
// =======================================================
fun getDiasRestantes(validade: String?): Int {
    if (validade.isNullOrBlank()) return Int.MAX_VALUE

    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val validadeDate = sdf.parse(validade) ?: return Int.MAX_VALUE

        val hojeCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val validadeCal = Calendar.getInstance().apply {
            time = validadeDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val diffMillis = validadeCal.timeInMillis - hojeCal.timeInMillis
        (diffMillis / (1000 * 60 * 60 * 24)).toInt()

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
        Divider(Modifier.weight(1f))
        Text(
            "  $text  ",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        Divider(Modifier.weight(1f))
    }
}

// =======================================================
// DRAWER MENU (SEU, INTEIRO)
// =======================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawerMenu(
    onDashboard: () -> Unit = {},
    onProdutos: () -> Unit = {},
    onValidadesProduto: () -> Unit = {},
    onImportacao: () -> Unit = {},
    onExportacao: () -> Unit = {},
    onAprovacao: () -> Unit = {},
    onVencidos: () -> Unit = {},
    onQueimaPreco: () -> Unit = {},
    onEstoque: () -> Unit = {},
    onVencimentos: () -> Unit = {},
    onHistorico: () -> Unit = {},
    onConfiguracoes: () -> Unit = {},
    onSobre: () -> Unit = {},
    onCreditos: () -> Unit = {}
) {
    ModalDrawerSheet(
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {

        Text("Menu", Modifier.padding(16.dp), fontWeight = FontWeight.Bold)


        NavigationDrawerItem(
            label = { Text("Dashboard") },
            icon = { Icon(Icons.Default.Home, null) },
            selected = false,
            onClick = onDashboard
        )

        SectionHeader("FLUXOS")

        NavigationDrawerItem(
            label = { Text("Aprovação Comercial") },
            icon = { Icon(Icons.Default.ThumbUp, null) },
            selected = false,
            onClick = onAprovacao
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
            label = { Text("Validades do Produto") },
            icon = { Icon(Icons.Default.DateRange, null) },
            selected = false,
            onClick = onValidadesProduto
        )

        NavigationDrawerItem(
            label = { Text("Produtos Vencendo") },
            icon = { Icon(Icons.Default.AccessTime, null) },
            selected = false,
            onClick = onVencimentos
        )
        NavigationDrawerItem(
            label = { Text("Produtos Vencidos") },
            icon = { Icon(Icons.Default.AccessTime, null) },
            selected = false,
            onClick = onVencidos
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

// =======================================================
// TELA PRINCIPAL (SUA LÓGICA COMPLETA)
// =======================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaPrincipal(service: ProductService) {

    // ---------------- STATE ----------------
    val drawerState = rememberDrawerState(
        initialValue = DrawerValue.Closed
    )
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val launcherVenda =
        rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                scope.launch {
                    service.carregar()
                }
            }
        }

    var query by remember { mutableStateOf("") }
    val produtos by service.produtos.collectAsState()

    var selectionMode by remember { mutableStateOf(false) }
    val selectedIds = remember { mutableStateListOf<String>() }

    val launcherCadastro =
        rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                scope.launch { service.carregar() }
            }
        }

    LaunchedEffect(Unit) {
        service.carregar()
    }

    // ---------------- FILTRO ----------------
    val queryNormalizada = query.trim().lowercase()

    val listaFiltrada = produtos.filter { produto ->

        if (queryNormalizada.isBlank()) return@filter true

        val descricao = produto.descricao.lowercase()
        val codigoBarras = produto.codigoBarras?.lowercase() ?: ""
        val codigoInterno = produto.codigoInterno?.lowercase() ?: ""

        descricao.contains(queryNormalizada) ||
                codigoBarras.contains(queryNormalizada) ||
                codigoInterno.contains(queryNormalizada)
    }




//    val listaFiltrada = produtos.filter {
//        query.isBlank() ||
//                it.descricao.contains(query, ignoreCase = true) ||
//                it.codigoBarras.contains(query)
//    }

    val grupos = listaFiltrada.groupBy {
        getDiasRestantes(it.validadeAtual)
    }

    val chavesOrdenadas = grupos.keys.sorted()

    // ---------------- DRAWER ----------------
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerMenu(
                onQueimaPreco = {
                    context.startActivity(
                        Intent(context, TrabalhandoPrecoActivity::class.java)
                    )
                },
                onDashboard = {
                    context.startActivity(
                        Intent(context, PainelOperacionalActivity::class.java)
                    )
                },
                onEstoque = {
                    context.startActivity(
                        Intent(context, VerificacaoEstoqueActivity::class.java)
                    )
                },
                onSobre = {
                    context.startActivity(
                        Intent(context, SobreActivity::class.java)
                    )
                },
                onVencimentos = {
                    context.startActivity(
                        Intent(context, ProdutosVencendoActivity::class.java)
                            .putExtra("modo", "VENCENDO")
                    )
                },
                onVencidos = {
                    context.startActivity(
                        Intent(context, ProdutosVencendoActivity::class.java)
                            .putExtra("modo", "VENCIDOS")
                    )
                },
                onAprovacao = {
                    context.startActivity(
                        Intent(context, AprovacaoComercialActivity::class.java)
                    )
                },
                onValidadesProduto = {
                    context.startActivity(
                        Intent(context, VerificacaoQualidadeProdutoActivity::class.java )
                    )
                },
                onConfiguracoes = {
                    context.startActivity(
                        Intent(context, ConfiguracoesSistemaActivity::class.java)
                    )
                },
                onHistorico = {
                    context.startActivity(
                        Intent(context, HistoricoGeralActivity::class.java)
                    )
                },

                )

        }
    ) {

        // ---------------- SCAFFOLD ÚNICO ----------------
        Scaffold(
            topBar = {

                if (selectionMode) {
                    TopAppBar(
                        title = { Text("${selectedIds.size} selecionado(s)") },
                        navigationIcon = {
                            IconButton(
                                onClick = {
                                    selectionMode = false
                                    selectedIds.clear()
                                }
                            ) {
                                Icon(Icons.Default.Close, null)
                            }
                        },
                        actions = {
                            IconButton(onClick = {
                                scope.launch {
                                    selectedIds.forEach { id ->
                                        produtos.find { it.id == id }?.let {
                                            service.mudarStatus(
                                                it,
                                                StatusProduto.AGUARDANDO_APROVACAO
                                            )
                                        }
                                    }
                                    selectionMode = false
                                    selectedIds.clear()
                                }
                            }) {
                                Icon(Icons.Default.ThumbUp, null)
                            }

                            IconButton(onClick = {
                                scope.launch {
                                    selectedIds.forEach { id ->
                                        produtos.find { it.id == id }?.let {
                                            service.mudarStatus(
                                                it,
                                                StatusProduto.TRABALHANDO_PRECO
                                            )
                                        }
                                    }
                                    selectionMode = false
                                    selectedIds.clear()
                                }
                            }) {
                                Icon(Icons.Default.LocalFireDepartment, null)
                            }

                            IconButton(onClick = {
                                scope.launch {
                                    val produto = produtos.firstOrNull { it.id in selectedIds }
                                        ?: return@launch

                                    service.mudarStatus(produto, StatusProduto.VERIFICACAO_ESTOQUE)

                                    selectionMode = false
                                    selectedIds.clear()

                                    context.startActivity(
                                        Intent(context, VerificacaoEstoqueActivity::class.java)
                                            .putExtra("produto_id", produto.id)
                                    )
                                }
                            }) {
                                Icon(Icons.Default.Inventory, null)
                            }

                            IconButton(onClick = {
                                val selecionados = produtos.filter { it.id in selectedIds }
                                enviarProdutos(context, selecionados)

                                selectionMode = false
                                selectedIds.clear()
                            }) {
                                Icon(Icons.Default.Send, contentDescription = "Enviar")
                            }
                        }
                    )
                } else {
                    TopAppBar(
                        title = { Text("HS TimeCheck") },
                        navigationIcon = {
                            IconButton(
                                onClick = {
                                    scope.launch { drawerState.open() }
                                }
                            ) {
                                Icon(Icons.Default.Menu, null)
                            }
                        }
                    )
                }
            },
            floatingActionButton = {
                if (!selectionMode) {
                    FloatingActionButton(
                        onClick = {
                            launcherCadastro.launch(
                                Intent(context, CadastroProdutoActivity::class.java)
                            )
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Adicionar")
                    }
                }
            }
        ) { padding ->

            // 🔴 O CONTEÚDO PRECISA ESTAR AQUI DENTRO
            Column(
                modifier = Modifier
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
                    singleLine = true,
                    placeholder = { Text("Buscar produto...") }
                )

                LazyColumn {
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

                        items(
                            items = grupo,
                            key = { it.id } // 🔴 chave estável para recomposição
                        ) { produto ->

                            val isSelected = produto.id in selectedIds

                            ProdutoItem(
                                produto = produto,
                                isSelected = isSelected,
                                onClick = {
                                    if (selectionMode) {
                                        if (isSelected) {
                                            selectedIds.remove(produto.id)
                                        } else {
                                            selectedIds.add(produto.id)
                                        }
                                        if (selectedIds.isEmpty()) {
                                            selectionMode = false
                                        }
                                    } else {
                                        context.startActivity(
                                            Intent(
                                                context,
                                                CadastroProdutoActivity::class.java
                                            ).putExtra("produto_id", produto.id)
                                        )
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
                                    // Normaliza validades
                                    produto.validades.forEach {
                                        if (it.quantidade == null) it.quantidade = 0
                                    }

                                    val validadeSelecionada = produto.validades
                                        .filter { (it.quantidade ?: 0) > 0 }
                                        .minByOrNull { it.validade }
                                        ?.validade

                                    if (validadeSelecionada == null) {
                                        Toast.makeText(
                                            context,
                                            "Produto sem estoque disponível",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        return@ProdutoItem
                                    }

                                    launcherVenda.launch(
                                        Intent(context, VendaProdutoActivity::class.java).apply {
                                            putExtra("produto_id", produto.id)
                                            putExtra("validade", validadeSelecionada)
                                        }
                                    )
                                }
                            )
                        }

                    }
                }
            }
        }
    }
}



// =======================================================
// ITEM DO PRODUTO (COMPLETO)
// =======================================================
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProdutoItem(
    produto: Produto,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    onDoubleClick: () -> Unit
) {
    val context = LocalContext.current
    var bloqueado by remember { mutableStateOf(false) }
    val dias = getDiasRestantes(produto.validadeAtual)

    val corFundo = when {
        dias == Int.MAX_VALUE -> Color(0xFFF5F5F5)
        dias < 0 -> Color.Red.copy(alpha = 0.10f)
        dias <= 2 -> Color(0xFFFFE0E0)
        dias <= 5 -> Color.Yellow.copy(alpha = 0.10f)
        else -> Color(0xFFE8F5E9)
    }

    val statusBadges = mutableListOf<Pair<String, Color>>()

    when (produto.status) {
        StatusProduto.AGUARDANDO_APROVACAO ->
            statusBadges += "Aguardando aprovação" to Color(0xFFFF9800)

        StatusProduto.TRABALHANDO_PRECO ->
            statusBadges += "Trabalhando preço" to Color(0xFFD32F2F)

        else -> {}
    }

    if (produto.emVerificacaoEstoque) {
        statusBadges += "Verificação estoque" to Color(0xFF1976D2)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 3.dp)
            .combinedClickable(
                onClick = {
                    if (!bloqueado) {
                        onClick()
                    }
                },
                onLongClick = {
                    if (!bloqueado) {
                        onLongPress()
                    }
                },
                onDoubleClick = {
                    if (bloqueado) return@combinedClickable
                    bloqueado = true

                    onDoubleClick()

                    Handler(Looper.getMainLooper()).postDelayed({
                        bloqueado = false
                    }, 600)
                }
            )
        ,
        colors = if (isSelected)
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            )
        else
            CardDefaults.cardColors(containerColor = corFundo)
    ) {


    Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // 📸 FOTO (clique simples + duplo clique)
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(
                    width = 1.dp,
                    color = Color(0x22000000),
                    shape = RoundedCornerShape(8.dp)
                )
                .clickable {
                    if (!produto.fotoUrl.isNullOrBlank()) {
                        context.startActivity(
                            Intent(context, FullImageActivity::class.java)
                                .putExtra("fotoUrl", produto.fotoUrl)
                        )
                    }
                }
        ) {
            if (!produto.fotoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = resolverImagem(produto.fotoUrl!!),
                    contentDescription = "Foto do produto",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }


            Spacer(Modifier.width(8.dp))

            Column(Modifier.weight(1f)) {

                Text(
                    produto.descricao,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1
                )

                Text(
                    "CB: ${produto.codigoBarras}",
                    style = MaterialTheme.typography.bodySmall
                )

                Text(
                    "CI: ${produto.codigoInterno ?: "—"}",
                    style = MaterialTheme.typography.bodySmall
                )



                val total = produto.quantidadeAtual ?: 0
                val qpc = produto.quantidadePorCaixa

                val estoqueTexto = when {
                    // Se o multiplicador for 1000, tratamos como conversão de Gramas para KG
                    qpc == 1000 -> {
                        val kg = total / 1000
                        val gr = total % 1000
                        if (gr > 0) "$kg kg • $gr g" else "$kg kg"
                    }

                    // Suas regras originais para caixas e unidades
                    qpc == -1 -> "$total cx"
                    qpc != null && qpc > 0 -> {
                        val cx = total / qpc
                        val un = total % qpc
                        if (un > 0) "$cx cx • $un un" else "$cx cx"
                    }
                    else -> "$total un"
                }
                Text(estoqueTexto, style = MaterialTheme.typography.bodySmall)
                Text("Validade: ${produto.validadeAtual ?: "—"}", style = MaterialTheme.typography.bodySmall)

                if (produto.status != StatusProduto.NORMAL) {
                    Text(
                        text = if(produto.status == StatusProduto.TRABALHANDO_PRECO) "Trabalhando Preço" else "Aguardando Aprovação",
                        color = Color.Red,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
    }
    }
}