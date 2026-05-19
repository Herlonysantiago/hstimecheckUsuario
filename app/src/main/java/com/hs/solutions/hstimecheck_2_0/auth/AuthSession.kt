package com.hs.solutions.hstimecheck_2_0.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

object AuthSession {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val userId: String?
        get() = currentUser?.uid

    fun requireUserId(): String {
        return userId ?: throw IllegalStateException("Usuario nao autenticado.")
    }

    fun isSignedIn(): Boolean = currentUser != null

    fun signOut() {
        auth.signOut()
    }
}
