package com.example.unwired_android.app.chat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.unwired_android.ui.theme.UnwiredandroidTheme
import com.example.unwired_android.viewmodels.ChatViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking

@AndroidEntryPoint
class ChatActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val groupId = intent.getIntExtra("groupId", 0)
        val chatViewModel: ChatViewModel by viewModels()
        val group = runBlocking {
            chatViewModel.getGroup(groupId)
        }

        setContent {
            UnwiredandroidTheme {
                if (group != null) {
                    Chat(group)
                } else {
                    // Handle error
                    println("Error getting group")
                    finish()
                }
            }
        }
    }
}