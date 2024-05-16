package com.example.unwired_android.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unwired_android.api.UnwiredAPI
import com.example.unwired_android.api.UserStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject


@HiltViewModel
class LoginViewModel @Inject constructor(
    private val unwiredAPI: UnwiredAPI,
    private val userStore: UserStore
) : ViewModel() {
    // LiveData objects for observing the results of the login and registration operations.
    private val _loginResult = MutableLiveData<Boolean>()
    val loginResult: LiveData<Boolean> = _loginResult
    private val _registerResult = MutableLiveData<Boolean>()
    val registerResult: LiveData<Boolean> = _registerResult

    // LiveData object for observing the error message of the registration operation.
    private val _registerErr = MutableLiveData<String>()
    val registerErr: LiveData<String> = _registerErr

    /**
     * Performs the login operation.
     *
     * This method takes a username and a password as arguments, makes an API call to login with the given username and password,
     * and updates the loginResult LiveData object with the result of the operation.
     *
     * @param username The username to login with.
     * @param password The password to login with.
     */
    fun login(username: String, password: String) {
        viewModelScope.launch {
            try {
                val success = unwiredAPI.login(username, password)
                if (success.isSuccessful) {
                    userStore.saveUserAndPassword(username, password)
                    userStore.saveToken(success.body()?.access_token.toString())
                }
                _loginResult.postValue(success.isSuccessful)
            } catch (e: Exception) {
                // Handle error
                _loginResult.postValue(false)
            }
        }
    }

    /**
     * Performs the registration operation.
     *
     * This method takes a username and a password as arguments, makes an API call to register with the given username and password,
     * and updates the registerResult and registerErr LiveData objects with the result of the operation.
     *
     * @param username The username to register with.
     * @param password The password to register with.
     */
    fun register(username: String, password: String) {
        viewModelScope.launch {
            try {
                val success = unwiredAPI.register(username, password)
                if (success.isSuccessful) {
                    userStore.saveToken(success.body()?.access_token.toString())
                } else {
                    val jObjError: JSONObject = JSONObject(success.errorBody()?.string() ?: "")
                    _registerErr.postValue(jObjError["detail"].toString())
                }
                _registerResult.postValue(success.isSuccessful)
            } catch (e: Exception) {
                // Handle error
                println(e.stackTraceToString())
                _registerResult.postValue(false)
            }
        }
    }
}