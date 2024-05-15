package com.example.unwired_android.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unwired_android.api.UnwiredAPI
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    private val unwiredAPI: UnwiredAPI
) : ViewModel() {
    private val _isTokenValid = MutableLiveData<Boolean>()
    val isTokenValid: LiveData<Boolean> = _isTokenValid

    private val _canReachServer = MutableLiveData<Boolean>()
    val canReachServer: LiveData<Boolean> = _canReachServer

    fun tokenValid() {
        viewModelScope.launch {
            try {
                val resp = unwiredAPI.getCurrentUser()
                if (resp.isSuccessful) {
                    _isTokenValid.postValue(true)
                } else {
                    _isTokenValid.postValue(false)
                }
            } catch (e: Exception) {
                // Handle error
                _isTokenValid.postValue(false)
            }
        }
    }

    fun pingServer(url: String, port: Int, timeout: Int = 10) {
        viewModelScope.launch {
            _canReachServer.postValue(
                isServerReachable(url, port, timeout)
            )

        }
    }

    private suspend fun isServerReachable(host: String, port: Int, timeout: Int): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                println("Checking for server at $host:$port")
                Socket().use { socket ->
                    val inetAddress = InetAddress.getByName(host)
                    val inetSocketAddress =
                        InetSocketAddress(inetAddress, port)

                    socket.connect(inetSocketAddress, timeout * 1000)
                    true
                }
            } catch (e: IOException) {
                e.printStackTrace()
                false
            }
        }
    }
}