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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.unwired_android.api.Message
import com.example.unwired_android.ui.theme.UnwiredandroidTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GroupMessagesActivity : ComponentActivity() {
    //private val groupViewModel: GroupViewModel by viewModels()

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

    LaunchedEffect(Unit) {
        groupViewModel.getCurrentUser()
        groupViewModel.getGroups()
        groupViewModel.getMessages(groupId)
        groupViewModel.getMembers(groupId)
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
    Column {
        TopAppBar(
            title = { groups?.let { group -> Text(text = group.first { it.id == groupId }.name) } },
            navigationIcon = {
                IconButton(onClick = { activity.finish() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "")
                }
            }
        )

        messages?.let {
            LazyColumn(state = listState) {
                var prevMessage: Message? = null
                items(it, key = { it.id }) { message ->
                    println("Render")
                    val isAuthor = message.author.id == currentUser?.id

                    val sameAuthor = prevMessage?.author?.id == message.author.id
                    val color =
                        if (isAuthor) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary

                    if (!sameAuthor) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {

                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(start = 4.dp, end = 4.dp)
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
                                text = message.content + Text(text = isAuthor.toString()) + Text(
                                    message.author.id.toString()
                                ),
                                color = textColorForBackground(color)
                            )
                        }

                    }

                    prevMessage = message
                }
            }
        }

        Row {
            TextField(value = newMsg, onValueChange = { newMsg = it })
            IconButton(onClick = { /*TODO*/ }) {
                Icon(Icons.AutoMirrored.Filled.Send, "")
            }
        }

    }
}