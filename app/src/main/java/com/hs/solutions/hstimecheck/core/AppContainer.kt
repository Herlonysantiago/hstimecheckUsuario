package com.hs.solutions.hstimecheck.core

object AppContainer {
    private val repo: ProductRepository by lazy {
        InMemoryProductRepository()
    }

    val productService: ProductService by lazy {
        ProductService(repo)
    }
}

