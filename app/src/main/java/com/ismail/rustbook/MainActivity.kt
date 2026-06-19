package com.ismail.rustbook

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.ismail.rustbook.ui.AppStateProvider
import com.ismail.rustbook.ui.ResourcesOfZipFilesWithInfo
import com.ismail.rustbook.ui.activity.DownloadZip
import com.ismail.rustbook.ui.activity.DownloadZipNavigation
import com.ismail.rustbook.ui.activity.HomeActivity
import com.ismail.rustbook.ui.activity.HomeActivityNavigation
import com.ismail.rustbook.ui.activity.LanguageSelect
import com.ismail.rustbook.ui.activity.LanguageSelectNavigation
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

  val appState = AppStateProvider(context)
  
  var languageName: String? = null;
  for( dirs in context.filesDir.list()?.toList()!!){
    for( zipFilesWithInfo in ResourcesOfZipFilesWithInfo(). zipFilesWithInfo){
      if(dirs == zipFilesWithInfo["language"]){
        languageName = zipFilesWithInfo["language"].toString()
        break
      }
    }
  }
  
  val lastPage = appState.lastOpenedPage
  val defaultIndex = "$languageName/book/index.html"
  val rootIndex = lastPage ?: defaultIndex
  
  val file = File(context.filesDir, rootIndex)
  val startDestination = if (languageName != null && file.exists()) {
    HomeActivityNavigation(rootIndex = rootIndex)
  } else {
    LanguageSelectNavigation
  }

  NavHost(
    navController = navController,
    startDestination = startDestination,
  ) {
    composable<HomeActivityNavigation> {
       val rootIndex: String= it.toRoute<HomeActivityNavigation>().rootIndex
      HomeActivity(navController, rootIndex) }
    composable<LanguageSelectNavigation> {
      LanguageSelect(navController)
    }
    composable<DownloadZipNavigation> {
      val downloadZipURL = it.toRoute<DownloadZipNavigation>().downloadZipURL
      val language = it.toRoute<DownloadZipNavigation>().language
      DownloadZip(navController, downloadZipURL, language)
    }
  }
}