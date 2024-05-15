package com.example.unwired_android.app.chat

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFrom
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.unwired_android.GroupViewModel
import com.example.unwired_android.api.Group
import com.example.unwired_android.api.Message
import com.example.unwired_android.ui.utils.Base64Avatar
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDateTime


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

    @Composable
    fun AuthorAndMessage(message: Message, isAuthor: Boolean) {
        Row {
            Column(Modifier.padding(8.dp)) {
                Base64Avatar(
                    members?.find { it.id == message.authorId }?.avatar,
                    size = 48,
                    //modifier = Modifier.padding(4.dp)
                )
            }

            Column {
                Row(modifier = Modifier.semantics(mergeDescendants = true) {}) {
                    Text(
                        text = message.author.username,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .alignBy(LastBaseline)
                            .paddingFrom(LastBaseline, after = 8.dp) // Space to 1st bubble
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${
                            LocalDateTime.parse(message.at).time.hour.toString().padStart(2, '0')
                        }:${
                            LocalDateTime.parse(
                                message.at
                            ).time.minute.toString().padStart(2, '0')
                        }",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.alignBy(LastBaseline),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                }
                MessageBlock(message = message, isAuthor = isAuthor)
            }

        }
    }

    val activity = LocalContext.current as Activity

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
        messages?.let {
            LazyColumn(
                state = listState, modifier = Modifier
                    .padding(padding)
                    .fillMaxHeight(), reverseLayout = true
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                itemsIndexed(it) { index, message ->
                    val isAuthor = message.author.id == currentUser?.id
                    val prevAuthor = it.getOrNull(index - 1)?.author
                    val nextAuthor = it.getOrNull(index + 1)?.author
                    val isFirstMessage = prevAuthor != message.author
                    val isLastMessage = nextAuthor != message.author

                    if (isLastMessage) {
                        //Spacer(modifier = Modifier.height(6.dp))
                        AuthorAndMessage(message = message, isAuthor = isAuthor)
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

        ModalNavigationDrawer(
            modifier = Modifier
                .fillMaxHeight()
                .padding(padding),
            drawerState = drawerState,
            gesturesEnabled = false,
            drawerContent = {
                ModalDrawerSheet(
                    drawerContainerColor = MaterialTheme.colorScheme.primaryContainer,
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

                    members?.let { members ->
                        LazyColumn(
                            modifier = Modifier.fillMaxHeight(),
                            content = {
                                itemsIndexed(members) { index, member ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp)
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
                                                .alignBy(LastBaseline)
                                                .paddingFrom(
                                                    LastBaseline,
                                                    after = 8.dp
                                                ) // Space to 1st bubble
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
            },
        ) {
        }

        if (showAddMemberDialog) {
            val allUsers by groupViewModel.users.observeAsState()
            val nonMembers = allUsers?.filter { user -> !members!!.contains(user) }

            DialogAddMember(
                nonMembers = nonMembers!!,
                onDismissRequest = { showAddMemberDialog = false },
                onAddMember = { user ->
                    scope.launch {
                        //groupViewModel.addMember(group.id, user.id)
                        // groupViewModel.getMembers(group.id)
                    }
                }
            )
        }
    }
}

