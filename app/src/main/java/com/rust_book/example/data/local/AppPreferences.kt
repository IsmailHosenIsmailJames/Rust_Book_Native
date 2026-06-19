package com.rust_book.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.rust_book.example.domain.model.AppSettings
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onStart

class AppPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("rust_book_prefs", Context.MODE_PRIVATE)

    var lastOpenedPage: String?
        get() = prefs.getString("last_opened_page", null)
        set(value) = prefs.edit().putString("last_opened_page", value).apply()

    var homePage: String?
        get() = prefs.getString("home_page", null)
        set(value) = prefs.edit().putString("home_page", value).apply()

    var favorites: Set<String>
        get() = prefs.getStringSet("favorites", emptySet()) ?: emptySet()
        set(value) = prefs.edit().putStringSet("favorites", value).apply()

    var completedPages: Set<String>
        get() = prefs.getStringSet("completed_pages", emptySet()) ?: emptySet()
        set(value) = prefs.edit().putStringSet("completed_pages", value).apply()

    var history: List<String>
        get() = prefs.getString("history", "")?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
        set(value) = prefs.edit().putString("history", value.joinToString(",")).apply()

    fun getAppSettings(): Flow<AppSettings> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            trySend(getCurrentSettings())
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }.onStart { emit(getCurrentSettings()) }

    private fun getCurrentSettings(): AppSettings {
        return AppSettings(
            lastOpenedPage = lastOpenedPage,
            homePage = homePage,
            favorites = favorites,
            history = history,
            completedPages = completedPages
        )
    }

    fun addToHistory(page: String) {
        val currentHistory = history.toMutableList()
        currentHistory.remove(page)
        currentHistory.add(0, page)
        history = if (currentHistory.size > 20) {
            currentHistory.take(20)
        } else {
            currentHistory
        }
    }

    fun toggleFavorite(page: String) {
        val currentFavorites = favorites.toMutableSet()
        if (currentFavorites.contains(page)) {
            currentFavorites.remove(page)
        } else {
            currentFavorites.add(page)
        }
        favorites = currentFavorites
    }

    fun isFavorite(page: String): Boolean = favorites.contains(page)

    fun toggleCompletion(page: String) {
        val current = completedPages.toMutableSet()
        if (current.contains(page)) {
            current.remove(page)
        } else {
            current.add(page)
        }
        completedPages = current
    }

    fun isCompleted(page: String): Boolean = completedPages.contains(page)

    fun resetAll() {
        prefs.edit().clear().apply()
    }
}
