package com.ismail.rustbook.ui.feature.home

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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ismail.rustbook.data.local.AppPreferences
import com.ismail.rustbook.data.repository.BookRepositoryImpl
import com.ismail.rustbook.domain.usecase.GetAppSettingsUseCase
import com.ismail.rustbook.domain.usecase.UpdateAppSettingsUseCase
import com.ismail.rustbook.ui.feature.language.LanguageScreenNavigation
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class HomeActivityNavigation(
    val rootIndex: String
)

private const val TAG = "HomeScreenDebug"

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun HomeScreen(navController: NavHostController, rootIndex: String) {
    val context = LocalContext.current
    val viewModel: HomeViewModel = viewModel {
        val repository = BookRepositoryImpl(context, AppPreferences(context))
        HomeViewModel(
            getAppSettingsUseCase = GetAppSettingsUseCase(repository),
            updateAppSettingsUseCase = UpdateAppSettingsUseCase(repository),
            repository = repository,
            rootIndex = rootIndex
        )
    }

    val state by viewModel.collectAsState()
    var webView by remember { mutableStateOf<WebView?>(null) }
    var showMenu by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showFavoritesDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                HomeContract.Effect.NavigateToLanguage -> {
                    navController.navigate(LanguageScreenNavigation) {
                        popUpTo(0)
                    }
                }
            }
        }
    }

    // Handle navigation changes from state (like Go Home)
    LaunchedEffect(state.currentUrl) {
        val absolutePath = File(context.filesDir, state.currentUrl).absolutePath
        if (webView?.url != "file://$absolutePath") {
            webView?.loadUrl(absolutePath)
        }
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
                        TextField(
                            value = state.searchQuery,
                            onValueChange = { viewModel.handleIntent(HomeContract.Intent.Search(it)) },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            placeholder = { Text("Search pages...", fontSize = 14.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                if (state.searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.handleIntent(HomeContract.Intent.Search("")) }) {
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

                        IconButton(onClick = { webView?.goBack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                        IconButton(onClick = { webView?.goForward() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Forward")
                        }
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Go Home") },
                                    leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                                    onClick = {
                                        showMenu = false
                                        viewModel.handleIntent(HomeContract.Intent.GoHome)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Set as Home") },
                                    leadingIcon = { Icon(Icons.Default.AddCircle, contentDescription = null) },
                                    onClick = {
                                        showMenu = false
                                        viewModel.handleIntent(HomeContract.Intent.SetAsHome)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("All Favorites") },
                                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                                    onClick = {
                                        showMenu = false
                                        showFavoritesDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (state.isFavorite) "Remove from Favorites" else "Save to Favorites") },
                                    leadingIcon = {
                                        Icon(
                                            if (state.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                            contentDescription = null,
                                            tint = if (state.isFavorite) Color.Red else LocalContentColor.current
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        viewModel.handleIntent(HomeContract.Intent.ToggleFavorite)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("History") },
                                    leadingIcon = { Icon(Icons.Default.Menu, contentDescription = null) },
                                    onClick = {
                                        showMenu = false
                                        showHistoryDialog = true
                                    }
                                )
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text("Reset App", color = MaterialTheme.colorScheme.error) },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        viewModel.handleIntent(HomeContract.Intent.ResetApp)
                                    }
                                )
                            }
                        }
                    }

                    if (state.filteredSearchFiles.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                .heightIn(max = 300.dp)
                        ) {
                            LazyColumn {
                                items(state.filteredSearchFiles) { path ->
                                    ListItem(
                                        headlineContent = {
                                            Text(
                                                path.substringAfterLast("/"),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        },
                                        modifier = Modifier.clickable {
                                            viewModel.handleIntent(HomeContract.Intent.NavigateTo(path))
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
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
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
                                    val relativePath = it.substringAfter(context.filesDir.absolutePath + "/")
                                    viewModel.handleIntent(HomeContract.Intent.PageFinished(relativePath))
                                }
                            }

                            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                return false
                            }
                        }
                        webView = this
                        loadUrl(File(context.filesDir, state.currentUrl).absolutePath)
                    }
                }
            )
        }
    }

    if (showHistoryDialog) {
        ListDialog(
            title = "History",
            items = state.history,
            onDismiss = { showHistoryDialog = false },
            onSelect = { path ->
                viewModel.handleIntent(HomeContract.Intent.NavigateTo(path))
                showHistoryDialog = false
            }
        )
    }

    if (showFavoritesDialog) {
        ListDialog(
            title = "Favorites",
            items = state.favorites,
            onDismiss = { showFavoritesDialog = false },
            onSelect = { path ->
                viewModel.handleIntent(HomeContract.Intent.NavigateTo(path))
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
                                Text(item.substringAfterLast("/"), style = MaterialTheme.typography.bodyLarge)
                            },
                            supportingContent = {
                                Text(item, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            },
                            modifier = Modifier.clickable { onSelect(item) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
fun HomeViewModel.collectAsState(): State<HomeContract.State> {
    return state.collectAsState()
}
