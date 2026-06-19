package com.ismail.rustbook.ui.feature.download

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ismail.rustbook.data.local.AppPreferences
import com.ismail.rustbook.data.repository.BookRepositoryImpl
import com.ismail.rustbook.domain.usecase.DownloadBookUseCase
import com.ismail.rustbook.ui.feature.home.HomeActivityNavigation
import kotlinx.serialization.Serializable

@Serializable
data class DownloadScreenNavigation(
    val downloadUrl: String,
    val language: String
)

@Composable
fun DownloadScreen(
    navController: NavHostController,
    downloadUrl: String,
    language: String
) {
    val context = LocalContext.current
    val viewModel: DownloadViewModel = viewModel {
        val repository = BookRepositoryImpl(context, AppPreferences(context))
        DownloadViewModel(DownloadBookUseCase(repository))
    }

    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.handleIntent(DownloadContract.Intent.StartDownload(language, downloadUrl))
    }

    LaunchedEffect(state.stage) {
        if (state.stage == DownloadStage.Finished) {
            navController.popBackStack()
            navController.navigate(HomeActivityNavigation(rootIndex = "${state.language}/book/index.html"))
        }
    }

    DownloadContent(state = state)
}

@Composable
private fun DownloadContent(state: DownloadContract.State) {
    val animatedProgress by animateFloatAsState(
        targetValue = state.progress,
        label = "ProgressAnimation"
    )

    Scaffold { paddingValues ->
        Box(
            Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (state.stage == DownloadStage.Downloading)
                                Icons.Default.Info else Icons.Default.Settings,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (state.stage == DownloadStage.Downloading)
                                "Downloading ${state.language} Content" else "Extracting Files...",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (state.stage == DownloadStage.Downloading)
                                "Please wait while we fetch the latest version." else "Setting up the offline book content.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            strokeCap = StrokeCap.Round,
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "${(state.progress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
