package com.ismail.rustbook.data.repository

import com.ismail.rustbook.data.local.StaticData
import com.ismail.rustbook.domain.model.LanguageInfo
import com.ismail.rustbook.domain.repository.LanguageRepository

class LanguageRepositoryImpl : LanguageRepository {
    override fun getLanguages(): List<LanguageInfo> = StaticData.languages
}
