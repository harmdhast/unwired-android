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
    /**
     * Retrieves a group by its ID.
     *
     * This method makes an API call to get the group with the given ID.
     * If the API call is successful, it returns the group.
     * If the API call is not successful, it returns null.
     *
     * @param groupId The ID of the group to retrieve.
     * @return The group with the given ID, or null if the API call is not successful.
     */
    suspend fun getGroup(groupId: Int): Group? {
        val response = unwiredAPI.getGroup(groupId)
        return if (response.isSuccessful) {
            response.body()
        } else {
            null
        }
    }
}