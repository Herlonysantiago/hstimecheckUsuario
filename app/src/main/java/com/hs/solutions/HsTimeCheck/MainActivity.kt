package com.hs.solutions.Hstimecheck

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hs.solutions.Hstimecheck.adapters.ProdutoCardAdapter
import com.hs.solutions.Hstimecheck.adapters.ProdutoListAdapter
import com.hs.solutions.Hstimecheck.bottomsheet.DeleteBottomSheet
import com.hs.solutions.Hstimecheck.models.Produto
import com.hs.solutions.Hstimecheck.models.StatusProduto
import com.hs.solutions.Hstimecheck.utils.RecyclerItemClickListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var rv: RecyclerView
    private lateinit var adapterList: ProdutoListAdapter
    private lateinit var adapterCard: ProdutoCardAdapter
    private val produtos: MutableList<Produto> = mutableListOf()

    private var currentTouchListener: RecyclerView.OnItemTouchListener? = null

    private val PREFS = "hs_prefs"
    private val PREF_VIEW_MODE = "view_mode" // "AUTO", "LIST", "CARD"

    // Menu contextual (AlertDialog) com opções diferentes por status
    private fun openContextMenu(produto: Produto) {
        val options: Array<String> = when (produto.status) {
            StatusProduto.NORMAL -> arrayOf("Enviar para aprovação", "Enviar para rebaixa", "Enviar para verificação de estoque", "Ver histórico", "Ver validades")
            StatusProduto.AGUARDANDO_APROVACAO -> arrayOf("Aprovar", "Rejeitar", "Sugerir preço", "Ver histórico", "Ver validades")
            StatusProduto.TRABALHANDO_PRECO -> arrayOf("Atualizar preço", "Atualizar quantidade", "Finalizar produto", "Ver histórico", "Ver validades")
            StatusProduto.VERIFICACAO_ESTOQUE -> arrayOf("Atualizar quantidade física", "Resolver divergência", "Remover da lista", "Ver histórico", "Ver validades")
            StatusProduto.VENCENDO -> arrayOf("Enviar para aprovação", "Enviar para rebaixa", "Ver histórico", "Ver validades")
        }

        AlertDialog.Builder(this)
            .setTitle(produto.descricao)
            .setItems(options) { dialog, which ->
                when (options[which]) {
                    "Enviar para aprovação" -> {
                        produto.status = StatusProduto.AGUARDANDO_APROVACAO
                        produto.historico.add(createHistorico("Enviado para aprovação", null, produto.quantidadeAtual, produto.precoAtual))
                        atualizarAdapters()
                        Toast.makeText(this, "Enviado para aprovação", Toast.LENGTH_SHORT).show()
                    }
                    "Enviar para rebaixa" -> {
                        produto.status = StatusProduto.TRABALHANDO_PRECO
                        produto.historico.add(createHistorico("Enviado para rebaixa", null, produto.quantidadeAtual, produto.precoAtual))
                        atualizarAdapters()
                        Toast.makeText(this, "Enviado para rebaixa", Toast.LENGTH_SHORT).show()
                    }
                    "Enviar para verificação de estoque" -> {
                        produto.status = StatusProduto.VERIFICACAO_ESTOQUE
                        produto.historico.add(createHistorico("Enviado para verificação de estoque", null, produto.quantidadeAtual, produto.precoAtual))
                        atualizarAdapters()
                        Toast.makeText(this, "Enviado para verificação", Toast.LENGTH_SHORT).show()
                    }
                    "Aprovar" -> {
                        // Pergunta se preço sugerido foi aprovado — aqui abrimos um diálogo simples
                        askIfSuggestedPriceApproved(produto)
                    }
                    "Rejeitar" -> {
                        produto.status = StatusProduto.NORMAL
                        produto.historico.add(createHistorico("Rejeitado pelo comercial", null, produto.quantidadeAtual, produto.precoAtual))
                        atualizarAdapters()
                        Toast.makeText(this, "Produto rejeitado e retornou ao normal", Toast.LENGTH_SHORT).show()
                    }
                    "Sugerir preço" -> {
                        askForSuggestedPrice(produto)
                    }
                    "Atualizar preço" -> {
                        askForNewPriceAndSave(produto)
                    }
                    "Atualizar quantidade" -> {
                        askForNewQuantity(produto)
                    }
                    "Finalizar produto" -> {
                        produto.historico.add(createHistorico("Produto finalizado (estoque zerado)", null, produto.quantidadeAtual, produto.precoAtual))
                        produto.status = StatusProduto.NORMAL
                        produto.quantidadeAtual = 0
                        atualizarAdapters()
                        Toast.makeText(this, "Produto finalizado", Toast.LENGTH_SHORT).show()
                    }
                    "Atualizar quantidade física" -> {
                        askForNewQuantity(produto)
                    }
                    "Resolver divergência" -> {
                        produto.historico.add(createHistorico("Divergência marcada como resolvida", null, produto.quantidadeAtual, produto.precoAtual))
                        atualizarAdapters()
                        Toast.makeText(this, "Divergência resolvida", Toast.LENGTH_SHORT).show()
                    }
                    "Remover da lista" -> {
                        removerProdutoDaListaAtual(produto)
                        Toast.makeText(this, "Removido da lista atual", Toast.LENGTH_SHORT).show()
                    }
                    "Ver histórico" -> {
                        openProdutoHistorico(produto)
                    }
                    "Ver validades" -> {
                        openProdutoValidades(produto)
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton("Fechar", null)
            .show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // views
        rv = findViewById(R.id.recyclerProdutos)
        val btnToggle = findViewById<ImageButton>(R.id.btnToggleView)
        val fab = findViewById<FloatingActionButton>(R.id.fabScanner)
        val search = findViewById<SearchView>(R.id.searchView)

        // adapters
        adapterList = ProdutoListAdapter(produtos)
        adapterCard = ProdutoCardAdapter(produtos)

        // carregar produtos (substitua por persistência real)
        loadSampleProducts()

        // aplicar view mode salvo
        val prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val mode = prefs.getString(PREF_VIEW_MODE, "AUTO") ?: "AUTO"
        applyViewMode(mode)

        // alterna entre lista/card
        btnToggle.setOnClickListener {
            val current = prefs.getString(PREF_VIEW_MODE, "AUTO") ?: "AUTO"
            val next = if (current == "LIST") "CARD" else "LIST"
            prefs.edit().putString(PREF_VIEW_MODE, next).apply()
            applyViewMode(next)
        }

        // FAB -> abrir scanner (implemente ScannerActivity e descomente)
        fab.setOnClickListener {
            openScanner()
        }

        // busca em tempo real
        search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText ?: "")
                return true
            }
        })
    }

    // Aplica o modo de visualização: LIST / CARD / AUTO
    private fun applyViewMode(mode: String) {
        when (mode) {
            "CARD" -> {
                val span = calculateSpanCount()
                rv.layoutManager = GridLayoutManager(this, span)
                rv.adapter = adapterCard
            }
            "LIST" -> {
                rv.layoutManager = LinearLayoutManager(this)
                rv.adapter = adapterList
            }
            else -> { // AUTO
                val span = calculateSpanCount()
                if (span >= 3) {
                    rv.layoutManager = GridLayoutManager(this, span)
                    rv.adapter = adapterCard
                } else {
                    rv.layoutManager = LinearLayoutManager(this)
                    rv.adapter = adapterList
                }
            }
        }
        attachItemTouchListener()
    }

    // Decide colunas automáticas
    private fun calculateSpanCount(): Int {
        val displayMetrics = resources.displayMetrics
        val dpWidth = displayMetrics.widthPixels / displayMetrics.density
        return if (dpWidth > 600) 3 else 2
    }

    // Remove listener anterior, adiciona novo listener que trata click/long/double
    private fun attachItemTouchListener() {
        currentTouchListener?.let {
            try {
                rv.removeOnItemTouchListener(it)
            } catch (ex: Exception) { /* ignore */ }
            currentTouchListener = null
        }

        val listener = RecyclerItemClickListener(this, rv, object : RecyclerItemClickListener.Callback {
            override fun onClick(view: View, position: Int) {
                val produto = getAdapterItem(position) ?: return
                openProdutoDetalhe(produto)
            }

            override fun onLongClick(view: View, position: Int) {
                val produto = getAdapterItem(position) ?: return
                openContextMenu(produto)
            }

            override fun onDoubleClick(view: View, position: Int) {
                val produto = getAdapterItem(position) ?: return
                openDeleteBottomSheet(produto)
            }
        })

        rv.addOnItemTouchListener(listener)
        currentTouchListener = listener
    }

    // Recupera item independente do adapter (lista ou card)
    private fun getAdapterItem(position: Int): Produto? {
        val adapter = rv.adapter ?: return null
        return when (adapter) {
            is ProdutoListAdapter -> adapter.getItem(position)
            is ProdutoCardAdapter -> adapter.getItem(position)
            else -> null
        }
    }

    // Abre a Activity de detalhes (implemente ProductDetailActivity depois)
    private fun openProdutoDetalhe(produto: Produto) {
        // Exemplo: abrir Activity com EXTRA produto.id
        // val intent = Intent(this, ProdutoDetalheActivity::class.java)
        // intent.putExtra("produto_id", produto.id)
        // startActivity(intent)

        // Por enquanto, só toast para teste
        Toast.makeText(this, "Abrir detalhe: ${produto.descricao}", Toast.LENGTH_SHORT).show()
    }

    // Abre bottom sheet de exclusão (click duplo)
    private fun openDeleteBottomSheet(produto: Produto) {
        val sheet = DeleteBottomSheet(this) { action ->
            when (action) {
                DeleteBottomSheet.Action.REMOVE_LIST -> {
                    removerProdutoDaListaAtual(produto)
                    produto.historico.add(createHistorico("Removido da lista", null, produto.quantidadeAtual, produto.precoAtual))
                    atualizarAdapters()
                    Toast.makeText(this, "Removido da lista", Toast.LENGTH_SHORT).show()
                }
                DeleteBottomSheet.Action.RETURN_NORMAL -> {
                    produto.status = StatusProduto.NORMAL
                    produto.historico.add(createHistorico("Retornado ao normal", null, produto.quantidadeAtual, produto.precoAtual))
                    atualizarAdapters()
                    Toast.makeText(this, "Produto retornado ao normal", Toast.LENGTH_SHORT).show()
                }
                DeleteBottomSheet.Action.DELETE_ALL -> {
                    produtos.remove(produto)
                    // registra evento antes de remover (poderia salvar em log)
                    // produto.historico.add(createHistorico("Excluído permanentemente", null, produto.quantidadeAtual, produto.precoAtual))
                    atualizarAdapters()
                    Toast.makeText(this, "Produto excluído permanentemente", Toast.LENGTH_SHORT).show()
                }
            }
        }
        sheet.show()
    }

    // Remove produto da lista atual (filtro) - aqui apenas remove do dataset visível; adapte conforme fluxo/ tela atual
    private fun removerProdutoDaListaAtual(produto: Produto) {
        // se você tem um filtro ativo, remova apenas dessa coleção de filtro
        // por enquanto removemos do dataset global
        produtos.remove(produto)
        atualizarAdapters()
    }

    // Abre tela de histórico (implemente Activity de histórico)
    private fun openProdutoHistorico(produto: Produto) {
        // Exemplo toast
        Toast.makeText(this, "Abrir histórico de ${produto.descricao}", Toast.LENGTH_SHORT).show()
        // Implementar: abrir Activity passando produto.id
    }

    // Abre tela de validades (implemente Activity de validades)
    private fun openProdutoValidades(produto: Produto) {
        Toast.makeText(this, "Abrir validades de ${produto.descricao}", Toast.LENGTH_SHORT).show()
    }

    // Funções auxiliares para diálogos simples (ex.: pedir novo preço / quantidade / sugestão)

    private fun askIfSuggestedPriceApproved(produto: Produto) {
        AlertDialog.Builder(this)
            .setTitle("Preço sugerido aprovado?")
            .setMessage("O preço sugerido foi aprovado e deve ser aplicado?")
            .setPositiveButton("Sim") { _, _ ->
                // aplica preco sugerido se houver (neste exemplo, usamos precoAtual como sugerido)
                produto.status = StatusProduto.TRABALHANDO_PRECO
                produto.historico.add(createHistorico("Aprovado pelo comercial", "Preço aprovado: ${produto.precoAtual}", produto.quantidadeAtual, produto.precoAtual))
                atualizarAdapters()
                Toast.makeText(this, "Produto aprovado e enviado para queima", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Não") { _, _ ->
                // pede novo preço
                askForPriceThenApprove(produto)
            }
            .show()
    }

    private fun askForPriceThenApprove(produto: Produto) {
        val edit = android.widget.EditText(this)
        edit.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        AlertDialog.Builder(this)
            .setTitle("Informe o preço aprovado")
            .setView(edit)
            .setPositiveButton("Confirmar") { _, _ ->
                val txt = edit.text.toString()
                val novo = txt.toDoubleOrNull()
                if (novo != null) {
                    produto.precoAtual = novo
                    produto.status = StatusProduto.TRABALHANDO_PRECO
                    produto.historico.add(createHistorico("Aprovado pelo comercial", "Preço final aprovado: $novo", produto.quantidadeAtual, novo))
                    atualizarAdapters()
                    Toast.makeText(this, "Preço aprovado e produto enviado para queima", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Preço inválido", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun askForSuggestedPrice(produto: Produto) {
        val edit = android.widget.EditText(this)
        edit.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        AlertDialog.Builder(this)
            .setTitle("Sugerir preço para aprovação")
            .setView(edit)
            .setPositiveButton("Enviar") { _, _ ->
                val txt = edit.text.toString()
                val sugerido = txt.toDoubleOrNull()
                produto.historico.add(createHistorico("Sugestão enviada para aprovação", "Sugestão: $txt", produto.quantidadeAtual, sugerido))
                produto.status = StatusProduto.AGUARDANDO_APROVACAO
                atualizarAdapters()
                Toast.makeText(this, "Sugestão enviada", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun askForNewPriceAndSave(produto: Produto) {
        val edit = android.widget.EditText(this)
        edit.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        AlertDialog.Builder(this)
            .setTitle("Informe novo preço")
            .setView(edit)
            .setPositiveButton("Salvar") { _, _ ->
                val txt = edit.text.toString()
                val novo = txt.toDoubleOrNull()
                if (novo != null) {
                    val detalhe = "Preço alterado de ${produto.precoAtual ?: "—"} para $novo"
                    produto.precoAtual = novo
                    produto.historico.add(createHistorico("Preço alterado", detalhe, produto.quantidadeAtual, novo))
                    atualizarAdapters()
                    Toast.makeText(this, "Preço atualizado", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Preço inválido", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun askForNewQuantity(produto: Produto) {
        val edit = android.widget.EditText(this)
        edit.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        AlertDialog.Builder(this)
            .setTitle("Informe nova quantidade")
            .setView(edit)
            .setPositiveButton("Salvar") { _, _ ->
                val txt = edit.text.toString()
                val novo = txt.toIntOrNull()
                if (novo != null) {
                    val detalhe = "Estoque alterado de ${produto.quantidadeAtual ?: "—"} para $novo"
                    produto.quantidadeAtual = novo
                    produto.historico.add(createHistorico("Estoque atualizado", detalhe, novo, produto.precoAtual))
                    atualizarAdapters()
                    Toast.makeText(this, "Quantidade atualizada", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Quantidade inválida", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // Simples helper para criar um HistoricoItem (utilize o modelo que você tiver)
    private fun createHistorico(evento: String, detalhe: String?, quantidade: Int?, preco: Double?): com.hs.solutions.Hstimecheck.models.HistoricoItem {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return com.hs.solutions.Hstimecheck.models.HistoricoItem(
            dataEvento = sdf.format(Date()),
            evento = evento,
            detalhe = detalhe,
            quantidade = quantidade,
            preco = preco
        )
    }

    // Filtro de busca simples
    private fun filterList(query: String) {
        val q = query.lowercase(Locale.getDefault())
        val filtered = produtos.filter {
            it.codigoBarras.lowercase(Locale.getDefault()).contains(q) ||
                    (it.codigoInterno?.lowercase(Locale.getDefault())?.contains(q) ?: false) ||
                    it.descricao.lowercase(Locale.getDefault()).contains(q)
        }
        adapterList.update(filtered)
        adapterCard.update(filtered)
    }

    // Atualiza adapters (após mudanças)
    private fun atualizarAdapters() {
        adapterList.update(produtos)
        adapterCard.update(produtos)
    }

    // Abre ScannerActivity (implemente a Activity e descomente se quiser)
    private fun openScanner() {
        // Exemplo placeholder
        Toast.makeText(this, "Abrir Scanner (implementar ScannerActivity)", Toast.LENGTH_SHORT).show()
        // val intent = Intent(this, ScannerActivity::class.java)
        // startActivityForResult(intent, REQUEST_SCAN)
    }

    // Carrega produtos de exemplo (substitua por sua persistência)
    private fun loadSampleProducts() {
        produtos.clear()
        produtos.add(
            Produto(
                codigoBarras = "7891234567890",
                descricao = "Leite Integral 1L",
                validadeAtual = "2025-03-18",
                quantidadeAtual = 50,
                precoAtual = 1.50,
                status = StatusProduto.TRABALHANDO_PRECO
            )
        )
        produtos.add(
            Produto(
                codigoBarras = "2034567012345",
                codigoInterno = "34567",
                descricao = "Carne Moída",
                validadeAtual = "2025-03-12",
                quantidadeAtual = 100,
                precoAtual = 21.90,
                status = StatusProduto.VENCENDO
            )
        )
        produtos.add(
            Produto(
                codigoBarras = "7891119990001",
                descricao = "Refrigerante 2L",
                quantidadeAtual = 12,
                status = StatusProduto.VERIFICACAO_ESTOQUE
            )
        )
        atualizarAdapters()
    }


}
