package com.ismail.rustbook.ui.feature.language

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ismail.rustbook.domain.usecase.GetLanguagesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LanguageViewModel(
    private val getLanguagesUseCase: GetLanguagesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(LanguageContract.State())
    val state: StateFlow<LanguageContract.State> = _state.asStateFlow()

    init {
        loadLanguages()
    }

    private fun loadLanguages() {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val languages = getLanguagesUseCase()
            _state.update {
                it.copy(
                    languages = languages,
                    isLoading = false,
                    selectedIndex = 2 // Default index
                )
            }
        }
    }

    fun handleIntent(intent: LanguageContract.Intent) {
        when (intent) {
            is LanguageContract.Intent.SelectLanguage -> {
                _state.update { it.copy(selectedIndex = intent.index) }
            }
            LanguageContract.Intent.ConfirmSelection -> {
                // Handled in the UI for navigation
            }
        }
    }
}
