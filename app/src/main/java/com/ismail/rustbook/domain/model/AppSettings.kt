package com.ismail.rustbook.domain.model

data class AppSettings(
    val lastOpenedPage: String?,
    val homePage: String?,
    val favorites: Set<String>,
    val history: List<String>
)
