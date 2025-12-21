package com.hs.solutions.hstimecheck_2_0.core

import com.hs.solutions.hstimecheck_2_0.models.*
import java.text.SimpleDateFormat
import java.util.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object HistoryService {

    private val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())


    private fun agora(): String {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        return LocalDateTime.now().format(formatter)
    }

    // =========================
    // BASE
    // =========================
    private fun base(
        produto: Produto,
        validade: String?,
        tipo: TipoEventoHistorico,
        titulo: String,
        descricao: String,
        bloco: HistoricoItem.() -> HistoricoItem
    ): HistoricoItem {
        return HistoricoItem(
            produtoId = produto.id,
            codigoInterno = produto.codigoInterno,
            codigoBarras = produto.codigoBarras,
            descricaoProduto = produto.descricao,
            validade = validade,
            tipoEvento = tipo,
            titulo = titulo,
            descricao = descricao,
            dataEvento = agora()
        ).bloco()
    }

    // =========================
    // CADASTRO
    // =========================
    fun cadastro(produto: Produto): HistoricoItem =
        base(
            produto,
            validade = produto.validadeAtual,
            tipo = TipoEventoHistorico.CADASTRO_PRODUTO,
            titulo = "Cadastro do produto",
            descricao = "Produto cadastrado no sistema"
        ) { this }

    // =========================
    // AJUSTE DE ESTOQUE
    // =========================
    fun ajusteEstoque(
        produto: Produto,
        validade: String?,
        cxAntes: Int?,
        cxDepois: Int?,
        unAntes: Int?,
        unDepois: Int?
    ): HistoricoItem =
        base(
            produto,
            validade,
            TipoEventoHistorico.AJUSTE_ESTOQUE,
            "Ajuste de estoque",
            "Estoque ajustado manualmente"
        ) {
            copy(
                estoqueCxAnterior = cxAntes,
                estoqueCxAtual = cxDepois,
                estoqueUnAnterior = unAntes,
                estoqueUnAtual = unDepois
            )
        }

    // =========================
    // VENDA
    // =========================
    fun venda(
        produto: Produto,
        validade: String?,
        estoqueUnAntes: Int,
        estoqueUnDepois: Int
    ): HistoricoItem =
        base(
            produto,
            validade,
            TipoEventoHistorico.VENDA,
            "Venda registrada",
            "Venda realizada"
        ) {
            copy(
                estoqueUnAnterior = estoqueUnAntes,
                estoqueUnAtual = estoqueUnDepois
            )
        }


    // =========================
    // ENVIO PARA APROVAÇÃO
    // =========================
    fun envioAprovacao(
        produto: Produto,
        validade: String?,
        precoAtual: Double,
        precoSugerido: Double
    ): HistoricoItem =
        base(
            produto,
            validade,
            TipoEventoHistorico.ENVIO_APROVACAO_COMERCIAL,
            "Envio para aprovação comercial",
            "Produto enviado para aprovação de preço"
        ) {
            copy(
                precoAnterior = precoAtual,
                precoSugerido = precoSugerido
            )
        }

    // =========================
    // APROVAÇÃO
    // =========================
    fun aprovacao(
        produto: Produto,
        precoSugerido: Double,
        precoAprovado: Double
    ): HistoricoItem {

        val precoAnterior = produto.precoAtual ?: 0.0

        return HistoricoItem(
            produtoId = produto.id,
            descricaoProduto = produto.descricao,
            codigoInterno = produto.codigoInterno,
            codigoBarras = produto.codigoBarras,
            validade = produto.validadeAtual,
            tipoEvento = TipoEventoHistorico.APROVACAO_COMERCIAL,
            titulo = "Aprovação comercial",
            descricao = buildString {
                append("Preço anterior: R$ ")
                append(String.format("%.2f", precoAnterior))
                append("\nPreço sugerido: R$ ")
                append(String.format("%.2f", precoSugerido))
                append("\nPreço aprovado: R$ ")
                append(String.format("%.2f", precoAprovado))
            },
            dataEvento = agora()
        )
    }



    // =========================
    // REJEIÇÃO
    // =========================
    fun rejeicao(
        produto: Produto,
        validade: String?,
        motivo: String?
    ): HistoricoItem =
        base(
            produto,
            validade,
            TipoEventoHistorico.REJEICAO_COMERCIAL,
            "Rejeição comercial",
            motivo ?: "Preço não autorizado"
        ) { this }

    // =========================
    // EXCLUSÃO
    // =========================
    fun exclusao(produto: Produto): HistoricoItem =
        base(
            produto,
            produto.validadeAtual,
            TipoEventoHistorico.EXCLUSAO_PRODUTO,
            "Produto excluído",
            "Produto removido do sistema"
        ) { this }
}
