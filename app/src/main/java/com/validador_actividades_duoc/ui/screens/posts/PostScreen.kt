package com.validador_actividades_duoc.ui.screens.posts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PostScreen(viewModel: PostViewModel) {
    // Se agregó la importación de collectAsState y getValue
    val posts by viewModel.postList.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(all = 12.dp)
    ) {
        // Se corrigió de "count = posts" a simplemente "posts"
        items(posts) { post ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(all = 10.dp)
                ) {
                    Text(
                        text = post.title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = post.body,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            Spacer(modifier = Modifier.padding(20.dp))
        }
    }
}