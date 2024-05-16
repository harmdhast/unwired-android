package com.example.unwired_android.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unwired_android.api.Group
import com.example.unwired_android.api.GroupCreateBody
import com.example.unwired_android.api.Message
import com.example.unwired_android.api.SendMessageBody
import com.example.unwired_android.api.UnwiredAPI
import com.example.unwired_android.api.User
import com.example.unwired_android.api.UserStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupViewModel @Inject constructor(
    private val unwiredAPI: UnwiredAPI,
    private val userStore: UserStore
) : ViewModel() {
    //private val token = runBlocking { userStore.getAccessToken.first() }

    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> = _users

    /**
     * Retrieves a list of users.
     *
     * This method makes an API call to get the users.
     * If the API call is successful, it updates the users LiveData object.
     * If the API call is not successful, it does not update the users LiveData object.
     *
     * @param searchQuery The query to search for users.
     */
    fun getUsers(searchQuery: String = "") {
        viewModelScope.launch {
            val resp = unwiredAPI.getUsers(searchQuery)
            if (resp.isSuccessful) {
                val usersWithoutCurrentUser =
                    resp.body()?.filter { it.id != currentUser.value?.id } ?: listOf()
                _users.postValue(usersWithoutCurrentUser)
            }
        }
    }


    private val _currentUser = MutableLiveData<User>()
    val currentUser: LiveData<User> = _currentUser

    /**
     * Retrieves the current user.
     *
     * This method makes an API call to get the current user.
     * If the API call is successful, it updates the currentUser LiveData object.
     * If the API call is not successful, it does not update the currentUser LiveData object.
     */
    fun getCurrentUser() {
        viewModelScope.launch {
            try {
                val resp = unwiredAPI.getCurrentUser()
                if (resp.isSuccessful) {
                    _currentUser.postValue(resp.body())
                }
            } catch (e: Exception) {
                // Handle error
                //_loginResult.postValue()
            }
        }
    }

    private val _groups = MutableLiveData<List<Group>>()
    val groups: LiveData<List<Group>> = _groups

    /**
     * Retrieves a list of groups.
     *
     * This method makes an API call to get the groups.
     * If the API call is successful, it updates the groups LiveData object.
     * If the API call is not successful, it does not update the groups LiveData object.
     */
    fun getGroups() {
        viewModelScope.launch {
            val resp = unwiredAPI.getGroups()
            if (resp.isSuccessful) {
                _groups.postValue(resp.body())
            }
        }
    }

    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages

    /**
     * Retrieves messages for a specific group.
     *
     * This method makes an API call to get the messages for the group with the given ID.
     * If the API call is successful, it updates the messages LiveData object.
     * If the API call is not successful, it does not update the messages LiveData object.
     *
     * @param groupId The ID of the group to retrieve messages for.
     */
    fun getMessages(groupId: Int) {
        viewModelScope.launch {
            try {
                val resp = unwiredAPI.getMessages(groupId)
                if (resp.isSuccessful) {
                    _messages.postValue(resp.body())
                }
            } catch (e: Exception) {
                // Handle error
                //_loginResult.postValue()
            }
        }
    }

    private val _members = MutableLiveData<List<User>>()
    val members: LiveData<List<User>> = _members

    /**
     * Retrieves members for a specific group.
     *
     * This method makes an API call to get the members for the group with the given ID.
     * If the API call is successful, it updates the members LiveData object.
     * If the API call is not successful, it does not update the members LiveData object.
     *
     * @param groupId The ID of the group to retrieve members for.
     */
    fun getMembers(groupId: Int) {
        viewModelScope.launch {
            try {
                val success = unwiredAPI.getMembers(groupId)
                _members.postValue(success.body())
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private val _newGroup = MutableLiveData<Group>()
    val newGroup: LiveData<Group> = _newGroup

    /**
     * Creates a new group.
     *
     * This method makes an API call to create a new group with the given name, privacy setting, and members.
     * If the API call is successful, it returns the new group.
     * If the API call is not successful, it returns null.
     *
     * @param name The name of the new group.
     * @param private A boolean indicating whether the new group is private.
     * @param members A list of users to add to the new group.
     * @return The new group, or null if the API call is not successful.
     */
    suspend fun groupAdd(name: String, private: Boolean, members: List<User>): Group? {
        val response =
            unwiredAPI.groupAdd(GroupCreateBody(name, private, members.map { it.id }))
        return if (response.isSuccessful) {
            response.body()
        } else {
            null
        }
    }

    /**
     * Sends a message to a specific group.
     *
     * This method makes an API call to send a message with the given content to the group with the given ID.
     * If the API call is successful, it adds the new message to the top of the messages LiveData object and returns the message.
     * If the API call is not successful, it returns null.
     *
     * @param groupId The ID of the group to send the message to.
     * @param content The content of the message.
     * @return The new message, or null if the API call is not successful.
     */
    suspend fun sendMessage(groupId: Int, content: String): Message? {
        val response =
            unwiredAPI.sendMessage(groupId, SendMessageBody(content))
        if (response.isSuccessful) {
            //getMessages(groupId)
            val message = response.body()
            println(message)
            if (message != null) {
                // Push message to the top of the list
                val currentMessages = _messages.value
                if (currentMessages != null) {
                    _messages.postValue(listOf(message) + currentMessages)
                }
            }
            return message
        } else {
            return null
        }
    }

    /**
     * Logout function.
     *
     * This function deletes the user's token from the UserStore.
     * It is a suspend function, which means it can be called from a coroutine or another suspend function.
     */
    suspend fun logout() {
        userStore.deleteToken()
    }

    /**
     * Adds a member to a specific group.
     *
     * This function makes an API call to add a user to the group with the given ID.
     * If the API call is successful, it retrieves the updated list of members for the group.
     *
     * @param groupId The ID of the group to add the member to.
     * @param user The ID of the user to add to the group.
     */
    fun addMember(groupId: Int, user: Int) {
        viewModelScope.launch {
            val resp = unwiredAPI.addUserToGroup(groupId, user)
            if (resp.isSuccessful) {
                getMembers(groupId)
            }
        }
    }
}