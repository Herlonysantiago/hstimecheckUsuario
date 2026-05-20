package com.hs.solutions.hstimecheck_2_0.core

import android.content.Context
import com.hs.solutions.hstimecheck_2_0.auth.AuthSession

object AppContainer {

    private var initialized = false
    private var initializedUserId: String? = null

    lateinit var productService: ProductService
        private set

    val isInitialized: Boolean
        get() = initialized

    var lancamentoContinuo: Boolean = false

    fun init(context: Context) {
        val userId = AuthSession.dataOwnerId(context)
        if (initialized && initializedUserId == userId) return

        val jsonRepo = ProductRepositoryJson(context, userId)
        productService = ProductService(
            repo = jsonRepo,
            firebaseEnabled = AuthSession.isSignedIn()
        )
        FotoRepository.carregar(context)

        initialized = true
        initializedUserId = userId
    }

    fun reset() {
        initialized = false
        initializedUserId = null
    }
}
