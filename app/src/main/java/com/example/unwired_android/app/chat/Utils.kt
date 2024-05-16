package com.example.unwired_android.app.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFrom
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.unwired_android.api.Message
import com.example.unwired_android.api.User
import com.example.unwired_android.app.misc.Base64Avatar
import kotlinx.datetime.LocalDateTime

/**
 * Composable function to display a message block.
 *
 * @param message The message to be displayed.
 * @param isAuthor A boolean indicating if the current user is the author of the message.
 */
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

/**
 * Composable element that combines the author and message.
 *
 * @param message The message to be displayed.
 * @param isAuthor A boolean indicating if the current user is the author of the message.
 * @param user A user object representing the author.
 */
@Composable
fun AuthorAndMessage(message: Message, isAuthor: Boolean, user: User?) {
    Row {
        Column(Modifier.padding(8.dp)) {
            Base64Avatar(
                user?.avatar,
                size = 48,
                //modifier = Modifier.padding(4.dp)
            )
        }

        Column {
            Row(modifier = Modifier.semantics(mergeDescendants = true) {}) {
                Text(
                    text = message.author.username,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .alignBy(LastBaseline)
                        .paddingFrom(LastBaseline, after = 8.dp) // Space to 1st bubble
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${
                        LocalDateTime.parse(message.at).time.hour.toString().padStart(2, '0')
                    }:${
                        LocalDateTime.parse(
                            message.at
                        ).time.minute.toString().padStart(2, '0')
                    }",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.alignBy(LastBaseline),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

            }
            MessageBlock(message = message, isAuthor = isAuthor)
        }

    }
}

/**
 * This function determines the text color based on the luminance of the background color.
 * It uses a simple rule: if the luminance of the color is less than 150 (on a scale of 0-255),
 * it returns white color for the text. Otherwise, it returns black.
 *
 * @param color The background color on which the text will be displayed.
 * @return The color of the text that ensures good visibility on the provided background color.
 */
fun textColorForBackground(color: Color): Color {
    return if (color.luminance() * 255 < 150) {
        Color.White
    } else {
        Color.Black
    }
}