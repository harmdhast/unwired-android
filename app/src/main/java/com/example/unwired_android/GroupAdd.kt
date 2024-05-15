package com.example.unwired_android

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.unwired_android.api.User
import com.example.unwired_android.app.chat.ChatActivity
import com.example.unwired_android.ui.utils.base64ToBitmap
import kotlinx.coroutines.runBlocking

@Composable
fun GroupAdd(navHostController: NavHostController) {
    var groupName by remember { mutableStateOf("") }
    var isGroupNameError by remember { mutableStateOf(false) }
    var groupNameError by remember { mutableStateOf("") }
    val groupViewModel: GroupViewModel = hiltViewModel()
    var private by remember {
        mutableStateOf(false)
    }
    val activity = LocalContext.current as Activity
    val users by groupViewModel.users.observeAsState()
    val selectedUsers = remember { mutableStateMapOf<User, Boolean>() }
    var searchQuery by remember { mutableStateOf("") }
    var selectedUser by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(Unit) {
        groupViewModel.getUsers(searchQuery)
    }

    LaunchedEffect(searchQuery) {
        groupViewModel.getUsers(searchQuery)
    }

    LaunchedEffect(users, private) {
        users?.forEach { user ->
            selectedUsers[user] = false
        }
        selectedUser = null
    }

    fun doCreateGroup(name: String) {
        if (name.length < 5) {
            isGroupNameError = true
            groupNameError = "Group name length must be at least 6"
            return
        }

        if (private && selectedUser == null) {
            isGroupNameError = true
            groupNameError = "Select a user for private group"
            return
        } else if (!private && selectedUsers.isEmpty()) {
            isGroupNameError = true
            groupNameError = "Select at least one user for public group"
            return
        }

        val members = if (private) {
            listOf(selectedUser!!)
        } else {
            selectedUsers.filter { it.value }.keys.toList()
        }
        val group = runBlocking { groupViewModel.groupAdd(name, private, members) }

        if (group == null) {
            isGroupNameError = true
            groupNameError = "Unknown error while creating group"
            return
        }

        println(group)
        isGroupNameError = false
        groupNameError = ""

        navHostController.navigate("groups")
        val intent = Intent(activity, ChatActivity::class.java)
        intent.putExtra("groupId", group.id)
        activity.startActivity(intent)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        TextField(
            value = groupName,
            onValueChange = { groupName = it },
            label = { Text("Group Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = isGroupNameError,
            keyboardActions = KeyboardActions {
                // Handle keyboard action
            },
        )
        Spacer(modifier = Modifier.height(16.dp))
        // Create tabs to switch between private and public groups
        TabRow(selectedTabIndex = if (private) 0 else 1) {
            Tab(
                selected = private,
                onClick = { private = true },
                text = { Text("Private") }
            )
            Tab(
                selected = !private,
                onClick = { private = false },
                text = { Text("Public") }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search Users") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))


        if (users?.isEmpty() == true) {
            Text("No users found")
            return
        }

        // Tab content to select users
        when (private) {
            true -> {
                LazyColumn(
                    Modifier
                        .fillMaxHeight(.4f)
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    users?.let { users ->
                        items(users) { user ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedUser = user },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedUser == user,
                                    onClick = { selectedUser = user }
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(user.username)
                            }
                        }
                    }
                }
            }

            false -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxHeight(.4f)
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    users?.let { users ->
                        items(users) { user ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedUsers[user] = selectedUsers[user]?.not() ?: true
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = selectedUsers[user] ?: false,
                                    onCheckedChange = { selectedUsers[user] = it }
                                )
                                Spacer(Modifier.width(8.dp))
                                // Display user avatar
                                base64ToBitmap(user.avatar)?.let {
                                    Image(
                                        bitmap = it,
                                        contentDescription = "Avatar",
                                        contentScale = ContentScale.FillBounds,
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                    )
                                }
                                Text(user.username)
                            }
                        }
                    }
                }
            }
        }

        Text(text = groupNameError, color = Color.Red)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { doCreateGroup(groupName) }) {
            Text("CREATE")
        }
    }
}
