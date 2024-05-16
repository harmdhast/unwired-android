/**
 * This file contains the ChatActivity class which is the main activity for the chat feature.
 *
 * The ChatActivity class extends the ComponentActivity class and overrides the onCreate method.
 * In the onCreate method, it retrieves the group ID from the intent extras, gets the ChatViewModel instance,
 * and retrieves the group with the given ID using a blocking coroutine.
 *
 * If the group is not null, it sets the content of the activity to the Chat composable function with the group as the argument.
 * If the group is null, it prints an error message and finishes the activity.
 *
 * The ChatActivity class is annotated with @AndroidEntryPoint, which indicates that Hilt should inject its dependencies.
 */

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