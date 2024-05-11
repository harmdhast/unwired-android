package com.example.unwired_android

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query


interface UnwiredAPI {
    @FormUrlEncoded
    @POST("token")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<LoginResponse>

    @GET("users/me")
    suspend fun getCurrentUser(): Response<User>

    @GET("groups")
    suspend fun getGroups(): Response<List<Group>>

    @GET("groups/{id}/messages")
    suspend fun getMessages(
        @Path("id") groupId: Int,
        @Query("limit") limit: Int = 100
    ): Response<List<Message>>

    @GET("groups/{id}/members")
    suspend fun getMembers(@Path("id") groupId: Int): Response<List<User>>
}