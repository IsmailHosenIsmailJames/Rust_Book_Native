package com.ismail.rustbook.ui.activity

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
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

  val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

  Scaffold(
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = {
      LargeTopAppBar(
        title = {
          Text(
            "Select Language",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
          )
        },
        scrollBehavior = scrollBehavior
      )
    },
    floatingActionButton = {
      ExtendedFloatingActionButton(
        onClick = {
          navController.popBackStack()
          navController.navigate(
            DownloadZipNavigation(
              zipFilesWithInfo[selectedLanguageIndex]["link"] as String,
              zipFilesWithInfo[selectedLanguageIndex]["language"] as String,
              zipFilesWithInfo[selectedLanguageIndex]["isComplete"] as Boolean
            )
          )
        },
        icon = { Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null) },
        text = { Text("Next") },
      )
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      item {
        Text(
          text = "Choose your preferred language to download the Rust Book content.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(bottom = 8.dp)
        )
      }

      items(zipFilesWithInfo.size) { index ->
        val isSelected = index == selectedLanguageIndex
        val containerColor by animateColorAsState(
          targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
          } else {
            MaterialTheme.colorScheme.surface
          },
          label = "CardSelectionColor"
        )

        OutlinedCard(
          onClick = { selectedLanguageIndex = index },
          modifier = Modifier.fillMaxWidth(),
          colors = androidx.compose.material3.CardDefaults.outlinedCardColors(
            containerColor = containerColor
          )
        ) {
          ListItem(
            headlineContent = {
              Text(
                zipFilesWithInfo[index]["language"].toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
              )
            },
            leadingContent = {
              if (isSelected) {
                Icon(
                  Icons.Filled.CheckCircle,
                  contentDescription = "Selected",
                  tint = MaterialTheme.colorScheme.primary
                )
              } else {
                Box(modifier = Modifier.size(24.dp))
              }
            },
            colors = androidx.compose.material3.ListItemDefaults.colors(
              containerColor = androidx.compose.ui.graphics.Color.Transparent
            )
          )
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