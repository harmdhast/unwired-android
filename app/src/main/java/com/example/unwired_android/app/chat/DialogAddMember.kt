package com.example.unwired_android.app.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.unwired_android.api.User
import com.example.unwired_android.app.misc.base64ToBitmap

/**
 * Composable function to display a dialog for adding a member to a chat.
 *
 * @param nonMembers A list of users who are not currently members of the chat.
 * @param onDismissRequest A function to be called when the dialog is dismissed.
 * @param onAddMember A function to be called when a user is selected to be added as a member. The selected user is passed as a parameter to this function.
 */
@Composable
fun DialogAddMember(
    nonMembers: List<User>,
    onDismissRequest: () -> Unit,
    onAddMember: (User) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var members by remember { mutableStateOf(nonMembers) }

    LaunchedEffect(searchQuery) {
        members = nonMembers.filter { it.username.contains(searchQuery, ignoreCase = true) }
    }

    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(Icons.Filled.PersonAdd, "", modifier = Modifier.padding(8.dp))
                Text(
                    "Add member",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(8.dp)
                )
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search Users") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxHeight(.5f)
                        .fillMaxWidth(.8f)
                        .padding(8.dp)
                )
                {
                    items(members) { user ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onAddMember(user)
                                    onDismissRequest()
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            base64ToBitmap(user.avatar)?.let {
                                Image(
                                    bitmap = it,
                                    contentDescription = "Avatar",
                                    contentScale = ContentScale.FillBounds,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(user.username)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                }
            }
        }

    }
}