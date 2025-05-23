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
import com.ismail.rustbook.ui.ResourcesOfZipFilesWithInfo
import com.ismail.rustbook.ui.activity.DownloadZip
import com.ismail.rustbook.ui.activity.DownloadZipNavigation
import com.ismail.rustbook.ui.activity.HomeActivity
import com.ismail.rustbook.ui.activity.HomeActivityNavigation
import com.ismail.rustbook.ui.activity.LanguageSelect
import com.ismail.rustbook.ui.activity.LanguageSelectNavigation
import com.ismail.rustbook.ui.theme.RustBookNativeTheme
import java.io.File
import android.util.Log

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
  // 1. Instantiate ResourcesOfZipFilesWithInfo once
  val resources = ResourcesOfZipFilesWithInfo() // Assuming this is lightweight or memoize if not
  val knownLanguages = resources.zipFilesWithInfo.mapNotNull { it["language"]?.toString() }.toSet()

  var foundLanguageDir: String? = null
  val filesInDir = context.filesDir.list()?.toList() ?: emptyList()

  // 3. Efficiently find matching language
  for (dirName in filesInDir) {
    if (knownLanguages.contains(dirName)) {
      foundLanguageDir = dirName
      Log.d("MyAppNavHost", "Found matching language directory: '$foundLanguageDir'")
      break // Found the first matching directory
    }
  }

  val startDestination: Any // Use Any for startDestination type before casting
  if (foundLanguageDir != null) {
    val rootIndexPath = "$foundLanguageDir/book/index.html"
    val indexFile = File(context.filesDir, rootIndexPath)
    if (indexFile.exists() && indexFile.isFile) {
      Log.d("MyAppNavHost", "Index file '$rootIndexPath' exists. Navigating to HomeActivity.")
      // Ensure HomeActivityNavigation can be constructed correctly
      startDestination = HomeActivityNavigation(rootIndex = rootIndexPath)
    } else {
      // Language directory found, but index.html is missing/invalid
      Log.w("MyAppNavHost", "Language dir '$foundLanguageDir' found, but '$rootIndexPath' is missing or not a file. Navigating to language select.")
      startDestination = LanguageSelectNavigation
    }
  } else {
    // 4. Handle languageName being null
    Log.d("MyAppNavHost", "No matching language directory found in filesDir. Navigating to language select.")
    startDestination = LanguageSelectNavigation
  }

  NavHost(
    navController = navController,
    startDestination = startDestination,
  ) {
    composable<HomeActivityNavigation> { backStackEntry ->
       // Ensure the route type is correctly inferred or explicitly cast if needed
       val homeArgs = backStackEntry.toRoute<HomeActivityNavigation>()
       HomeActivity(navController, homeArgs.rootIndex)
    }
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