package com.rust_book.example.ui.feature.download

sealed interface DownloadStage {
    data object Downloading : DownloadStage
    data object Extracting : DownloadStage
    data object Finished : DownloadStage
    data class Error(val message: String) : DownloadStage
}

object DownloadContract {
    data class State(
        val stage: DownloadStage = DownloadStage.Downloading,
        val progress: Float = 0f,
        val language: String = "",
        val errorMessage: String? = null
    )

    sealed class Intent {
        data class StartDownload(val language: String, val url: String) : Intent()
        data class Retry(val language: String, val url: String) : Intent()
    }
}
