package com.hs.solutions.hstimecheck_2_0.historico

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hs.solutions.hstimecheck_2_0.core.ProductService

class HistoricoViewModelFactory(
    private val productService: ProductService
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoricoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoricoViewModel(productService) as T
        }
        throw IllegalArgumentException("ViewModel desconhecido")
    }

}
