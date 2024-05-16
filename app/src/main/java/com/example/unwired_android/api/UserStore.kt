/**
 * This file contains the UserStore class which is responsible for managing user data.
 *
 * The UserStore class provides methods for getting and setting user data, such as the user's token, username, and password.
 * The data is stored in the Android DataStore and is encrypted using the KeystoreHelper class before being stored.
 * The encrypted data is then Base64 encoded before being stored in the DataStore.
 * The data is decrypted and Base64 decoded when it is retrieved from the DataStore.
 *
 * The UserStore class uses the following keys to store data in the DataStore:
 * - TOKEN_KEY: The key for the user's token.
 * - IV_KEY: The key for the initialization vector used to encrypt the token.
 * - USER_KEY: The key for the username.
 * - USER_IV_KEY: The key for the initialization vector used to encrypt the username.
 * - PASSWORD_KEY: The key for the password.
 * - PASSWORD_IV_KEY: The key for the initialization vector used to encrypt the password.
 */

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

    /**
     * Retrieves the user's token from the DataStore.
     *
     * The token is Base64 decoded and then decrypted using the KeystoreHelper class before being returned.
     *
     * @return A Flow that emits the user's token, or null if the token is not set.
     */
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

    /**
     * Retrieves the user's token from the DataStore.
     *
     * The token is Base64 decoded and then decrypted using the KeystoreHelper class before being returned.
     *
     * @return A Flow that emits the user's token, or null if the token is not set.
     */
    suspend fun saveToken(token: String) {
        val (iv, encryptedToken) = KeystoreHelper.encryptData(token)
        val ivString = Base64.encodeToString(iv, Base64.DEFAULT)
        val encryptedTokenString = Base64.encodeToString(encryptedToken, Base64.DEFAULT)

        context.dataStore.edit { preferences ->
            preferences[IV_KEY] = ivString
            preferences[TOKEN_KEY] = encryptedTokenString
        }
    }

    /**
     * Deletes the user's token, username, and password from the DataStore.
     */
    suspend fun deleteToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
            preferences.remove(IV_KEY)
            preferences.remove(USER_KEY)
            preferences.remove(USER_IV_KEY)
            preferences.remove(PASSWORD_KEY)
            preferences.remove(PASSWORD_IV_KEY)
        }
    }

    /**
     * Stores the user's username and password in the DataStore.
     *
     * The username and password are encrypted using the KeystoreHelper class and then Base64 encoded before being stored.
     *
     * @param user The user's username.
     * @param password The user's password.
     */
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

    /**
     * Retrieves the user's username and password from the DataStore.
     *
     * The username and password are Base64 decoded and then decrypted using the KeystoreHelper class before being returned.
     *
     * @return A Flow that emits a Pair containing the user's username and password, or null if they are not set.
     */
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