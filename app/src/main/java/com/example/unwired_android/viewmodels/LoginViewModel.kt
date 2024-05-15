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
    private val _loginResult = MutableLiveData<Boolean>()
    val loginResult: LiveData<Boolean> = _loginResult
    private val _registerResult = MutableLiveData<Boolean>()
    val registerResult: LiveData<Boolean> = _registerResult

    private val _registerErr = MutableLiveData<String>()
    val registerErr: LiveData<String> = _registerErr

    fun login(username: String, password: String) {
        viewModelScope.launch {
            try {
                val success = unwiredAPI.login(username, password)
                if (success.isSuccessful) {
                    userStore.saveUserAndPassword(username, password)
                    userStore.saveToken(success.body()?.access_token.toString())
                } else {

                }
                _loginResult.postValue(success.isSuccessful)
            } catch (e: Exception) {
                // Handle error
                _loginResult.postValue(false)
            }
        }
    }

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