package com.rust_book.example.ui.feature.language

import com.rust_book.example.domain.model.LanguageInfo

object LanguageContract {
    data class State(
        val languages: List<LanguageInfo> = emptyList(),
        val selectedIndex: Int = 0,
        val isLoading: Boolean = false
    )

    sealed class Intent {
        data class SelectLanguage(val index: Int) : Intent()
        object ConfirmSelection : Intent()
    }
}
