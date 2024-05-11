package com.example.unwired_android

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


// Retrofit singleton
object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8000/"

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okhttpClient())
            .build()
    }


    private fun okhttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        return OkHttpClient.Builder()
            .addInterceptor(AuthMiddleware())
            .addInterceptor(logging)
            .build()
    }
}

object ApiClient {
    val apiService: UnwiredAPI by lazy {
        RetrofitClient.retrofit.create(UnwiredAPI::class.java)
    }
}

class AuthMiddleware() : Interceptor {
    private val sessionManager = SessionManager

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()

        println(sessionManager.fetchAuthToken())
        // If token has been saved, add it to the request
        sessionManager.fetchAuthToken()?.let {
            requestBuilder.addHeader("Authorization", "Bearer $it")
        }

        return chain.proceed(requestBuilder.build())
    }
}


object SessionManager {
    private var authToken: String? = null

    fun saveAuthToken(token: String) {
        authToken = token
    }

    fun fetchAuthToken(): String? {
        return authToken
    }
}


