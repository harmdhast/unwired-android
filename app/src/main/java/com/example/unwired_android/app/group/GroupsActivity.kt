package com.example.unwired_android.app.group

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.unwired_android.ui.theme.UnwiredandroidTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GroupsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UnwiredandroidTheme {
                GroupsMenu()
            }
        }
    }
}