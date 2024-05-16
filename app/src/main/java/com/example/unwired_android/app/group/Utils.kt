package com.example.unwired_android.app.group

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unwired_android.api.User
import com.example.unwired_android.app.misc.Base64Avatar
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone


/**
 * This function calculates the time difference between the current time and a given time.
 * It returns a string representation of the time difference in a human-readable format.
 *
 * @param instant The given time to calculate the difference from.
 * @param timeZone The time zone to use for the calculation. Defaults to the current system default time zone.
 * @return A string representation of the time difference in a human-readable format.
 */
fun timeAgoSince(instant: Instant, timeZone: TimeZone = TimeZone.currentSystemDefault()): String {
    val now = Clock.System.now()
    val duration = now - instant

    val seconds = duration.inWholeSeconds
    val minutes = duration.inWholeMinutes
    val hours = duration.inWholeHours
    val days = duration.inWholeDays

    return when {
        seconds < 60 -> "$seconds seconds ago"
        minutes < 60 -> "$minutes minutes ago"
        hours < 24 -> "$hours hours ago"
        else -> "$days days ago"
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