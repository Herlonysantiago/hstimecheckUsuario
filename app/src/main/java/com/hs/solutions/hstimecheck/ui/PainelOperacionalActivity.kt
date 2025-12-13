package com.hs.solutions.hstimecheck.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hs.solutions.hstimecheck.aprovacao.AprovacaoComercialActivity
import com.hs.solutions.hstimecheck.core.AppContainer
import com.hs.solutions.hstimecheck.models.*
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit

class PainelOperacionalActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppContainer.init(this)
        val service = AppContainer.productService

        setContent {
            MaterialTheme {
                val produtos by service.produtos.collectAsState()
                PainelOperacional(produtos)
            }
        }
    }
}

// ---------------- FUNÇÕES AUX ----------------

fun diasAteValidade(dataStr: String?): Long? {
    if (dataStr.isNullOrBlank()) return null
    return try {
        val d = LocalDate.parse(dataStr)
        ChronoUnit.DAYS.between(LocalDate.now(), d)
    } catch (_: DateTimeParseException) {
        null
    }
}

// ---------------- DASHBOARD ----------------

@Composable
fun PainelOperacional(produtos: List<Produto>) {

    val context = LocalContext.current

    val ate15 = produtos.count {
        val d = diasAteValidade(it.validadeAtual)
        d != null && d in 0..15
    }

    val semana = produtos.count {
        val d = diasAteValidade(it.validadeAtual)
        d != null && d in 0..7
    }

    val queimando = produtos.count {
        it.status == StatusProduto.TRABALHANDO_PRECO
    }

    val aguardando = produtos.count {
        it.status == StatusProduto.AGUARDANDO_APROVACAO
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text("Painel Operacional", fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))

        // ----------- CARDS INDICADORES -----------

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IndicadorGrande(
                "Até 15 dias",
                ate15.toString(),
                Color(0xFFE65100),
                Modifier.weight(1f)
            ) {
                abrirListaFiltrada(context, FiltroLista.ATE_15_DIAS)
            }

            IndicadorGrande(
                "Essa semana",
                semana.toString(),
                Color(0xFFD32F2F),
                Modifier.weight(1f)
            ) {
                abrirListaFiltrada(context, FiltroLista.ESSA_SEMANA)
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IndicadorGrande(
                "Queima de estoque",
                queimando.toString(),
                Color(0xFF2E7D32),
                Modifier.weight(1f)
            ) {
                abrirListaFiltrada(context, FiltroLista.QUEIMANDO_ESTOQUE)
            }

            IndicadorGrande(
                "Aguardando aprovação",
                aguardando.toString(),
                Color(0xFFB71C1C),
                Modifier.weight(1f)
            ) {
                abrirListaFiltrada(context, FiltroLista.AGUARDANDO_APROVACAO)
            }
        }

        // ----------- BOTÕES INFERIORES -----------

        Spacer(Modifier.height(32.dp))

        Text(
            text = "Acessar rapidamente",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(12.dp))

        LinhaAtalho("Produtos", Icons.Default.List) {
            context.startActivity(
                Intent(context, TelaPrincipalActivity::class.java)
            )
        }

        LinhaAtalho("Aprovação Comercial", Icons.Default.ThumbUp) {
            context.startActivity(
                Intent(context, AprovacaoComercialActivity::class.java)
            )
        }

        LinhaAtalho("Queima de Estoque", Icons.Default.LocalFireDepartment) {
            abrirListaFiltrada(context, FiltroLista.QUEIMANDO_ESTOQUE)
        }

        LinhaAtalho("Produtos Vencendo", Icons.Default.AccessTime) {
            abrirListaFiltrada(context, FiltroLista.ESSA_SEMANA)
        }
    }
}

// ---------------- NAVEGAÇÃO ----------------

fun abrirListaFiltrada(
    context: android.content.Context,
    filtro: FiltroLista
) {
    val intent = Intent(context, TelaListaFiltradaActivity::class.java)
    intent.putExtra("filtro", filtro.name)
    context.startActivity(intent)
}

// ---------------- COMPONENTES ----------------

@Composable
fun IndicadorGrande(
    titulo: String,
    valor: String,
    cor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(110.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = cor.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(titulo, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(6.dp))
            Text(valor, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun LinhaAtalho(
    texto: String,
    icone: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icone, contentDescription = null, tint = Color(0xFF1976D2))
            Spacer(Modifier.width(12.dp))
            Text(texto, fontSize = 18.sp)
        }
    }
}
