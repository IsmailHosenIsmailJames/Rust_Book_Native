package com.ismail.rustbook.data.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ismail.rustbook.data.database.AppDatabase
import com.ismail.rustbook.data.database.entities.UserPreference
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class UserPreferenceDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var userPreferenceDao: UserPreferenceDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        userPreferenceDao = db.userPreferenceDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertOrUpdateAndGetByKey() = runTest {
        // Insert
        val initialPreference = UserPreference(key = "theme", value = "dark")
        userPreferenceDao.insertOrUpdate(initialPreference)

        var retrievedPreference = userPreferenceDao.getByKey("theme")
        assertThat(retrievedPreference).isNotNull()
        assertThat(retrievedPreference?.key).isEqualTo("theme")
        assertThat(retrievedPreference?.value).isEqualTo("dark")

        // Update
        val updatedPreference = UserPreference(key = "theme", value = "light")
        userPreferenceDao.insertOrUpdate(updatedPreference)

        retrievedPreference = userPreferenceDao.getByKey("theme")
        assertThat(retrievedPreference).isNotNull()
        assertThat(retrievedPreference?.value).isEqualTo("light")
    }

    @Test
    fun getByKey_nonExistent() = runTest {
        val retrievedPreference = userPreferenceDao.getByKey("non_existent_key")
        assertThat(retrievedPreference).isNull()
    }

    @Test
    fun insertMultipleAndGetCorrectly() = runTest {
        val pref1 = UserPreference(key = "fontSize", value = "16")
        val pref2 = UserPreference(key = "lineHeight", value = "1.5")
        userPreferenceDao.insertOrUpdate(pref1)
        userPreferenceDao.insertOrUpdate(pref2)

        val retrievedFont = userPreferenceDao.getByKey("fontSize")
        assertThat(retrievedFont).isNotNull()
        assertThat(retrievedFont?.value).isEqualTo("16")

        val retrievedLineHeight = userPreferenceDao.getByKey("lineHeight")
        assertThat(retrievedLineHeight).isNotNull()
        assertThat(retrievedLineHeight?.value).isEqualTo("1.5")
    }
}
