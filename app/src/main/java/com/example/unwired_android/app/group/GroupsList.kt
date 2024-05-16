package com.example.unwired_android.app.group

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.unwired_android.viewmodels.GroupViewModel

@Composable
fun GroupsList() {
    val groupViewModel: GroupViewModel = hiltViewModel()
    val groups by remember {
        groupViewModel.groups
    }.observeAsState()

    LaunchedEffect(Unit) {
        groupViewModel.getGroups()
    }

    groups?.let {
        LazyColumn(modifier = Modifier.fillMaxHeight()) {
            items(it, key = { it.id }) { group ->
                GroupsListItem(group = group)
            }
        }
    }
}
