package com.example.unwired_android.api

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

data class LoginResponse(
    val access_token: String,
    val token_type: String,
    val username: String,
    val avatar: String,
    val disabled: Boolean
)

data class Message(
    val id: Int,

    @SerializedName("parent_id")
    val parentId: Int,
    val content: String,

    @SerializedName("author_id")
    val authorId: Int,
    val author: User,
    val at: String
)

data class User(val id: Int, val username: String, val avatar: String)

data class Group(
    val id: Int,
    val name: String,

    @SerializedName("owner_id")
    val ownerId: Int,

    val owner: User,

    val members: List<User>,
    val private: Boolean,

    @SerializedName("last_message")
    val lastMessage: Message?
)

data class GroupCreateBody(
    val name: String,
    val private: Boolean,
    val members: List<Int>
)

data class SendMessageBody(
    @SerializedName("message_content")
    val messageContent: String
)

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

    @POST("groups/{id}/messages")
    suspend fun sendMessage(
        @Path("id") groupId: Int,
        @Body sendMessageBody: SendMessageBody,
    ): Response<Message>

    @GET("groups/{id}/members")
    suspend fun getMembers(
        @Path("id") groupId: Int,
    ): Response<List<User>>

    @FormUrlEncoded
    @POST("register")
    suspend fun register(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<LoginResponse>

    @POST("groups")
    suspend fun groupAdd(
        @Body groupCreateBody: GroupCreateBody
    ): Response<Group>
}