package com.ismail.rustbook.ui.feature.download

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ismail.rustbook.domain.usecase.DownloadBookUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DownloadViewModel(
    private val downloadBookUseCase: DownloadBookUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(DownloadContract.State())
    val state = _state.asStateFlow()

    fun handleIntent(intent: DownloadContract.Intent) {
        when (intent) {
            is DownloadContract.Intent.StartDownload -> {
                startDownload(intent.language, intent.url)
            }
        }
    }

    private fun startDownload(language: String, url: String) {
        viewModelScope.launch {
            _state.update { it.copy(language = language, stage = DownloadStage.Downloading, progress = 0f) }
            downloadBookUseCase(language, url) { totalProgress ->
                val stage = if (totalProgress < 0.5f) DownloadStage.Downloading else DownloadStage.Extracting
                val displayProgress = if (totalProgress < 0.5f) totalProgress * 2 else (totalProgress - 0.5f) * 2
                _state.update { it.copy(
                    stage = stage,
                    progress = displayProgress
                ) }
            }
            _state.update { it.copy(stage = DownloadStage.Finished, progress = 1f) }
        }
    }
}
