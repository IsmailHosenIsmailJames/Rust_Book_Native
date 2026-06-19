package com.ismail.rustbook.ui.feature.language

import com.ismail.rustbook.domain.model.LanguageInfo

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
