package com.rust_book.example

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.rust_book.example.data.local.AppPreferences
import com.rust_book.example.data.repository.LanguageRepositoryImpl
import com.rust_book.example.ui.feature.home.HomeScreen
import com.rust_book.example.ui.feature.home.HomeActivityNavigation
import com.rust_book.example.ui.feature.download.DownloadScreen
import com.rust_book.example.ui.feature.download.DownloadScreenNavigation
import com.rust_book.example.ui.feature.language.LanguageScreen
import com.rust_book.example.ui.feature.language.LanguageScreenNavigation
import com.rust_book.example.ui.feature.progress.ProgressScreen
import com.rust_book.example.ui.feature.progress.ProgressScreenNavigation
import com.rust_book.example.ui.theme.RustBookNativeTheme
import java.io.File

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      RustBookNativeTheme {
        MyAppNavHost(context = this)
      }
    }
  }
}


@Composable
fun MyAppNavHost(
  context: Context,
  navController: NavHostController = rememberNavController(),
) {
  val appPreferences = remember { AppPreferences(context) }
  val languageRepository = remember { LanguageRepositoryImpl() }

  val languages = languageRepository.getLanguages()
  val downloadedLanguage = languages.find { language ->
    File(context.filesDir, language.name).exists()
  }
  val languageName = downloadedLanguage?.name

  val lastPage = appPreferences.lastOpenedPage
    ?.takeIf { !it.startsWith("http") && !it.startsWith("https") && !it.startsWith("file://") }
  val defaultIndex = "$languageName/book/index.html"
  val rootIndex = lastPage ?: defaultIndex

  val file = File(context.filesDir, rootIndex)
  val startDestination = if (languageName != null && file.exists()) {
    HomeActivityNavigation(rootIndex = rootIndex, defaultIndex = defaultIndex)
  } else {
    LanguageScreenNavigation
  }

  NavHost(
    navController = navController,
    startDestination = startDestination,
  ) {
    composable<HomeActivityNavigation> {
      val nav = it.toRoute<HomeActivityNavigation>()
      HomeScreen(navController, nav.rootIndex, nav.defaultIndex)
    }
    composable<LanguageScreenNavigation> {
      LanguageScreen(navController)
    }
    composable<DownloadScreenNavigation> {
      val downloadUrl = it.toRoute<DownloadScreenNavigation>().downloadUrl
      val language = it.toRoute<DownloadScreenNavigation>().language
      DownloadScreen(navController, downloadUrl, language)
    }
    composable<ProgressScreenNavigation> {
      ProgressScreen(navController)
    }
  }
}
