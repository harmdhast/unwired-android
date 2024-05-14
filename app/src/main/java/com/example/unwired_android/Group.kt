package com.example.unwired_android

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.unwired_android.api.Group
import com.example.unwired_android.api.Message
import com.example.unwired_android.api.UnwiredAPI
import com.example.unwired_android.api.User
import com.example.unwired_android.ui.theme.UnwiredandroidTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class GroupActivity : ComponentActivity() {
    private val groupViewModel: GroupViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val groupId = intent.getIntExtra("groupId", 0)
        setContent {
            UnwiredandroidTheme {
                Group(groupId, groupViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Group(id: Int, viewModel: GroupViewModel) {
    val messages by remember { viewModel.messages }.observeAsState()
    val currentUser by remember { viewModel.currentUser }.observeAsState()
    val members by remember { viewModel.members }.observeAsState()
    val avatars by remember { viewModel.avatars }.observeAsState()
    val listState = rememberLazyListState()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    LaunchedEffect(Unit) {
        viewModel.getCurrentUser()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Image(
                    painter = painterResource(R.drawable.unwired_white),
                    "Logo"
                )
            })
        },
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
                    selected = navBackStackEntry?.destination?.route == "groups",
                    onClick = {
                        println(navBackStackEntry)
                        navController.navigate("groups")
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Add, contentDescription = "Create Group") },
                    label = { Text("Add Group") },
                    selected = navBackStackEntry?.destination?.route == "add",
                    onClick = { navController.navigate("add") }
                )
                NavigationBarItem(
                    icon = {
                        Base64Avatar(b64 = currentUser?.avatar, size = 40)
                    },
                    //label = { Text(currentUser?.username.toString()) },
                    selected = navBackStackEntry?.destination?.route == "user",
                    onClick = { navController.navigate("user") }
                )

            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            NavHost(
                navController = navController,
                startDestination = "groups",
                enterTransition = { fadeIn(animationSpec = tween(700)) },
                exitTransition = { fadeOut(animationSpec = tween(700)) }
            ) {
                composable("groups") {
                    GroupList()
                }
                composable("add") {
                    Text("Test 2")
                }
                composable("user") {
                    currentUser?.let { it1 -> UserView(it1) }
                }
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

@Composable
fun GroupList() {
    val groupViewModel: GroupViewModel = hiltViewModel()
    val groups by remember {
        groupViewModel.groups
    }.observeAsState()

    LaunchedEffect(Unit) {
        groupViewModel.getGroups()
    }

    groups?.let {
        LazyColumn {
            items(it) { group ->
                GroupListItem(group = group)
            }
        }
    }
}

@Composable
fun GroupListItem(group: Group) {
    val activity = LocalContext.current as Activity
    Column(
        modifier = Modifier
            .padding(16.dp)
            .clickable {
                val intent = Intent(activity, GroupMessagesActivity::class.java)
                intent.putExtra("groupId", group.id)
                activity.startActivity(intent)
            }
    ) {
        Text(text = group.name, style = TextStyle(fontWeight = FontWeight.Bold))
        Text(text = group.lastMessage.content, style = TextStyle(color = Color.Gray))
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

@HiltViewModel
class GroupViewModel @Inject constructor(
    private val unwiredAPI: UnwiredAPI
) : ViewModel() {
    //private val token = runBlocking { userStore.getAccessToken.first() }
    private val _currentUser = MutableLiveData<User>()
    val currentUser: LiveData<User> = _currentUser

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

    private val _avatars = MutableLiveData<HashMap<Int, ImageBitmap>>()
    val avatars: LiveData<HashMap<Int, ImageBitmap>> = _avatars


    fun getMembers(groupId: Int) {
        viewModelScope.launch {
            try {
                val success = unwiredAPI.getMembers(groupId)
                _members.postValue(success.body())
                val myHashMap: HashMap<Int, ImageBitmap> = HashMap()
                success.body()?.forEach { user ->
                    val bitmap = base64ToBitmap(user.avatar)
                    if (bitmap != null) {
                        myHashMap[user.id] = bitmap
                    }
                }
                _avatars.postValue(myHashMap)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}