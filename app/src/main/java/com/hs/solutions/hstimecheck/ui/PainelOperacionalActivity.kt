/*
package com.hs.solutions.hstimecheck.ui

import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.hs.solutions.hstimecheck.core.AppContainer
import com.hs.solutions.hstimecheck.models.Produto
import com.hs.solutions.hstimecheck.models.StatusProduto
import com.hs.solutions.hstimecheck.scanner.ScannerActivity

class PainelOperacionalActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppContainer.init(this)
        val service = AppContainer.productService

        setContent {
            MaterialTheme {
                val produtos by service.produtos.collectAsState()
                PainelOperacional(this, produtos)
            }
        }
    }
}
fun diasAteValidadeFromStringB(dataStr: String?): Long? {
    if (dataStr.isNullOrBlank()) return null
    return try {
        val d = LocalDate.parse(dataStr)
        ChronoUnit.DAYS.between(LocalDate.now(), d)
    } catch (e: DateTimeParseException) {
        null
    }
}

*/
/* -------------------------------------------------------------- *//*

*/
/* NAVEGAÇÃO SIMPLES                                              *//*

*/
/* -------------------------------------------------------------- *//*

inline fun <reified T : Activity> Activity.navegarPara() {
    startActivity(Intent(this, T::class.java))
}

*/
/* -------------------------------------------------------------- *//*

*/
/* LAYOUT PRINCIPAL                                               *//*

*/
/* -------------------------------------------------------------- *//*


@Composable
fun PainelOperacional(activity: Activity, produtos: List<Produto>) {

    val total = produtos.size
    val vencendo = produtos.count {
        val d = diasAteValidadeFromStringB(it.validadeAtual)
        d != null && d in 0..15
    }
    val aguardando = produtos.count { it.status == StatusProduto.AGUARDANDO_APROVACAO }
    val trabalhando = produtos.count { it.status == StatusProduto.TRABALHANDO_PRECO }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            "Painel Operacional",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        */
/* ---------------- INDICADORES ---------------- *//*

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IndicadorPequeno("Total", total.toString(), Color(0xFF1565C0))
            IndicadorPequeno("Vencendo", vencendo.toString(), Color(0xFFE65100))
            IndicadorPequeno("Aprovação", aguardando.toString(), Color(0xFFB71C1C))
            IndicadorPequeno("Preço", trabalhando.toString(), Color(0xFF2E7D32))
        }

        Spacer(Modifier.height(24.dp))

        Text("Acessar rapidamente", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))

        Column(Modifier.fillMaxWidth()) {

            LinhaAtalho("Produtos", Icons.Default.List) {
                activity.navegarPara<TelaPrincipalActivity>()
            }

            LinhaAtalho("Scanner", Icons.Default.CameraAlt) {
                activity.navegarPara<ScannerActivity>()
            }

            LinhaAtalho("Vencendo", Icons.Default.Warning) {}

            LinhaAtalho("Aprovação Comercial", Icons.Default.Check) {}

            LinhaAtalho("Verificação de Estoque", Icons.Default.Inventory) {}
        }
    }
}

*/
/* -------------------------------------------------------------- *//*

*/
/* COMPONENTES VISUAIS                                            *//*

*/
/* -------------------------------------------------------------- *//*


@Composable
fun IndicadorPequeno(titulo: String, valor: String, cor: Color) {
    Card(
        modifier = Modifier
            .width(90.dp)
            .height(90.dp),
        colors = CardDefaults.cardColors(cor.copy(alpha = 0.15f))
    ) {
        Column(
            Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(titulo, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(valor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun LinhaAtalho(texto: String, icone: ImageVector, onClick: () -> Unit) {
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
*/
