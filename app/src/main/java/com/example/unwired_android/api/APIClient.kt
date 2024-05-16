/**
 * This file contains the APIClient class and related classes and functions for managing API calls.
 *
 * The APIClient class provides methods for creating instances of the UnwiredAPI interface, which defines the API endpoints.
 * It also provides methods for creating instances of the OkHttpClient and Retrofit classes, which are used for making the API calls.
 *
 * The AuthMiddleware class is an interceptor that adds the Authorization header to every API request.
 * The ReAuthHandler class handles the case when an API request returns a 401 Unauthorized response.
 * It attempts to re-authenticate the user and retry the original request with the new token.
 *
 * The Context.dataStore property and the API_IP, API_PORT, and baseURL constants are also defined in this file.
 */

package com.example.unwired_android.api

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")
const val API_IP: String = "10.0.2.2"
const val API_PORT: Int = 8000
const val baseURL = "http://$API_IP:$API_PORT/"

@Module
@InstallIn(SingletonComponent::class)
class APIClient {
    @Singleton
    @Provides
    fun provideTokenManager(@ApplicationContext context: Context): UserStore = UserStore(context)

    @Singleton
    @Provides
    fun provideOkHttpClient(
        authInterceptor: AuthMiddleware,
        reAuthHandler: ReAuthHandler
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        val reAuthInterceptor = Interceptor { chain ->
            val response = chain.proceed(chain.request())
            if (response.code == 401) {
                response.close()
                val newRequest = runBlocking { reAuthHandler.handleReAuth(chain.request()) }
                return@Interceptor chain.proceed(newRequest)
            }
            response
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .addInterceptor(reAuthInterceptor)
            .build()
    }

    @Singleton
    @Provides
    fun provideAuthInterceptor(tokenManager: UserStore): AuthMiddleware =
        AuthMiddleware(tokenManager)

    @Singleton
    @Provides
    fun provideRetrofitBuilder(): Retrofit.Builder =
        Retrofit.Builder()
            .baseUrl(baseURL)
            .addConverterFactory(GsonConverterFactory.create())

    @Singleton
    @Provides
    fun provideMainAPIService(
        okHttpClient: OkHttpClient,
        retrofit: Retrofit.Builder
    ): UnwiredAPI =
        retrofit
            .client(okHttpClient)
            .build()
            .create(UnwiredAPI::class.java)
}

/**
 * Interceptor for adding the Authorization header to every API request.
 */
class AuthMiddleware @Inject constructor(
    private val tokenManager: UserStore,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking {
            tokenManager.getToken().first()
        }
        val request = chain.request().newBuilder()
        request.addHeader("Authorization", "Bearer $token")
        return chain.proceed(request.build())
    }
}

/**
 * Handler for the case when an API request returns a 401 Unauthorized response.
 */
class ReAuthHandler @Inject constructor(
    private val userStore: UserStore
) {
    suspend fun handleReAuth(originalRequest: Request): Request {
        val (user, password) = userStore.getUserAndPassword().first()
        if (user != null && password != null) {
            val loginRequest = Request.Builder()
                .url(baseURL + "token")
                .post(
                    okhttp3.FormBody.Builder()
                        .add("username", user)
                        .add("password", password)
                        .build()
                )
                .build()

            OkHttpClient().newCall(loginRequest).execute().use { loginResponse ->
                if (loginResponse.isSuccessful) {
                    val newToken = GsonBuilder().create()
                        .fromJson(loginResponse.body?.string(), LoginResponse::class.java)
                    if (newToken != null) {
                        userStore.saveToken(newToken.access_token)
                        return originalRequest.newBuilder()
                            .header("Authorization", "Bearer $newToken")
                            .build()
                    }
                }
            }
        }
        return originalRequest
    }
}