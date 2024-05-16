package com.example.unwired_android.app.group

import android.app.Activity
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.unwired_android.R
import com.example.unwired_android.app.misc.Base64Avatar
import com.example.unwired_android.viewmodels.GroupViewModel
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsMenu() {
    val viewModel: GroupViewModel = hiltViewModel()
    val currentUser by remember { viewModel.currentUser }.observeAsState()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val activity = LocalContext.current as Activity

    LaunchedEffect(Unit) {
        viewModel.getCurrentUser()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Image(
                        painter = painterResource(R.drawable.unwired_white),
                        "Logo"
                    )
                },
                actions = {
                    // Icon button to logout
                    Icon(
                        Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Logout",
                        modifier = Modifier
                            .clickable {
                                runBlocking {
                                    viewModel.logout()
                                }
                                activity.finish()
                            }
                            .padding(8.dp)
                    )

                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
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
                enterTransition = { fadeIn(animationSpec = tween(200)) },
                exitTransition = { fadeOut(animationSpec = tween(200)) }
            ) {
                composable("groups") {
                    GroupsList()
                }
                composable("add") {
                    GroupsAdd(navController)
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
