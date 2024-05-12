package com.example.unwired_android

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unwired_android.api.Group
import com.example.unwired_android.api.User
import com.example.unwired_android.api.UserStore
import kotlinx.coroutines.launch

@Composable
fun MainMenu(viewModel: MainMenuViewModel, context: ComponentActivity) {
    var selected by remember {
        mutableIntStateOf(0)
    }
    val currentUser by remember { viewModel.currentUser }.observeAsState()
    val groups by remember(viewModel) { viewModel.groups }.observeAsState()

    SideEffect {
        if (currentUser == null) {
            viewModel.getCurrentUser()
        }
    }

    SideEffect {
        if (groups == null) {
            viewModel.getGroups()
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {

                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.AutoMirrored.Filled.Message,
                            contentDescription = "List Groups"
                        )
                    },
                    label = { Text("Groups") },
                    selected = selected == 0,
                    onClick = { selected = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Add, contentDescription = "Create Group") },
                    label = { Text("Add Group") },
                    selected = selected == 1,
                    onClick = { selected = 1 }
                )
                NavigationBarItem(
                    icon = {
                        Base64Avatar(b64 = currentUser?.avatar, size = 40)
                    },
                    //label = { Text(currentUser?.username.toString()) },
                    selected = selected == 2,
                    onClick = { selected = 2 }
                )

            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            when (selected) {
                0 -> {
                    groups?.let { GroupList(it) }
                }

                1 -> {
                    Text(
                        modifier = Modifier.padding(8.dp),
                        text =
                        """
                                Create group
                            """.trimIndent(),
                    )
                }

                2 -> {
                    currentUser?.let { UserView(user = it) }
                }


            }

        }
    }
}

fun base64ToBitmap(b64: String): ImageBitmap? {
    return try {
        val imageBytes = Base64.decode(b64, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size).asImageBitmap()
    } catch (_: Exception) {
        println("Couldn't convert avatar")
        null
    }
}


@Composable
fun Base64Avatar(b64: String?, size: Int = 200): Unit {
    if (b64 == null) {
        return Image(
            painter = painterResource(id = R.drawable.avatar),
            contentDescription = "Avatar",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .size(size.dp)
                .clip(CircleShape)
        )
    }
    val decodedImage = base64ToBitmap(b64)
    return if (decodedImage != null) {
        Image(
            bitmap = decodedImage,
            contentDescription = "Avatar",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .size(size.dp)
                .clip(CircleShape)
        )
    } else {
        Image(
            painter = painterResource(id = R.drawable.avatar),
            contentDescription = "Avatar",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .size(size.dp)
                .clip(CircleShape)
        )
    }
}

@Composable
fun UserView(user: User) {
    Column(
        modifier = Modifier
            .clickable { /* Handle click on group */ }
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally

    ) {
        Base64Avatar(user.avatar)
        Text(
            text = user.username,
            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 32.sp)
        )
    }
}

@Composable
fun GroupList(groups: List<Group>) {
    LazyColumn {
        items(groups) { group ->
            GroupListItem(group = group)
        }
    }
}

@Composable
fun GroupListItem(group: Group) {
    println(group.id)
    val activity = LocalContext.current as Activity
    Column(
        modifier = Modifier
            .padding(16.dp)
            .clickable {
                val intent = Intent(activity, GroupActivity::class.java)
                intent.putExtra("groupId", group.id)
                activity.startActivity(intent)
            }
    ) {
        Text(text = group.name, style = TextStyle(fontWeight = FontWeight.Bold))
        Text(text = group.lastMessage.content, style = TextStyle(color = Color.Gray))
    }
}

class MainMenuViewModel(context: Context) : ViewModel() {
    private val userStore = UserStore(context)
    // private val token = runBlocking { userStore.getAccessToken.first() }

    private val _currentUser = MutableLiveData<User>()
    val currentUser: LiveData<User> = _currentUser

    private val _groups = MutableLiveData<List<Group>>()
    val groups: LiveData<List<Group>> = _groups


    fun getCurrentUser() {
        viewModelScope.launch {
            try {
//                val success = ApiClient.apiService.getCurrentUser(token)
//                _currentUser.postValue(success.body())
            } catch (e: Exception) {
                // Handle error
                //_loginResult.postValue()
            }
        }
    }

    fun getGroups() {
        viewModelScope.launch {
            try {
//                val success = ApiClient.apiService.getGroups(token)
//                _groups.postValue(success.body())
            } catch (e: Exception) {
                // Handle error
                //_loginResult.postValue()
            }
        }
    }
}
