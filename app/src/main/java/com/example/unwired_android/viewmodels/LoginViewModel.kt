package com.example.unwired_android.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unwired_android.api.UnwiredAPI
import com.example.unwired_android.api.UserStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val unwiredAPI: UnwiredAPI,
    private val userStore: UserStore
) : ViewModel() {
    private val _loginResult = MutableLiveData<Boolean>()
    val loginResult: LiveData<Boolean> = _loginResult

    fun login(username: String, password: String, context: Context) {
        viewModelScope.launch {
            try {
                val success = unwiredAPI.login(username, password)
                if (success.isSuccessful) {
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
}