package com.hs.solutions.hstimecheck_2_0.ui.verificacaoqualidade

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hs.solutions.hstimecheck_2_0.core.AppContainer

class VerificacaoQualidadeProdutoViewModelFactory : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VerificacaoQualidadeProdutoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VerificacaoQualidadeProdutoViewModel(
                AppContainer.productService
            ) as T
        }
        throw IllegalArgumentException("ViewModel desconhecido")
    }
}
