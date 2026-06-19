package com.ismail.rustbook.ui.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ismail.rustbook.domain.repository.BookRepository
import com.ismail.rustbook.domain.usecase.GetAppSettingsUseCase
import com.ismail.rustbook.domain.usecase.UpdateAppSettingsUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getAppSettingsUseCase: GetAppSettingsUseCase,
    private val updateAppSettingsUseCase: UpdateAppSettingsUseCase,
    private val repository: BookRepository,
    private val rootIndex: String
) : ViewModel() {

    private val _state = MutableStateFlow(HomeContract.State(currentUrl = rootIndex))
    val state = _state.asStateFlow()

    private val _effect = MutableSharedFlow<HomeContract.Effect>()
    val effect = _effect.asSharedFlow()

    private var allHtmlFiles: List<String> = emptyList()

    init {
        val baseDir = rootIndex.substringBeforeLast("/") + "/"
        _state.update { it.copy(baseDir = baseDir) }
        
        viewModelScope.launch {
            allHtmlFiles = repository.getHtmlFiles(baseDir)
            
            getAppSettingsUseCase().collect { settings ->
                _state.update { currentState ->
                    currentState.copy(
                        appSettings = settings,
                        history = settings.history,
                        favorites = settings.favorites.toList(),
                        isFavorite = settings.favorites.contains(currentState.currentUrl)
                    )
                }
            }
        }
    }

    fun handleIntent(intent: HomeContract.Intent) {
        when (intent) {
            is HomeContract.Intent.Search -> {
                _state.update { it.copy(searchQuery = intent.query) }
                updateFilteredFiles(intent.query)
            }
            is HomeContract.Intent.NavigateTo -> {
                _state.update { it.copy(currentUrl = intent.url, searchQuery = "") }
            }
            HomeContract.Intent.ToggleFavorite -> {
                viewModelScope.launch {
                    updateAppSettingsUseCase.toggleFavorite(_state.value.currentUrl)
                }
            }
            HomeContract.Intent.GoHome -> {
                val home = _state.value.appSettings?.homePage ?: rootIndex
                handleIntent(HomeContract.Intent.NavigateTo(home))
            }
            HomeContract.Intent.SetAsHome -> {
                viewModelScope.launch {
                    updateAppSettingsUseCase.updateHomePage(_state.value.currentUrl)
                }
            }
            HomeContract.Intent.ResetApp -> {
                viewModelScope.launch {
                    updateAppSettingsUseCase.resetSettings()
                    _effect.emit(HomeContract.Effect.NavigateToLanguage)
                }
            }
            HomeContract.Intent.RateApp -> {
                viewModelScope.launch {
                    _effect.emit(HomeContract.Effect.OpenUrl("https://play.google.com/store/apps/details?id=com.rust_book.example"))
                }
            }
            HomeContract.Intent.StarOnGitHub -> {
                viewModelScope.launch {
                    _effect.emit(HomeContract.Effect.OpenUrl("https://github.com/IsmailHosenIsmailJames/Rust_Book_Native"))
                }
            }
            is HomeContract.Intent.PageFinished -> {
                _state.update { it.copy(
                    currentUrl = intent.url,
                    isFavorite = _state.value.appSettings?.favorites?.contains(intent.url) ?: false
                ) }
                viewModelScope.launch {
                    updateAppSettingsUseCase.updateLastOpenedPage(intent.url)
                    updateAppSettingsUseCase.addToHistory(intent.url)
                }
            }
            HomeContract.Intent.LoadInitialData -> {
                // Already handled in init, but can be used for refreshing if needed
            }
        }
    }

    private fun updateFilteredFiles(query: String) {
        if (query.isBlank()) {
            _state.update { it.copy(filteredSearchFiles = emptyList()) }
        } else {
            val filtered = allHtmlFiles.filter { it.contains(query, ignoreCase = true) }.take(15)
            _state.update { it.copy(filteredSearchFiles = filtered) }
        }
    }
}
