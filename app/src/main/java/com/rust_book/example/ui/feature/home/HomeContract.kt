package com.rust_book.example.ui.feature.home

import com.rust_book.example.domain.model.AppSettings

interface HomeContract {
    data class State(
        val currentUrl: String = "",
        val searchQuery: String = "",
        val isFavorite: Boolean = false,
        val isCompleted: Boolean = false,
        val history: List<String> = emptyList(),
        val favorites: List<String> = emptyList(),
        val filteredSearchFiles: List<String> = emptyList(),
        val baseDir: String = "",
        val appSettings: AppSettings? = null
    )

    sealed interface Intent {
        data class Search(val query: String) : Intent
        data class NavigateTo(val url: String) : Intent
        object ToggleFavorite : Intent
        object ToggleCompletion : Intent
        object NavigateToProgress : Intent
        object GoHome : Intent
        object SetAsHome : Intent
        object ResetApp : Intent
        data class PageFinished(val url: String) : Intent
        object LoadInitialData : Intent
        object RateApp : Intent
        object StarOnGitHub : Intent
    }

    sealed interface Effect {
        object NavigateToLanguage : Effect
        object NavigateToProgress : Effect
        data class OpenUrl(val url: String) : Effect
    }
}
