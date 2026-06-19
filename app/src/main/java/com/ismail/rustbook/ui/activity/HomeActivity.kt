package com.ismail.rustbook.ui.activity

import android.annotation.SuppressLint
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import com.ismail.rustbook.ui.AppStateProvider
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class HomeActivityNavigation(
    var rootIndex: String
)

private const val TAG = "HomeActivityDebug"

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun HomeActivity(navController: NavHostController, rootIndex: String) {
    Log.d(TAG, "HomeActivity launched with rootIndex: $rootIndex")
    val context = LocalContext.current
    val appState = remember { AppStateProvider(context) }
    var webView by remember { mutableStateOf<WebView?>(null) }
    var currentUrl by remember { mutableStateOf(rootIndex) }
    var searchQuery by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showFavoritesDialog by remember { mutableStateOf(false) }

    // Extract base directory for file search (e.g., "English/book/")
    val baseDir = remember(rootIndex) {
        rootIndex.substringBeforeLast("/") + "/"
    }

    // Get all HTML files for search
    val allHtmlFiles = remember(baseDir) {
        val dir = File(context.filesDir, baseDir)
        dir.walkTopDown()
            .filter { it.extension == "html" }
            .map { it.absolutePath.substringAfter(context.filesDir.absolutePath + "/") }
            .toList()
    }

    val filteredFiles = remember(searchQuery, allHtmlFiles) {
        if (searchQuery.isBlank()) emptyList()
        else allHtmlFiles.filter { it.contains(searchQuery, ignoreCase = true) }.take(15)
    }

    Scaffold(
        topBar = {
            Surface(
                shadowElevation = 4.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Search Bar
                        TextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                                Log.d(TAG, "Search query changed: $it")
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            placeholder = { Text("Search pages...", fontSize = 14.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = null)
                                    }
                                }
                            },
                            shape = RoundedCornerShape(26.dp),
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        IconButton(onClick = {
                            Log.d(TAG, "Navigation: Go Back clicked")
                            if (webView != null) {
                                try {
                                    webView!!.goBack()
                                } catch (e: Exception) {
                                    Log.d(TAG, "WebView: ${e.toString()}")
                                }
                            } else {
                                Log.d(TAG, "WebView: Null")
                            }
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                        IconButton(onClick = {
                            Log.d(TAG, "Navigation: Go Forward clicked")
                            webView?.goForward()
                        }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Forward"
                            )
                        }
                        Box {
                            IconButton(onClick = {
                                Log.d(TAG, "Menu: More Options clicked")
                                showMenu = true
                            }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Go Home") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Home,
                                            contentDescription = null
                                        )
                                    },
                                    onClick = {
                                        Log.d(TAG, "Action: Go Home clicked")
                                        showMenu = false
                                        val home = appState.homePage ?: rootIndex
                                        webView?.loadUrl(File(context.filesDir, home).absolutePath)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Set as Home") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.AddCircle,
                                            contentDescription = null
                                        )
                                    },
                                    onClick = {
                                        Log.d(TAG, "Action: Set as Home clicked for: $currentUrl")
                                        showMenu = false
                                        appState.homePage = currentUrl
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("All Favorites") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.AutoMirrored.Filled.List,
                                            contentDescription = null
                                        )
                                    },
                                    onClick = {
                                        Log.d(TAG, "Action: Show All Favorites clicked")
                                        showMenu = false
                                        showFavoritesDialog = true
                                    }
                                )
                                val isFav = appState.isFavorite(currentUrl)
                                DropdownMenuItem(
                                    text = { Text(if (isFav) "Remove from Favorites" else "Save to Favorites") },
                                    leadingIcon = {
                                        Icon(
                                            if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                            contentDescription = null,
                                            tint = if (isFav) Color.Red else LocalContentColor.current
                                        )
                                    },
                                    onClick = {
                                        Log.d(
                                            TAG,
                                            "Action: Toggle Favorite clicked for: $currentUrl (Was favorite: $isFav)"
                                        )
                                        showMenu = false
                                        appState.toggleFavorite(currentUrl)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("History") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Menu,
                                            contentDescription = null
                                        )
                                    },
                                    onClick = {
                                        Log.d(TAG, "Action: Show History clicked")
                                        showMenu = false
                                        showHistoryDialog = true
                                    }
                                )
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "Reset App",
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    },
                                    onClick = {
                                        Log.d(TAG, "Action: Reset App clicked")
                                        showMenu = false
                                        appState.resetAll()
                                        navController.navigate(LanguageSelectNavigation) {
                                            popUpTo(0)
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // Search Results Overlay
                    if (filteredFiles.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(8.dp)
                                )
                                .heightIn(max = 300.dp)
                        ) {
                            LazyColumn {
                                items(filteredFiles) { path ->
                                    ListItem(
                                        headlineContent = {
                                            Text(
                                                path.substringAfterLast("/"),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        },
                                        modifier = Modifier.clickable {
                                            Log.d(TAG, "Action: Search result selected: $path")
                                            searchQuery = ""
                                            webView?.loadUrl(
                                                File(
                                                    context.filesDir,
                                                    path
                                                ).absolutePath
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        settings.javaScriptEnabled = true
                        settings.allowFileAccess = true
                        settings.allowContentAccess = true
                        settings.domStorageEnabled = true

                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                url?.let {
                                    val relativePath =
                                        it.substringAfter(context.filesDir.absolutePath + "/")
                                    Log.d(TAG, "WebView: Page finished loading: $relativePath")
                                    currentUrl = relativePath
                                    appState.lastOpenedPage = relativePath
                                    appState.addToHistory(relativePath)
                                }
                            }

                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean {
                                return false
                            }
                        }
                        webView = this
                        loadUrl(File(context.filesDir, rootIndex).absolutePath)
                    }
                },
                update = {
                    // No-op for now as logic is handled in onPageFinished
                }
            )
        }
    }

    // Dialogs
    if (showHistoryDialog) {
        ListDialog(
            title = "History",
            items = appState.history,
            onDismiss = {
                Log.d(TAG, "Dialog: History closed")
                showHistoryDialog = false
            },
            onSelect = { path ->
                Log.d(TAG, "Dialog: History item selected: $path")
                webView?.loadUrl(File(context.filesDir, path).absolutePath)
                showHistoryDialog = false
            }
        )
    }

    if (showFavoritesDialog) {
        ListDialog(
            title = "Favorites",
            items = appState.favorites.toList(),
            onDismiss = {
                Log.d(TAG, "Dialog: Favorites closed")
                showFavoritesDialog = false
            },
            onSelect = { path ->
                Log.d(TAG, "Dialog: Favorite item selected: $path")
                webView?.loadUrl(File(context.filesDir, path).absolutePath)
                showFavoritesDialog = false
            }
        )
    }
}

@Composable
fun ListDialog(
    title: String,
    items: List<String>,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            if (items.isEmpty()) {
                Text("No items found.")
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                    items(items) { item ->
                        ListItem(
                            headlineContent = {
                                Text(
                                    item.substringAfterLast("/"),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            supportingContent = {
                                Text(
                                    item,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            modifier = Modifier.clickable { onSelect(item) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
