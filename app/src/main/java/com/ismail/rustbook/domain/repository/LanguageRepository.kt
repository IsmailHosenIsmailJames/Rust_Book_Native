package com.ismail.rustbook.domain.repository

import com.ismail.rustbook.domain.model.LanguageInfo

interface LanguageRepository {
    fun getLanguages(): List<LanguageInfo>
}
