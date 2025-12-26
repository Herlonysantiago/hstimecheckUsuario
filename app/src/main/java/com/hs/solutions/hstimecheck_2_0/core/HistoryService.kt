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
    fun envioVerificacaoEstoque(produto: Produto): HistoricoItem =
        base(
            produto,
            produto.validadeAtual,
            TipoEventoHistorico.CORRECAO_ESTOQUE,
            "Envio para verificação de estoque",
            "Produto enviado para conferência de estoque"
        ) { this }

    fun envioQueimaPreco(produto: Produto): HistoricoItem =
        base(
            produto,
            produto.validadeAtual,
            TipoEventoHistorico.TRABALHANDO_PRECO,
            "Envio para queima de estoque",
            "Produto enviado para trabalhar preço"
        ) { this }

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
    // =========================
// VERIFICAÇÃO DE ESTOQUE
// =========================
    fun verificacaoEstoque(
        produto: Produto,
        quantidadeAnterior: Int,
        quantidadeNova: Int
    ): HistoricoItem =
        base(
            produto = produto,
            validade = produto.validadeAtual,
            tipo = TipoEventoHistorico.AJUSTE_ESTOQUE,
            titulo = "Verificação de estoque",
            descricao = "Conferência de estoque realizada"
        ) {
            copy(
                estoqueUnAnterior = quantidadeAnterior,
                estoqueUnAtual = quantidadeNova
            )
        }
    fun precoEmNegociacao(
        produto: Produto,
        motivo: String?
    ): HistoricoItem {

        return HistoricoItem(
            // Identificação
            produtoId = produto.id,
            codigoInterno = produto.codigoInterno,
            codigoBarras = produto.codigoBarras,
            descricaoProduto = produto.descricao,

            // Validade
            validade = produto.validadeAtual,

            // Evento
            tipoEvento = TipoEventoHistorico.PRECO_EM_NEGOCIACAO,
            titulo = "Preço em negociação",
            descricao = motivo ?: "Preço atual não atende, negociação em andamento",
            dataEvento = System.currentTimeMillis().toString(),

            // Estoque (não se aplica)
            estoqueCxAnterior = null,
            estoqueCxAtual = null,
            estoqueUnAnterior = null,
            estoqueUnAtual = null,
            estoqueTotalAnterior = null,
            estoqueTotalAtual = null,

            // Preço
            precoAnterior = produto.precoAtual,
            precoAtual = produto.precoAtual,
            precoSugerido = null,
            precoAprovado = null,

            // Auditoria
            observacao = "Solicitação registrada na tela Trabalhando Preço"
        )
    }
    fun validadeRemovida(produto: Produto): HistoricoItem =
        base(
            produto = produto,
            validade = produto.validadeAtual,
            tipo = TipoEventoHistorico.VALIDADE_REMOVIDA,
            titulo = "Validade removida",
            descricao = "Validade excluída manualmente"
        ) { this }


}
