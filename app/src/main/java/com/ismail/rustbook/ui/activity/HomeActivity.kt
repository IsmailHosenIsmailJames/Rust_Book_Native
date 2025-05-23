package com.ismail.rustbook.ui.activity

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import com.ismail.rustbook.data.database.entities.LastPageState
import com.ismail.rustbook.data.database.dao.LastPageStateDao
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.ismail.rustbook.data.database.AppDatabase
import com.ismail.rustbook.data.database.entities.Bookmark
import com.ismail.rustbook.data.database.entities.UserPreference
import com.ismail.rustbook.ui.features.bookmarks.BookmarkListScreen
import androidx.navigation.NavHostController
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class HomeActivityNavigation(
  var rootIndex: String
)

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun HomeActivity(navController: NavHostController, rootIndex: String) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val db = remember { AppDatabase.getDatabase(context) }
  val lastPageStateDao = remember { db.lastPageStateDao() }
  val bookmarkDao = remember { db.bookmarkDao() }
  val userPreferenceDao = remember { db.userPreferenceDao() }

  var webViewUrl by remember { mutableStateOf<String?>(null) }
  val currentScrollY = remember { mutableStateOf(0) }
  val currentScrollX = remember { mutableStateOf(0) }
  val currentPageTitle = remember { mutableStateOf("") }
  val initialFile = remember(context, rootIndex) { File(context.filesDir, rootIndex) }
  var webViewInstance by remember { mutableStateOf<WebView?>(null) }
  var canGoBack by remember { mutableStateOf(false) }
  var canGoForward by remember { mutableStateOf(false) }

  var isSearchActive by remember { mutableStateOf(false) }
  var searchQuery by remember { mutableStateOf("") }
  var searchResults by remember { mutableStateOf<List<File>>(emptyList()) }
  val currentLanguage = remember(rootIndex) { rootIndex.substringBefore('/') }
  var showMenu by remember { mutableStateOf(false) }
  var showBookmarkScreen by remember { mutableStateOf(false) }

  val updateNavigationButtonStates = {
    canGoBack = webViewInstance?.canGoBack() ?: false
    canGoForward = webViewInstance?.canGoForward() ?: false
    Log.d("HomeActivity", "Updated Nav States: Back=$canGoBack, Forward=$canGoForward")
  }

  LaunchedEffect(searchQuery, isSearchActive, currentLanguage, context.filesDir) {
    if (isSearchActive && searchQuery.isNotBlank()) {
      withContext(Dispatchers.IO) {
        val langDir = File(context.filesDir, currentLanguage)
        if (langDir.exists() && langDir.isDirectory) {
          try {
                // Combine filters: find HTML/HTM files that also contain the searchQuery in their name.
                searchResults = langDir.walkTopDown()
                    .filter { file ->
                        file.isFile &&
                        (file.name.endsWith(".html", ignoreCase = true) || file.name.endsWith(".htm", ignoreCase = true)) &&
                        file.name.contains(searchQuery, ignoreCase = true)
                    }
                    .toList() // Collect the results into a list
                Log.d("HomeActivity", "Search results for '$searchQuery': ${searchResults.size} items.")
          } catch (e: Exception) {
            Log.e("HomeActivity", "Error during file search: ", e)
            searchResults = emptyList()
          }
        } else {
          Log.w("HomeActivity", "Language directory not found: ${langDir.absolutePath}")
          searchResults = emptyList()
        }
      }
    } else {
      searchResults = emptyList()
      if (isSearchActive && searchQuery.isBlank()) {
          Log.d("HomeActivity", "Search query is blank, clearing results.")
      }
    }
  }

  LaunchedEffect(lastPageStateDao, initialFile) {
    val lastState = withContext(Dispatchers.IO) {
        lastPageStateDao.get()
    }
    if (lastState != null && lastState.url.isNotBlank()) {
        val stateFile = File(lastState.url)
        if (stateFile.exists() && stateFile.isFile && stateFile.canRead()) {
            webViewUrl = stateFile.absolutePath
            Log.d("HomeActivity", "Loading last state URL: ${stateFile.absolutePath}, ScrollX: ${lastState.scrollX}, ScrollY: ${lastState.scrollY}")
        } else {
            Log.w("HomeActivity", "Last state URL file non-existent/unreadable: ${lastState.url}. Loading default.")
            webViewUrl = initialFile.absolutePath
        }
    } else {
        webViewUrl = initialFile.absolutePath
        Log.d("HomeActivity", "No last state found or URL blank, loading default: ${initialFile.absolutePath}")
    }
  }

  DisposableEffect(Unit) {
    onDispose {
      val urlToSave = webViewInstance?.url
      val scrollXToSave = currentScrollX.value
      val scrollYToSave = currentScrollY.value

      Log.d("HomeActivity", "DisposableEffect onDispose: Triggered. URL='$urlToSave', ScrollX (from state)=${scrollXToSave}, ScrollY (from state)=${scrollYToSave}")

      if (urlToSave != null && urlToSave != "about:blank" && webViewInstance != null) {
        Log.d("HomeActivity", "onDispose: Preparing to save state. ScrollX=$scrollXToSave, ScrollY=$scrollYToSave.")
        coroutineScope.launch(Dispatchers.IO) {
          saveCurrentState(lastPageStateDao, urlToSave, scrollXToSave, scrollYToSave, context)
        }
      } else {
        Log.d("HomeActivity", "onDispose: Not saving state. URL is null/about:blank or webViewInstance is null. URL='$urlToSave', webViewInstance null?=${webViewInstance == null}")
      }
    }
  }

  Scaffold(
    topBar = {
      if (isSearchActive) {
        TopAppBar(
          title = {
            TextField(
              value = searchQuery,
              onValueChange = { searchQuery = it },
              placeholder = { Text("Search HTML files...") },
              modifier = Modifier.fillMaxWidth(),
              singleLine = true
            )
          },
          navigationIcon = {
            IconButton(onClick = {
              isSearchActive = false
              searchQuery = ""
              // searchResults will be cleared by LaunchedEffect
            }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Close Search") }
          }
        )
      } else {
        TopAppBar(
          title = { Text(currentPageTitle.value.ifBlank { "Rust Book" }) },
          navigationIcon = {
            if (canGoBack) {
              IconButton(onClick = {
                webViewInstance?.goBack()
                updateNavigationButtonStates()
              }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
              }
            }
          },
          actions = {
            IconButton(
              onClick = {
                webViewInstance?.goForward()
                updateNavigationButtonStates()
              },
              enabled = canGoForward
            ) {
              Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Forward")
            }
            IconButton(onClick = {
              coroutineScope.launch {
                val preferredHome = withContext(Dispatchers.IO) {
                  userPreferenceDao.getByKey("home_page_url")
                }
                var loadSuccess = false
                if (preferredHome != null && preferredHome.value.isNotBlank()) {
                  val preferredFile = File(preferredHome.value)
                  if (preferredFile.exists() && preferredFile.isFile && preferredFile.canRead()) {
                    webViewUrl = preferredFile.absolutePath
                    Log.d("HomeActivity", "Home button: Loading preferred URL: ${preferredFile.absolutePath}")
                    loadSuccess = true
                  } else {
                    Log.w("HomeActivity", "Home button: Preferred URL file not found or not readable: ${preferredHome.value}")
                  }
                }

                if (!loadSuccess) {
                  val defaultFile = File(context.filesDir, rootIndex)
                  webViewUrl = defaultFile.absolutePath
                  Log.d("HomeActivity", "Home button: Loading default URL: ${defaultFile.absolutePath}")
                }
              }
            }) {
              Icon(Icons.Filled.Home, contentDescription = "Home")
            }
            IconButton(onClick = { isSearchActive = true }) {
              Icon(Icons.Filled.Search, "Search")
            }
            Box {
              IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Filled.MoreVert, contentDescription = "More options")
              }
              DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
              ) {
                DropdownMenuItem(
                  text = { Text("Bookmark this page") },
                  onClick = {
                    val url = webViewInstance?.url
                    val title = currentPageTitle.value.ifBlank {
                      url?.substringAfterLast('/')?.substringBeforeLast('.') ?: "Unknown Page"
                    }
                    if (url != null && url.startsWith("file://") && url.contains(context.filesDir.path)) {
                      val fileForUrl = File(url.substring("file://".length))
                      if (fileForUrl.exists() && fileForUrl.isFile) {
                        coroutineScope.launch(Dispatchers.IO) {
                          val newBookmark = Bookmark(url = fileForUrl.absolutePath, title = title, timestamp = System.currentTimeMillis())
                          val resultId = bookmarkDao.insert(newBookmark)
                          withContext(Dispatchers.Main) {
                            if (resultId != -1L) {
                              Toast.makeText(context, "Bookmarked: $title", Toast.LENGTH_SHORT).show()
                            } else {
                              Toast.makeText(context, "Already bookmarked or error.", Toast.LENGTH_SHORT).show()
                            }
                          }
                        }
                      } else {
                        Toast.makeText(context, "Page file does not exist.", Toast.LENGTH_SHORT).show()
                      }
                    } else {
                      Toast.makeText(context, "Cannot bookmark this page.", Toast.LENGTH_SHORT).show()
                    }
                    showMenu = false
                  }
                )
                DropdownMenuItem(
                  text = { Text("Set this page as home") },
                  onClick = {
                    val url = webViewInstance?.url
                    if (url != null && url.startsWith("file://") && url.contains(context.filesDir.path)) {
                       val fileForUrl = File(url.substring("file://".length))
                       if (fileForUrl.exists() && fileForUrl.isFile) {
                         coroutineScope.launch(Dispatchers.IO) {
                           userPreferenceDao.insertOrUpdate(UserPreference("home_page_url", fileForUrl.absolutePath))
                           withContext(Dispatchers.Main) {
                             Toast.makeText(context, "Home page set!", Toast.LENGTH_SHORT).show()
                           }
                         }
                       } else {
                          Toast.makeText(context, "Home page file does not exist.", Toast.LENGTH_SHORT).show()
                       }
                    } else {
                      Toast.makeText(context, "Cannot set this page as home.", Toast.LENGTH_SHORT).show()
                    }
                    showMenu = false
                  }
                )
                DropdownMenuItem(
                  text = { Text("View Bookmarks") },
                  onClick = {
                    showBookmarkScreen = true
                    showMenu = false
                    Log.d("HomeActivity", "View Bookmarks menu item clicked.")
                  }
                )
              }
            }
          }
        )
      }
    }
  ){ paddingValues ->
    Box(
      modifier = Modifier
        .padding(paddingValues)
        .fillMaxSize()
    ) {
      // WebView is always rendered, search results overlay it
      AndroidView(
        factory = { ctx ->
          WebView(ctx).apply {
            webViewInstance = this
            settings.javaScriptEnabled = true
            settings.allowFileAccess = true
            settings.allowContentAccess = true
            settings.domStorageEnabled = true

            // Setup Javascript Interface
            val webAppInterface = WebAppInterface(context, currentScrollY, currentScrollX, currentPageTitle)
            addJavascriptInterface(webAppInterface, "AndroidInterface")

            webViewClient = object : WebViewClient() {
              override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Log.d("HomeActivity", "onPageFinished for URL: $url. Requesting scroll/title from JS.")
                // These JS calls are asynchronous in nature regarding when WebAppInterface methods will be hit.
                view?.loadUrl("javascript:AndroidInterface.reportScrollPosition(window.pageYOffset, window.pageXOffset);")
                view?.loadUrl("javascript:AndroidInterface.reportPageTitle(document.title);")

                val loadedUrl = view?.url
                if (loadedUrl != null && loadedUrl != "about:blank") {
                    // Add a small delay to allow JS interface to potentially update state.
                    // This is a temporary diagnostic measure.
                    coroutineScope.launch {
                        // Delaying slightly to see if it affects the values read from state.
                        // NOTE: This delay is for diagnostics. A robust solution would not rely on arbitrary delays.
                        kotlinx.coroutines.delay(100) // e.g., 100ms delay

                        val scrollXToSave = currentScrollX.value
                        val scrollYToSave = currentScrollY.value
                        Log.d("HomeActivity", "onPageFinished: Preparing to save state for URL '$loadedUrl'. ScrollX=$scrollXToSave, ScrollY=$scrollYToSave (values read from Composable state after JS calls and a small delay).")
                        withContext(Dispatchers.IO) {
                            saveCurrentState(lastPageStateDao, loadedUrl, scrollXToSave, scrollYToSave, context)
                        }
                    }
                }
                updateNavigationButtonStates()
              }
            }

            if (!webViewUrl.isNullOrBlank()) {
              loadUrl(webViewUrl)
              Log.d("HomeActivity", "WebView factory: Initial URL loaded: $webViewUrl")
            } else {
              Log.d("HomeActivity", "WebView factory: Initial URL is null or blank.")
            }
          }
        },
        update = { wv ->
          webViewInstance = wv // Update instance on re-composition if webview is re-created by key change
          val newUrlToLoad = webViewUrl // Use the state variable
          val currentWebViewInternalUrl = wv.url // Get current URL from WebView

          Log.d("HomeActivity", "WebView update: Desired URL='${newUrlToLoad}', Current WebView URL='${currentWebViewInternalUrl}'")

          // Condition: new URL must be non-null, not blank, and different from WebView's current URL.
          if (newUrlToLoad != null && newUrlToLoad.isNotBlank() && newUrlToLoad != currentWebViewInternalUrl) {
            wv.loadUrl(newUrlToLoad)
            Log.d("HomeActivity", "WebView update: Loading new URL: '${newUrlToLoad}'")
          } else if (newUrlToLoad == currentWebViewInternalUrl) {
            // Log if the URL is already the current one; no action needed.
            Log.d("HomeActivity", "WebView update: Desired URL is the same as current. No action.")
          } else {
            // Log if the new URL is null or blank; no action taken.
            // This branch covers (newUrlToLoad == null || newUrlToLoad.isBlank())
            Log.d("HomeActivity", "WebView update: Desired URL is null or blank. No action. Current URL remains '${currentWebViewInternalUrl}'.")
          }
        },
        modifier = Modifier.fillMaxSize()
      )

      if (isSearchActive && searchResults.isNotEmpty()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.95f))
        ) {
          items(searchResults, key = { it.absolutePath }) { file ->
            Text(
              text = file.name,
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  webViewUrl = file.absolutePath
                  isSearchActive = false
                  searchQuery = ""
                  Log.d("HomeActivity", "Search result clicked: ${file.absolutePath}")
                }
                .padding(16.dp)
            )
          }
        }
      }

      if (showBookmarkScreen) {
        BookmarkListScreen(
            bookmarkDao = bookmarkDao,
            onBookmarkClick = { bookmark ->
                val bookmarkedFile = File(bookmark.url)
                if (bookmarkedFile.exists() && bookmarkedFile.isFile) {
                    webViewUrl = bookmarkedFile.absolutePath
                    Log.d("HomeActivity", "Loading bookmarked URL: ${bookmarkedFile.absolutePath}")
                } else {
                    Log.w("HomeActivity", "Bookmarked file not found: ${bookmark.url}")
                    Toast.makeText(context, "Bookmarked file not found.", Toast.LENGTH_SHORT).show()
                }
                showBookmarkScreen = false
            },
            onDismissRequest = {
                showBookmarkScreen = false
            },
            modifier = Modifier.fillMaxSize() // Ensure it covers the screen
        )
      }
    }
  }
}

private suspend fun saveCurrentState(
    dao: LastPageStateDao,
    url: String?,
    scrollX: Int,
    scrollY: Int,
    appContext: Context // Use a more specific context if possible, e.g. appContext
) {
    if (url.isNullOrBlank() || url == "about:blank") {
        Log.d("HomeActivity", "Not saving state, URL is null, blank, or about:blank.")
        return
    }

    // Ensure the URL is a local file URL and within the app's files directory for security/relevance
    if (!url.startsWith("file://")) {
        Log.d("HomeActivity", "Not saving state, URL is not a local file: $url")
        return
    }
    
    val filePath = url.substring("file://".length)
    val file = File(filePath)
    
    // Check if the file is within the app's filesDir. This is a basic check.
    // A more robust check might involve canonical paths if symlinks are a concern.
    if (!file.absolutePath.startsWith(appContext.filesDir.absolutePath)) {
        Log.w("HomeActivity", "Not saving state, URL is not within app's files directory: $url")
        return
    }

    Log.d("HomeActivity", "Saving state: URL=$url, ScrollX=$scrollX, ScrollY=$scrollY")
    // The URL stored should be the absolute path for consistency with loading logic
    val newState = LastPageState(id = 0, url = file.absolutePath, scrollX = scrollX, scrollY = scrollY)
    dao.insertOrUpdate(newState)
}


// Inner class for JavaScript Interface
class WebAppInterface(
    private val context: Context,
    private val scrollY: MutableState<Int>,
    private val scrollX: MutableState<Int>,
    private val title: MutableState<String>
) {
    @JavascriptInterface
    fun reportScrollPosition(y: Int, x: Int) {
        Log.d("WebAppInterface", "JS -> Native: reportScrollPosition received: Y=$y, X=$x. Current state before update: scrollY=${scrollY.value}, scrollX=${scrollX.value}")
        scrollY.value = y
        scrollX.value = x
        Log.d("WebAppInterface", "JS -> Native: reportScrollPosition updated state to: scrollY=${scrollY.value}, scrollX=${scrollX.value}")
    }

    @JavascriptInterface
    fun reportPageTitle(documentTitle: String) {
        Log.d("WebAppInterface", "JS -> Native: reportPageTitle received: $documentTitle. Current state before update: title='${title.value}'")
        title.value = documentTitle
        Log.d("WebAppInterface", "JS -> Native: reportPageTitle updated state to: title='${title.value}'")
    }
}