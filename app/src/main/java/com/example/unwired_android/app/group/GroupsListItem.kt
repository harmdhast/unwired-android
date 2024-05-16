package com.example.unwired_android.app.group

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.unwired_android.api.Group
import com.example.unwired_android.app.chat.ChatActivity
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

@Composable
fun GroupsListItem(group: Group) {
    val activity = LocalContext.current as Activity
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val intent = Intent(activity, ChatActivity::class.java)
                intent.putExtra("groupId", group.id)
                activity.startActivity(intent)
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .weight(1f)
        ) {
            Text(
                text = group.name,
                style = TextStyle(fontWeight = FontWeight.Bold),
                overflow = TextOverflow.Ellipsis
            )
            if (group.lastMessage == null) {
                Text(text = "No message", style = TextStyle(color = Color.Gray))
            } else {
                Text(
                    text = "${group.lastMessage.author.username}: ${group.lastMessage.content}",
                    style = TextStyle(color = Color.Gray),
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(end = 8.dp),
                    maxLines = 1
                )
            }
        }
        if (group.lastMessage != null) {
            Text(
                text = timeAgoSince(
                    LocalDateTime.parse(group.lastMessage.at)
                        .toInstant(TimeZone.UTC)
                ),
                style = TextStyle(color = Color.Gray),
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(.5f)
                    .padding(16.dp)
            )
        }
    }
}