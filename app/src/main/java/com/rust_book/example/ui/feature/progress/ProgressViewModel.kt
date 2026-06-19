package com.rust_book.example.ui.feature.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rust_book.example.domain.repository.BookRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProgressViewModel(
    private val repository: BookRepository
) : ViewModel() {

    data class State(
        val progress: Float = 0f,
        val totalPages: Int = 0,
        val completedCount: Int = 0,
        val completedPages: Set<String> = emptySet()
    )

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAppSettings().collect { settings ->
                val completed = settings.completedPages
                
                // Get all HTML files to calculate total pages
                // We assume the base directory is known or we can get it from the last opened page
                val baseDir = settings.lastOpenedPage?.substringBefore("/") ?: ""
                val allFiles = if (baseDir.isNotEmpty()) repository.getHtmlFiles(baseDir) else emptyList()
                
                val total = allFiles.size
                val count = completed.size
                val progressValue = if (total > 0) count.toFloat() / total else 0f

                _state.update {
                    it.copy(
                        progress = progressValue,
                        totalPages = total,
                        completedCount = count,
                        completedPages = completed
                    )
                }
            }
        }
    }
}
