package com.example.unwired_android.viewmodels

import androidx.lifecycle.ViewModel
import com.example.unwired_android.api.Group
import com.example.unwired_android.api.UnwiredAPI
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val unwiredAPI: UnwiredAPI
) : ViewModel() {
    suspend fun getGroup(groupId: Int): Group? {
        val response = unwiredAPI.getGroup(groupId)
        return if (response.isSuccessful) {
            response.body()
        } else {
            null
        }
    }
}