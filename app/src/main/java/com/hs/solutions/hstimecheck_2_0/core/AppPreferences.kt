package com.hs.solutions.hstimecheck_2_0.core

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore(name = "hs_timecheck_prefs")

object AppPreferences {

    // CHAVES
    val MODO_ONLINE = booleanPreferencesKey("modo_online")
    val ALERTA_VALIDADE = booleanPreferencesKey("alerta_validade")
    val ALERTA_APROVACAO = booleanPreferencesKey("alerta_aprovacao")
    val FOTO_OBRIGATORIA = booleanPreferencesKey("foto_obrigatoria")
    val VALIDADE_OBRIGATORIA = booleanPreferencesKey("validade_obrigatoria")
    val BLOQUEAR_SEM_APROVACAO = booleanPreferencesKey("bloquear_sem_aprovacao")

    // SALVAR
    suspend fun save(context: Context, key: Preferences.Key<Boolean>, value: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[key] = value
        }
    }
    suspend fun readOnce(
        context: Context,
        key: Preferences.Key<Boolean>,
        default: Boolean
    ): Boolean {
        return read(context, key, default).first()
    }

    // LER
    fun read(context: Context, key: Preferences.Key<Boolean>, default: Boolean): Flow<Boolean> {
        return context.dataStore.data.map { prefs ->
            prefs[key] ?: default
        }
    }
}
