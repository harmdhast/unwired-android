/**
 * This file contains the Chat composable function which is responsible for displaying the chat interface.
 *
 * The Chat function takes a Group object as an argument and displays a chat interface for that group.
 * It uses the GroupViewModel to get the current user, the messages for the group, and the members of the group.
 * It also provides a text field for the user to enter new messages and a button to send the messages.
 *
 * The Chat function uses a Scaffold to provide a top bar and a bottom bar.
 * The top bar displays the name of the group and a back button.
 * The bottom bar contains the text field for entering new messages and the send button.
 *
 * The Chat function also uses a ModalNavigationDrawer to display a list of the members of the group.
 * The drawer can be opened by clicking on the group icon in the top bar.
 * If the current user is the owner of the group and the group is not private, the drawer also provides a button to add new members to the group.
 *
 * The Chat function uses a LazyColumn to display the messages in the chat.
 * Each message is displayed as a MessageBlock, and if the message is the last message from the same author, it is displayed with the author's name and avatar.
 *
 * The Chat function also provides a dialog for adding new members to the group.
 * The dialog displays a list of users who are not already members of the group, and provides a button to add a user to the group.
 */

package com.example.unwired_android.app.chat

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.unwired_android.api.Group
import com.example.unwired_android.app.misc.Base64Avatar
import com.example.unwired_android.viewmodels.GroupViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Chat(group: Group) {
    val groupViewModel: GroupViewModel = hiltViewModel()
    val messages by remember { groupViewModel.messages }.observeAsState()
    val members by remember { groupViewModel.members }.observeAsState()
    val listState = rememberLazyListState()
    val currentUser by remember {
        groupViewModel.currentUser
    }.observeAsState()
    var newMsg by remember { mutableStateOf("") }
    var newMsgError by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showAddMemberDialog by remember { mutableStateOf(false) }
    val activity = LocalContext.current as Activity

    LaunchedEffect(Unit) {
        groupViewModel.getCurrentUser()
        groupViewModel.getGroups()
        groupViewModel.getMessages(group.id)
        groupViewModel.getMembers(group.id)
    }

    LaunchedEffect(showAddMemberDialog) {
        groupViewModel.getUsers()
    }

    fun sendMessage() {
        if (newMsg.isBlank()) {
            newMsgError = true
            return
        }

        newMsgError = false
        val message = runBlocking { groupViewModel.sendMessage(group.id, newMsg) }

        if (message == null) {
            newMsgError = true
            return
        }

        newMsg = ""
        focusManager.clearFocus()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = group.name) },
                navigationIcon = {
                    IconButton(onClick = { activity.finish() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "")
                    }
                },
                actions = {
                    BadgedBox(
                        badge = {
                            members?.let { members ->
                                Badge(
                                    modifier = Modifier.padding(4.dp),
                                    contentColor = Color.Black,
                                    containerColor = Color.White
                                ) {
                                    Text(text = members.size.toString())
                                }
                            }
                        }
                    ) {
                        IconButton(onClick = {
                            scope.launch {
                                drawerState.apply {
                                    if (isClosed) open() else close()
                                }
                            }
                        }) {
                            Icon(Icons.Filled.Group, "")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        },
        bottomBar = {
            BottomAppBar(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                TextField(
                    value = newMsg,
                    onValueChange = { newMsg = it },
                    modifier = Modifier.weight(1f),
                    isError = newMsgError,
                    keyboardActions = KeyboardActions {
                        sendMessage()
                    }
                )
                IconButton(onClick = { sendMessage() }) {
                    Icon(Icons.AutoMirrored.Filled.Send, "")
                }
            }
        }) { padding ->


        ModalNavigationDrawer(
            modifier = Modifier
                .fillMaxHeight(),
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    drawerContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(padding)
                ) {
                    // Button to add members
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (group.private) {
                            Text("This group is private", fontWeight = FontWeight.Bold)
                        }

                        if (group.ownerId == currentUser?.id && !group.private) {
                            Button(onClick = {
                                scope.launch {
                                    drawerState.close()
                                    showAddMemberDialog = true
                                }
                            }) {
                                Text(
                                    "Add member",
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Icon(Icons.Filled.PersonAdd, "")
                            }

                        }
                    }
                    // Current members
                    members?.let { members ->
                        LazyColumn(
                            modifier = Modifier.fillMaxHeight(),
                            content = {
                                items(members) { member ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Base64Avatar(
                                            member.avatar,
                                            size = 48,
                                            modifier = Modifier.padding(4.dp)
                                        )
                                        Text(
                                            text = member.username,
                                            style = MaterialTheme.typography.titleMedium,
                                            modifier = Modifier
                                                .padding(8.dp)
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
            },
        ) {
            // Messages
            messages?.let {
                LazyColumn(
                    state = listState, modifier = Modifier
                        .padding(padding)
                        .fillMaxHeight(), reverseLayout = true
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    itemsIndexed(it) { index, message ->
                        val isAuthor = message.author.id == currentUser?.id
                        // val prevAuthor = it.getOrNull(index - 1)?.author
                        val nextAuthor = it.getOrNull(index + 1)?.author
                        // val isFirstMessage = prevAuthor != message.author
                        val isLastMessage = nextAuthor != message.author

                        if (isLastMessage) {

                            AuthorAndMessage(
                                message = message,
                                isAuthor = isAuthor,
                                user = group.members.find { it.id == message.author.id }
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                        } else {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Spacer(modifier = Modifier.width(64.dp))
                                MessageBlock(message = message, isAuthor = isAuthor)
                            }
                        }

                    }
                }
            }
        }

        // Add member dialog
        if (showAddMemberDialog) {
            val allUsers by groupViewModel.users.observeAsState()
            val nonMembers = allUsers?.filter { user -> !members!!.contains(user) }

            DialogAddMember(
                nonMembers = nonMembers!!,
                onDismissRequest = { showAddMemberDialog = false },
                onAddMember = { user ->
                    scope.launch {
                        runBlocking { groupViewModel.addMember(group.id, user.id) }
                        groupViewModel.getMembers(group.id)
                        showAddMemberDialog = false
                    }
                }
            )
        }
    }
}

