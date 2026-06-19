package com.rust_book.example.ui.feature.download

enum class DownloadStage {
    Downloading,
    Extracting,
    Finished
}

object DownloadContract {
    data class State(
        val stage: DownloadStage = DownloadStage.Downloading,
        val progress: Float = 0f,
        val language: String = ""
    )

    sealed class Intent {
        data class StartDownload(val language: String, val url: String) : Intent()
    }
}
