package com.example.unwired_android

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unwired_android.api.Message
import com.example.unwired_android.api.User
import com.example.unwired_android.api.UserStore
import kotlinx.coroutines.launch

class GroupActivity : ComponentActivity() {
    private val groupViewModel: GroupViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val groupId = intent.getIntExtra("groupId", 0)
        setContent {
            Group(groupId, groupViewModel)
        }
    }
}

@Composable
fun Group(id: Int, viewModel: GroupViewModel) {
    val messages by remember { viewModel.messages }.observeAsState()
    val currentUser by remember { viewModel.currentUser }.observeAsState()
    val members by remember { viewModel.members }.observeAsState()
    val avatars by remember { viewModel.avatars }.observeAsState()
    val listState = rememberLazyListState()

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

    SideEffect {
        if (currentUser == null) {
            viewModel.getCurrentUser()
        }
    }

    SideEffect {
        if (members == null) {
            viewModel.getMembers(id)
        }
    }

    SideEffect {
        if (messages == null) {
            viewModel.getMessages(id)
        }
    }


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
                            text = message.content + Text(text = isAuthor.toString()) + Text(message.author.id.toString()),
                            color = textColorForBackground(color)
                        )
                    }

                }

                prevMessage = message
            }
        }

    }
    Spacer(
        modifier = Modifier
            .height(8.dp)
            .fillMaxWidth()
    )
}

fun textColorForBackground(color: Color): Color {
    return if (color.luminance() * 255 < 150) {
        Color.White
    } else {
        Color.Black
    }
}

class GroupViewModel(context: Context) : ViewModel() {
    private val userStore = UserStore(context)

    //private val token = runBlocking { userStore.getAccessToken.first() }
    private val _currentUser = MutableLiveData<User>()
    val currentUser: LiveData<User> = _currentUser

    fun getCurrentUser() {
        viewModelScope.launch {
            try {
                //val success = ApiClient.apiService.getCurrentUser(token)
                //_currentUser.postValue(success.body())
            } catch (e: Exception) {
                // Handle error
                //_loginResult.postValue()
            }
        }
    }

    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages

    fun getMessages(groupId: Int) {
        viewModelScope.launch {
            try {
                //val success = ApiClient.apiService.getMessages(token, groupId)
                //_messages.postValue(success.body())
            } catch (e: Exception) {
                // Handle error
                //_loginResult.postValue()
            }
        }
    }

    private val _members = MutableLiveData<List<User>>()
    val members: LiveData<List<User>> = _members

    private val _avatars = MutableLiveData<HashMap<Int, ImageBitmap>>()
    val avatars: LiveData<HashMap<Int, ImageBitmap>> = _avatars

    fun getMembers(groupId: Int) {
        viewModelScope.launch {
            try {
                //val success = ApiClient.apiService.getMembers(groupId, token)
//                _members.postValue(success.body())
//                val myHashMap: HashMap<Int, ImageBitmap> = HashMap()
//                success.body()?.forEach { user ->
//                    val bitmap = base64ToBitmap(user.avatar)
//                    if (bitmap != null) {
//                        myHashMap[user.id] = bitmap
//                    }
//                }
                //_avatars.postValue(myHashMap)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}