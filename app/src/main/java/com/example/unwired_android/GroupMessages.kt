package com.example.unwired_android

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.unwired_android.api.Message
import com.example.unwired_android.ui.theme.UnwiredandroidTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking

@AndroidEntryPoint
class GroupMessagesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val groupId = intent.getIntExtra("groupId", 0)
        setContent {
            UnwiredandroidTheme {
                GroupMessages(groupId)
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupMessages(groupId: Int) {
    val groupViewModel: GroupViewModel = hiltViewModel()
    val groups by remember { groupViewModel.groups }.observeAsState()
    val messages by remember { groupViewModel.messages }.observeAsState()
    val avatars by remember { groupViewModel.avatars }.observeAsState()
    val listState = rememberLazyListState()
    val currentUser by remember {
        groupViewModel.currentUser
    }.observeAsState()
    var newMsg by remember { mutableStateOf("") }
    var newMsgError by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current


    LaunchedEffect(Unit) {
        groupViewModel.getCurrentUser()
        groupViewModel.getGroups()
        groupViewModel.getMessages(groupId)
        groupViewModel.getMembers(groupId)
    }


    fun sendMessage() {
        if (newMsg.isBlank()) {
            newMsgError = true
            return
        }

        newMsgError = false
        val message = runBlocking { groupViewModel.sendMessage(groupId, newMsg) }

        if (message == null) {
            newMsgError = true
            return
        }

        newMsg = ""
        focusManager.clearFocus()

        //groupViewModel.getMessages(groupId)

    }

    @Composable
    fun AvatarOrDefault(userId: Int, size: Int = 24, modifier: Modifier = Modifier) {
        val avatar = avatars?.get(userId)
        if (avatar != null) {
            return Image(
                bitmap = avatar,
                contentDescription = "Avatar",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .size(size.dp)
                    .clip(CircleShape)
                    .then(modifier)
            )
        } else {
            return Image(
                painter = painterResource(id = R.drawable.avatar),
                contentDescription = "Avatar",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .size(size.dp)
                    .clip(CircleShape)
                    .then(modifier)
            )
        }
    }


    val activity = LocalContext.current as Activity
    Scaffold(
        topBar = {
            TopAppBar(
                title = { groups?.let { group -> Text(text = group.first { it.id == groupId }.name) } },
                navigationIcon = {
                    IconButton(onClick = { activity.finish() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "")
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

                    itemsIndexed(it) { index, message ->
                        val prevMessage: Message? = it.elementAtOrNull(index - 1)
                        val isAuthor = message.author.id == currentUser?.id

                        val sameAuthor = prevMessage?.author?.id == message.author.id
                        val color =
                            if (isAuthor) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary

                        if (!sameAuthor) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        Row(modifier = Modifier.fillMaxWidth()) {

                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .padding(4.dp)
                            ) {
                                if (!sameAuthor) {
                                    AvatarOrDefault(
                                        message.author.id,
                                        size = 48
                                    )
                                } else {
                                    Spacer(modifier = Modifier.width(48.dp))
                                }

                            }

                            Column(
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .background(
                                        color
                                    )
                                    .padding(8.dp)
                                    .heightIn(min = 32.dp)
                                    .widthIn(max = LocalConfiguration.current.screenWidthDp.dp * .75f)

                            ) {
                                Text(
                                    text = message.content,
                                    color = textColorForBackground(color)
                                )
                            }

                        }
                    }
                }


            }
    }
}