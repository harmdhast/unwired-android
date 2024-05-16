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
    /**
     * LiveData object for observing the validity of the token.
     */
    private val _isTokenValid = MutableLiveData<Boolean>()
    val isTokenValid: LiveData<Boolean> = _isTokenValid

    /**
     * LiveData object for observing the reachability of the server.
     */
    private val _canReachServer = MutableLiveData<Boolean>()
    val canReachServer: LiveData<Boolean> = _canReachServer

    /**
     * Validates the token by making an API call to get the current user.
     * If the API call is successful, it posts true to _isTokenValid.
     * If the API call is not successful, it posts false to _isTokenValid.
     */
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

    /**
     * Pings the server by calling the isServerReachable method with the given URL, port, and timeout.
     * It posts the result to _canReachServer.
     *
     * @param url The URL of the server.
     * @param port The port of the server.
     * @param timeout The timeout for the ping in seconds.
     */
    fun pingServer(url: String, port: Int, timeout: Int = 10) {
        viewModelScope.launch {
            _canReachServer.postValue(
                isServerReachable(url, port, timeout)
            )

        }
    }

    /**
     * Checks if the server is reachable by attempting to establish a socket connection.
     *
     * This function is a suspending function and should be called from a coroutine or another suspending function.
     * It uses the IO dispatcher to perform the network operation on a background thread.
     *
     * It attempts to establish a socket connection to the server with the given host and port.
     * If the connection is successful, it returns true.
     * If the connection is not successful, it catches the IOException, prints the stack trace, and returns false.
     *
     * @param host The host of the server.
     * @param port The port of the server.
     * @param timeout The timeout for the connection attempt in seconds.
     * @return A Boolean indicating whether the server is reachable.
     */
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