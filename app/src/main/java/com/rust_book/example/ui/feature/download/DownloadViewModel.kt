package com.rust_book.example.ui.feature.download

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rust_book.example.domain.usecase.DownloadBookUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

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
            is DownloadContract.Intent.Retry -> {
                startDownload(intent.language, intent.url)
            }
        }
    }

    private fun startDownload(language: String, url: String) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    language = language,
                    stage = DownloadStage.Downloading,
                    progress = 0f,
                    errorMessage = null
                )
            }
            try {
                downloadBookUseCase(language, url) { totalProgress ->
                    val stage = if (totalProgress < 0.5f) DownloadStage.Downloading else DownloadStage.Extracting
                    val displayProgress = if (totalProgress < 0.5f) totalProgress * 2 else (totalProgress - 0.5f) * 2
                    _state.update {
                        it.copy(
                            stage = stage,
                            progress = displayProgress
                        )
                    }
                }
                _state.update { it.copy(stage = DownloadStage.Finished, progress = 1f) }
            } catch (e: Exception) {
                val errorMsg = when (e) {
                    is UnknownHostException -> "No internet connection or server unreachable. Please check your network and try again."
                    is SocketTimeoutException -> "Connection timed out. Please check your network connection and try again."
                    is ConnectException -> "Unable to connect to the server. Please verify your connection."
                    is IOException -> e.localizedMessage ?: "Network error occurred while downloading."
                    else -> e.localizedMessage ?: "An unexpected error occurred."
                }
                _state.update {
                    it.copy(
                        stage = DownloadStage.Error(errorMsg),
                        errorMessage = errorMsg
                    )
                }
            }
        }
    }
}
