package com.rust_book.example.data.repository

import com.rust_book.example.data.local.StaticData
import com.rust_book.example.domain.model.LanguageInfo
import com.rust_book.example.domain.repository.LanguageRepository

class LanguageRepositoryImpl : LanguageRepository {
    override fun getLanguages(): List<LanguageInfo> = StaticData.languages
}
