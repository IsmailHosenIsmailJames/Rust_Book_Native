package com.ismail.rustbook.ui

import android.content.Context
import android.content.SharedPreferences

class AppStateProvider(context: Context) {
  private val prefs: SharedPreferences = context.getSharedPreferences("rust_book_prefs", Context.MODE_PRIVATE)

  var lastOpenedPage: String?
    get() = prefs.getString("last_opened_page", null)
    set(value) = prefs.edit().putString("last_opened_page", value).apply()

  var homePage: String?
    get() = prefs.getString("home_page", null)
    set(value) = prefs.edit().putString("home_page", value).apply()

  var favorites: Set<String>
    get() = prefs.getStringSet("favorites", emptySet()) ?: emptySet()
    set(value) = prefs.edit().putStringSet("favorites", value).apply()

  var history: List<String>
    get() = prefs.getString("history", "")?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
    set(value) = prefs.edit().putString("history", value.joinToString(",")).apply()

  fun addToHistory(page: String) {
    val currentHistory = history.toMutableList()
    currentHistory.remove(page) // Move to top if exists
    currentHistory.add(0, page)
    if (currentHistory.size > 20) {
      history = currentHistory.take(20)
    } else {
      history = currentHistory
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

  fun resetAll() {
    prefs.edit().clear().apply()
  }
}
