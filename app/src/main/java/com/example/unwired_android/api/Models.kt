package com.example.unwired_android.api

import com.google.gson.annotations.SerializedName

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