package com.hs.solutions.hstimecheck_2_0.core

import android.content.Context

object AppContainer {

    private var initialized = false

    lateinit var productService: ProductService
        private set

    var lancamentoContinuo: Boolean = false   // <<< OBRIGATÓRIO

    fun init(context: Context) {
        if (initialized) return

        val jsonRepo = ProductRepositoryJson(context)
        productService = ProductService(jsonRepo)
        FotoRepository.carregar(context)

        initialized = true
    }
}
