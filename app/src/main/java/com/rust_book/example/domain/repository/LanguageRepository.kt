package com.rust_book.example.domain.repository

import com.rust_book.example.domain.model.LanguageInfo

interface LanguageRepository {
    fun getLanguages(): List<LanguageInfo>
}
