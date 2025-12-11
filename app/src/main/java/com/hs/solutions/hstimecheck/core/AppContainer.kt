package com.hs.solutions.hstimecheck.core

import android.content.Context

object AppContainer {

    private var initialized = false

    lateinit var productService: ProductService
        private set

    fun init(context: Context) {
        if (initialized) return

        // Usando SOMENTE JSON por enquanto
        val jsonRepo = ProductRepositoryJson(context)

        productService = ProductService(jsonRepo)

        initialized = true
    }

}

