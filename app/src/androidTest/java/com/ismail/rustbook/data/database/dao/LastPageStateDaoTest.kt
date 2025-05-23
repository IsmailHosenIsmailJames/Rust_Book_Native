package com.ismail.rustbook.data.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ismail.rustbook.data.database.AppDatabase
import com.ismail.rustbook.data.database.entities.LastPageState
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class LastPageStateDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var lastPageStateDao: LastPageStateDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries() // Allowed for testing
            .build()
        lastPageStateDao = db.lastPageStateDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertOrUpdateAndGetLastPageState() = runTest {
        // Insert
        val initialState = LastPageState(id = 0, url = "file:///initial", scrollX = 10, scrollY = 20)
        lastPageStateDao.insertOrUpdate(initialState)

        var retrievedState = lastPageStateDao.get() // Non-suspend
        assertThat(retrievedState).isNotNull()
        assertThat(retrievedState?.id).isEqualTo(0)
        assertThat(retrievedState?.url).isEqualTo("file:///initial")
        assertThat(retrievedState?.scrollX).isEqualTo(10)
        assertThat(retrievedState?.scrollY).isEqualTo(20)

        // Update
        val updatedState = LastPageState(id = 0, url = "file:///updated", scrollX = 100, scrollY = 200)
        lastPageStateDao.insertOrUpdate(updatedState)

        retrievedState = lastPageStateDao.get() // Non-suspend
        assertThat(retrievedState).isNotNull()
        assertThat(retrievedState?.url).isEqualTo("file:///updated")
        assertThat(retrievedState?.scrollX).isEqualTo(100)
        assertThat(retrievedState?.scrollY).isEqualTo(200)
    }
}
