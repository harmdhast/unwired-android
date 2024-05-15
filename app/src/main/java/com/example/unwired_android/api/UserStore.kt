package com.example.unwired_android.api

import KeystoreHelper
import android.content.Context
import android.util.Base64
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class UserStore(private val context: Context) {
    companion object {
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
        private val IV_KEY = stringPreferencesKey("iv")
        private val USER_KEY = stringPreferencesKey("user")
        private val USER_IV_KEY = stringPreferencesKey("user_iv")
        private val PASSWORD_KEY = stringPreferencesKey("password")
        private val PASSWORD_IV_KEY = stringPreferencesKey("password_iv")
    }

    fun getToken(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            val ivString = preferences[IV_KEY]
            val encryptedTokenString = preferences[TOKEN_KEY]

            if (ivString != null && encryptedTokenString != null) {
                val iv = Base64.decode(ivString, Base64.DEFAULT)
                val encryptedToken = Base64.decode(encryptedTokenString, Base64.DEFAULT)
                val decryptedToken = KeystoreHelper.decryptData(iv, encryptedToken)
                decryptedToken
            } else {
                null
            }
        }
    }

    suspend fun saveToken(token: String) {
        val (iv, encryptedToken) = KeystoreHelper.encryptData(token)
        val ivString = Base64.encodeToString(iv, Base64.DEFAULT)
        val encryptedTokenString = Base64.encodeToString(encryptedToken, Base64.DEFAULT)

        context.dataStore.edit { preferences ->
            preferences[IV_KEY] = ivString
            preferences[TOKEN_KEY] = encryptedTokenString
        }
    }

    suspend fun deleteToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
        }
    }

    suspend fun saveUserAndPassword(user: String, password: String) {
        val (ivUser, encryptedUser) = KeystoreHelper.encryptData(user)
        val ivUserString = Base64.encodeToString(ivUser, Base64.DEFAULT)
        val encryptedUserString = Base64.encodeToString(encryptedUser, Base64.DEFAULT)

        val (ivPassword, encryptedPassword) = KeystoreHelper.encryptData(password)
        val ivPasswordString = Base64.encodeToString(ivPassword, Base64.DEFAULT)
        val encryptedPasswordString = Base64.encodeToString(encryptedPassword, Base64.DEFAULT)

        context.dataStore.edit { preferences ->
            preferences[USER_KEY] = encryptedUserString
            preferences[PASSWORD_KEY] = encryptedPasswordString
            preferences[USER_IV_KEY] = ivUserString
            preferences[PASSWORD_IV_KEY] = ivPasswordString
        }
    }

    fun getUserAndPassword(): Flow<Pair<String?, String?>> {
        return context.dataStore.data.map { preferences ->
            val ivUserString = preferences[USER_IV_KEY]
            val encryptedUserString = preferences[USER_KEY]
            val ivPasswordString = preferences[PASSWORD_IV_KEY]
            val encryptedPasswordString = preferences[PASSWORD_KEY]

            val user = if (ivUserString != null && encryptedUserString != null) {
                val ivUser = Base64.decode(ivUserString, Base64.DEFAULT)
                val encryptedUser = Base64.decode(encryptedUserString, Base64.DEFAULT)
                KeystoreHelper.decryptData(ivUser, encryptedUser)
            } else {
                null
            }

            val password = if (ivPasswordString != null && encryptedPasswordString != null) {
                val ivPassword = Base64.decode(ivPasswordString, Base64.DEFAULT)
                val encryptedPassword = Base64.decode(encryptedPasswordString, Base64.DEFAULT)
                KeystoreHelper.decryptData(ivPassword, encryptedPassword)
            } else {
                null
            }

            Pair(user, password)
        }
    }
}