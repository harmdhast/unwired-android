package com.example.unwired_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier


class MainActivity : ComponentActivity() {
    private val loginViewModel: LoginViewModel by viewModels()
    private val mainMenuViewModel: MainMenuViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            //UnwiredandroidTheme {
            // A surface container using the 'background' color from the theme
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                LoginContent()
            }
            //}
        }
    }

    @Composable
    fun LoginContent() {
        var loggedIn by remember { mutableStateOf(false) }

        if (loggedIn) {
            MainMenu(viewModel = mainMenuViewModel, context = this@MainActivity)
        } else {
            LoginScreen(
                viewModel = loginViewModel,
                onLoginResult = { success ->
                    loggedIn = success
                },
                applicationContext
            )
        }
    }
}