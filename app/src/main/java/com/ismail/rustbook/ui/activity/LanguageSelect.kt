package com.ismail.rustbook.ui.activity

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.ismail.rustbook.ui.ResourcesOfZipFilesWithInfo
import com.ismail.rustbook.ui.theme.RustBookNativeTheme
import kotlinx.serialization.Serializable


@Serializable
object LanguageSelectNavigation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelect(navController: NavHostController) {
  val zipFilesWithInfo = ResourcesOfZipFilesWithInfo().zipFilesWithInfo
  var selectedLanguageIndex by remember { mutableIntStateOf(2) }
  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            "Select Language",
            fontSize = 24.sp,
            fontWeight = FontWeight.W500,
            modifier = Modifier.padding(20.dp)
          )
        }
      )
    },
    floatingActionButton = {
      FloatingActionButton(onClick = {
        navController.popBackStack()
        navController.navigate(
          DownloadZipNavigation(
            zipFilesWithInfo[selectedLanguageIndex]["link"] as String,
            zipFilesWithInfo[selectedLanguageIndex]["language"] as String,
            zipFilesWithInfo[selectedLanguageIndex]["isComplete"] as Boolean
          )
        )
      }) {
        Row(modifier = Modifier.padding(start = 10.dp, end = 10.dp)) {
          Text("Next", fontSize = 16.sp, fontWeight = FontWeight.W500)
          Spacer(modifier = Modifier.width(10.dp))
          Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Arrow Forward")
        }
      }
    }
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(bottom = 50.dp, start = 20.dp, end = 20.dp)
        ) {

          items(zipFilesWithInfo.size) {
            Row(
              horizontalArrangement = Arrangement.Start,
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clickable(onClick = {
                  selectedLanguageIndex = it
                })
                .padding(start = 10.dp, end = 10.dp)
            ) {
              Box(modifier = Modifier.size(30.dp, 30.dp)) {
                if (it == selectedLanguageIndex) Icon(
                  Icons.Filled.CheckCircle,
                  contentDescription = "Selected",
                  tint = Color(0xFF2196F3)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                zipFilesWithInfo[it]["language"].toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.W500
              )
            }
          }
        }
      }

    }
  }
}


@Preview
@Composable
fun LanguageSelectPreview() {
  RustBookNativeTheme {
    LanguageSelect(NavHostController(LocalContext.current))
  }
}