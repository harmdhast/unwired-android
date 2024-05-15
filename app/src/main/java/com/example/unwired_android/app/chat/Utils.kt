package com.example.unwired_android.app.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.example.unwired_android.api.Message
import com.example.unwired_android.textColorForBackground

@Composable
fun MessageBlock(message: Message, isAuthor: Boolean) {
    val color =
        if (isAuthor) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
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