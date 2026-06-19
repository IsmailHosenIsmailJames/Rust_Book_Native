package com.ismail.rustbook

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
import com.ismail.rustbook.data.local.AppPreferences
import com.ismail.rustbook.data.repository.LanguageRepositoryImpl
import com.ismail.rustbook.ui.feature.home.HomeScreen
import com.ismail.rustbook.ui.feature.home.HomeActivityNavigation
import com.ismail.rustbook.ui.feature.download.DownloadScreen
import com.ismail.rustbook.ui.feature.download.DownloadScreenNavigation
import com.ismail.rustbook.ui.feature.language.LanguageScreen
import com.ismail.rustbook.ui.feature.language.LanguageScreenNavigation
import com.ismail.rustbook.ui.theme.RustBookNativeTheme
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
  val defaultIndex = "$languageName/book/index.html"
  val rootIndex = lastPage ?: defaultIndex

  val file = File(context.filesDir, rootIndex)
  val startDestination = if (languageName != null && file.exists()) {
    HomeActivityNavigation(rootIndex = rootIndex)
  } else {
    LanguageScreenNavigation
  }

  NavHost(
    navController = navController,
    startDestination = startDestination,
  ) {
    composable<HomeActivityNavigation> {
      val rootIndex = it.toRoute<HomeActivityNavigation>().rootIndex
      HomeScreen(navController, rootIndex)
    }
    composable<LanguageScreenNavigation> {
      LanguageScreen(navController)
    }
    composable<DownloadScreenNavigation> {
      val downloadUrl = it.toRoute<DownloadScreenNavigation>().downloadUrl
      val language = it.toRoute<DownloadScreenNavigation>().language
      DownloadScreen(navController, downloadUrl, language)
    }
  }
}
