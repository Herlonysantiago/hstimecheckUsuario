package com.hs.solutions.hstimecheck_2_0.auth

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

object AuthSession {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private const val PREFS_NAME = "auth_session"
    private const val KEY_OFFLINE_MODE = "offline_mode"
    private const val OFFLINE_USER_ID = "offline_local"

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val userId: String?
        get() = currentUser?.uid

    fun requireUserId(): String {
        return userId ?: throw IllegalStateException("Usuario nao autenticado.")
    }

    fun isSignedIn(): Boolean = currentUser != null

    fun isOfflineMode(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_OFFLINE_MODE, false)
    }

    fun hasActiveSession(context: Context): Boolean {
        return isSignedIn() || isOfflineMode(context)
    }

    fun enableOfflineMode(context: Context) {
        auth.signOut()
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_OFFLINE_MODE, true)
            .apply()
    }

    fun disableOfflineMode(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_OFFLINE_MODE, false)
            .apply()
    }

    fun dataOwnerId(context: Context): String {
        return userId ?: if (isOfflineMode(context)) {
            OFFLINE_USER_ID
        } else {
            throw IllegalStateException("Usuario nao autenticado.")
        }
    }

    fun safeDataOwnerId(context: Context): String {
        return dataOwnerId(context).replace(Regex("[^A-Za-z0-9_-]"), "_")
    }

    fun signOut() {
        auth.signOut()
    }

    fun clearSession(context: Context) {
        signOut()
        disableOfflineMode(context)
    }
}
