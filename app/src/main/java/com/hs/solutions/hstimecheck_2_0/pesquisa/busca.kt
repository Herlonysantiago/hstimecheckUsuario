@file:OptIn(ExperimentalMaterial3Api::class)

package com.hs.solutions.hstimecheck_2_0.pesquisa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.hs.solutions.hstimecheck_2_0.core.ProductLookupService
import com.hs.solutions.hstimecheck_2_0.core.ProdutoJsonEntrada
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.clickable
import android.app.Activity
import android.content.Intent

class PesquisaProdutoActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TelaPesquisaProdutos()
        }
    }
}

@Composable
fun TelaPesquisaProdutos() {

    val context = LocalContext.current
    val activity = context as Activity
    val lookup = remember { ProductLookupService(context) }

    var query by remember { mutableStateOf("") }

    var lista by remember {
        mutableStateOf<List<ProdutoJsonEntrada>>(emptyList())
    }

    LaunchedEffect(Unit) {

        lookup.preload()

        lista = lookup.getCache()
            .filter { !it.descricao.isNullOrBlank() }
            .take(50)
    }

    LaunchedEffect(query) {

        if (query.length >= 2) {

            val cache = lookup.getCache()

            lista = cache
                .filter {
                    !it.descricao.isNullOrBlank() &&
                            (
                                    it.bar_cod?.toString()?.contains(query) == true ||
                                            it.descricao!!.contains(query, ignoreCase = true)
                                    )
                }
                .take(50)

        } else {

            lista = lookup.getCache()
                .filter { !it.descricao.isNullOrBlank() }
                .take(50)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pesquisar produto") }
            )
        }
    ) { padding ->

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
                placeholder = { Text("Código ou descrição") },
                singleLine = true
            )

            LazyColumn {

                items(lista) { produto ->

                    ListItem(
                        modifier = Modifier.clickable {

                            val intent = Intent().apply {
                                putExtra("codigo", produto.bar_cod?.toString())
                                putExtra("codigo_interno", produto.codigo?.toString())
                                putExtra("descricao", produto.descricao)
                                putExtra("complemento", produto.complemento) // ADICIONAR
                            }

                            activity.setResult(Activity.RESULT_OK, intent)
                            activity.finish()

                            activity.setResult(Activity.RESULT_OK, intent)
                            activity.finish()
                        },
                        headlineContent = {
                            Text(produto.descricao!!)
                        },
                        supportingContent = {
                            Text(
                                buildString {

                                    produto.complemento?.let {
                                        append(it)
                                    }

                                    produto.codigo?.let {

                                        if (isNotEmpty()) append(" • ")

                                        append(it.toString())
                                    }
                                }
                            )
                        }
                    )

                    Divider()
                }
            }
        }
    }
}