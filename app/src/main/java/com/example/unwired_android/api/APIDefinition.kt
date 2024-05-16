package com.example.unwired_android.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Interface defining the UnwiredAPI endpoints.
 *
 * This interface uses Retrofit annotations to define HTTP requests for each endpoint.
 */
interface UnwiredAPI {
    /**
     * Login endpoint.
     *
     * @param username The username of the user.
     * @param password The password of the user.
     * @return A Response containing a LoginResponse object.
     */
    @FormUrlEncoded
    @POST("token")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<LoginResponse>

    /**
     * Get current user endpoint.
     *
     * @return A Response containing a User object.
     */
    @GET("users/me")
    suspend fun getCurrentUser(): Response<User>

    /**
     * Get groups endpoint.
     *
     * @return A Response containing a list of Group objects.
     */
    @GET("groups")
    suspend fun getGroups(): Response<List<Group>>

    /**
     * Get messages endpoint.
     *
     * @param groupId The ID of the group.
     * @param limit The maximum number of messages to return.
     * @return A Response containing a list of Message objects.
     */
    @GET("groups/{id}/messages")
    suspend fun getMessages(
        @Path("id") groupId: Int,
        @Query("limit") limit: Int = 100
    ): Response<List<Message>>

    /**
     * Send message endpoint.
     *
     * @param groupId The ID of the group.
     * @param sendMessageBody The body of the message to send.
     * @return A Response containing a Message object.
     */
    @POST("groups/{id}/messages")
    suspend fun sendMessage(
        @Path("id") groupId: Int,
        @Body sendMessageBody: SendMessageBody,
    ): Response<Message>

    /**
     * Get members endpoint.
     *
     * @param groupId The ID of the group.
     * @return A Response containing a list of User objects.
     */
    @GET("groups/{id}/members")
    suspend fun getMembers(
        @Path("id") groupId: Int,
    ): Response<List<User>>

    /**
     * Register endpoint.
     *
     * @param username The username of the user.
     * @param password The password of the user.
     * @return A Response containing a LoginResponse object.
     */
    @FormUrlEncoded
    @POST("register")
    suspend fun register(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<LoginResponse>

    /**
     * Group add endpoint.
     *
     * @param groupCreateBody The body of the group to create.
     * @return A Response containing a Group object.
     */
    @POST("groups")
    suspend fun groupAdd(
        @Body groupCreateBody: GroupCreateBody
    ): Response<Group>

    /**
     * Get group endpoint.
     *
     * @param groupId The ID of the group.
     * @return A Response containing a Group object.
     */
    @GET("groups/{id}")
    suspend fun getGroup(
        @Path("id") groupId: Int
    ): Response<Group>

    /**
     * Add user to group endpoint.
     *
     * @param groupId The ID of the group.
     * @param userId The ID of the user to add.
     * @return A Response containing a Group object.
     */
    @POST("groups/{id}/add-user/{user_id}")
    suspend fun addUserToGroup(
        @Path("id") groupId: Int,
        @Path("user_id") userId: Int
    ): Response<Group>

    /**
     * Get users endpoint.
     *
     * This endpoint retrieves a list of User objects. The list can be filtered by providing a query string.
     * If no query string is provided, it returns all users.
     *
     * @param query The query string used to filter the users. Default is an empty string, which returns all users.
     * @return A Response containing a list of User objects.
     */
    @GET("users")
    suspend fun getUsers(@Query("q") query: String = ""): Response<List<User>>
}