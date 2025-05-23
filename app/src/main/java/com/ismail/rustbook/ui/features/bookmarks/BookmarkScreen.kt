package com.ismail.rustbook.ui.features.bookmarks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.ismail.rustbook.data.database.dao.BookmarkDao
import com.ismail.rustbook.data.database.entities.Bookmark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarkListScreen(
    bookmarkDao: BookmarkDao,
    onBookmarkClick: (Bookmark) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bookmarksState: State<List<Bookmark>> = bookmarkDao.getAll().collectAsState(initial = emptyList())
    val bookmarks = bookmarksState.value

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bookmarks") },
                navigationIcon = {
                    IconButton(onClick = onDismissRequest) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Close Bookmarks"
                        )
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize() // Apply fillMaxSize to Scaffold itself
    ) { paddingValues ->
        if (bookmarks.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No bookmarks yet.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues) // Apply padding from Scaffold
                    .fillMaxSize()
            ) {
                items(bookmarks, key = { it.id }) { bookmark -> // Added key for better performance
                    ListItem(
                        headlineContent = {
                            Text(
                                bookmark.title,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        supportingContent = {
                            Text(
                                bookmark.url,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        modifier = Modifier.clickable { onBookmarkClick(bookmark) }
                    )
                    Divider()
                }
            }
        }
    }
}
