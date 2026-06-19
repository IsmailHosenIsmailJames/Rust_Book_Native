package com.rust_book.example.domain.model

data class AppSettings(
    val lastOpenedPage: String?,
    val homePage: String?,
    val favorites: Set<String>,
    val history: List<String>,
    val completedPages: Set<String>
)
