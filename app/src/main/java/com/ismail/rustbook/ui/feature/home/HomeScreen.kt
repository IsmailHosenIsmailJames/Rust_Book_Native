package com.ismail.rustbook.ui.feature.home

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
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
import com.ismail.rustbook.ui.feature.progress.ProgressScreenNavigation
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class HomeActivityNavigation(
    val rootIndex: String,
    val defaultIndex: String
)

private const val TAG = "HomeScreenDebug"

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun HomeScreen(navController: NavHostController, rootIndex: String, defaultIndex: String) {
    val context = LocalContext.current
    val viewModel: HomeViewModel = viewModel {
        val repository = BookRepositoryImpl(context, AppPreferences(context))
        HomeViewModel(
            getAppSettingsUseCase = GetAppSettingsUseCase(repository),
            updateAppSettingsUseCase = UpdateAppSettingsUseCase(repository),
            repository = repository,
            rootIndex = rootIndex,
            defaultIndex = defaultIndex
        )
    }

    val state by viewModel.state.collectAsState()
    var webView by remember { mutableStateOf<WebView?>(null) }
    var showMenu by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showFavoritesDialog by remember { mutableStateOf(false) }
    var isSearchFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val displayPath = remember(state.currentUrl) { state.currentUrl.substringAfterLast("/") }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                HomeContract.Effect.NavigateToLanguage -> {
                    navController.navigate(LanguageScreenNavigation) {
                        popUpTo(0)
                    }
                }
                HomeContract.Effect.NavigateToProgress -> {
                    navController.navigate(ProgressScreenNavigation)
                }
                is HomeContract.Effect.OpenUrl -> {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(effect.url))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to open URL: ${effect.url}", e)
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = if (isSearchFocused || state.searchQuery.isNotEmpty()) state.searchQuery else displayPath,
                        onValueChange = {
                            if (isSearchFocused) {
                                viewModel.handleIntent(HomeContract.Intent.Search(it))
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .onFocusChanged { isSearchFocused = it.isFocused },
                        textStyle = LocalTextStyle.current.copy(
                            textAlign = if (isSearchFocused || state.searchQuery.isNotEmpty()) TextAlign.Start else TextAlign.Center,
                            fontSize = 14.sp
                        ),
                        placeholder = { Text("Search pages...", fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon =
                            if(state.searchQuery.isNotEmpty() || isSearchFocused)
                            {
                                {
                                    IconButton(onClick = {
                                        viewModel.handleIntent(HomeContract.Intent.Search(""))
                                        focusManager.clearFocus()
                                    }) {
                                        Icon(Icons.Default.Close, contentDescription = null)
                                    }
                                }
                            } else null

                        ,
                        shape = RoundedCornerShape(26.dp),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
                    )

                    if (!isSearchFocused && state.searchQuery.isEmpty()) {
                        if (state.isCompleted) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Completed",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }

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
                                    text = { Text("My Progress") },
                                    leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
                                    onClick = {
                                        showMenu = false
                                        viewModel.handleIntent(HomeContract.Intent.NavigateToProgress)
                                    }
                                )
                                HorizontalDivider()
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
                                    text = { Text(if (state.isCompleted) "Remove Completion" else "Mark as Complete") },
                                    leadingIcon = {
                                        Icon(
                                            if (state.isCompleted) Icons.Default.CheckCircle else Icons.Default.AddCircle,
                                            contentDescription = null,
                                            tint = if (state.isCompleted) Color(0xFF4CAF50) else LocalContentColor.current
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        viewModel.handleIntent(HomeContract.Intent.ToggleCompletion)
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
                                    text = { Text("Star on GitHub") },
                                    leadingIcon = { Icon(Icons.Default.ThumbUp, contentDescription = null) },
                                    onClick = {
                                        showMenu = false
                                        viewModel.handleIntent(HomeContract.Intent.StarOnGitHub)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Rate App") },
                                    leadingIcon = { Icon(Icons.Default.Star, contentDescription = null) },
                                    onClick = {
                                        showMenu = false
                                        viewModel.handleIntent(HomeContract.Intent.RateApp)
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
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()) {
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
                                if (url != null && url.startsWith("file://")) {
                                    val relativePath = url.substringAfter(context.filesDir.absolutePath + "/")
                                    viewModel.handleIntent(HomeContract.Intent.PageFinished(relativePath))
                                }
                            }

                            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                val url = request?.url?.toString() ?: return false
                                if (!url.startsWith("file://") && !url.startsWith("about:blank")) {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Log.e(TAG, "Failed to open external link: $url", e)
                                    }
                                    return true
                                }
                                return false
                            }
                        }
                        webView = this
                        loadUrl(File(context.filesDir, state.currentUrl).absolutePath)
                    }
                }
            )

            AnimatedVisibility(
                visible = state.filteredSearchFiles.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                        .clickable { viewModel.handleIntent(HomeContract.Intent.Search("")) }
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .align(Alignment.TopCenter),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                            items(state.filteredSearchFiles) { path ->
                                ListItem(
                                    headlineContent = {
                                        Text(
                                            path.substringAfterLast("/"),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    supportingContent = {
                                        Text(
                                            path,
                                            style = MaterialTheme.typography.bodySmall,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    modifier = Modifier.clickable {
                                        viewModel.handleIntent(HomeContract.Intent.NavigateTo(path))
                                        focusManager.clearFocus()
                                    }
                                )
                            }
                        }
                    }
                }
            }
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
