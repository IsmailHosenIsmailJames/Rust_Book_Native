package com.ismail.rustbook.data.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ismail.rustbook.data.database.AppDatabase
import com.ismail.rustbook.data.database.entities.Bookmark
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class BookmarkDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var bookmarkDao: BookmarkDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        bookmarkDao = db.bookmarkDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndGetAllBookmarks() = runTest {
        val bookmark1 = Bookmark(url = "file:///page1", title = "Page 1", timestamp = System.currentTimeMillis())
        val bookmark2 = Bookmark(url = "file:///page2", title = "Page 2", timestamp = System.currentTimeMillis() + 1000)
        
        bookmarkDao.insert(bookmark1)
        bookmarkDao.insert(bookmark2)

        val allBookmarks = bookmarkDao.getAll().first()
        assertThat(allBookmarks).hasSize(2)
        assertThat(allBookmarks[0].url).isEqualTo(bookmark2.url) // Ordered by timestamp DESC
        assertThat(allBookmarks[1].url).isEqualTo(bookmark1.url)
    }

    @Test
    fun insertWithConflictIgnore() = runTest {
        val originalBookmark = Bookmark(url = "file:///unique", title = "Original Title", timestamp = 100L)
        val insertResult1 = bookmarkDao.insert(originalBookmark)
        assertThat(insertResult1).isGreaterThan(0L) // Successful insert returns rowId

        val conflictingBookmark = Bookmark(url = "file:///unique", title = "Conflicting Title", timestamp = 200L)
        val insertResult2 = bookmarkDao.insert(conflictingBookmark) // Should be ignored
        assertThat(insertResult2).isEqualTo(-1L) // OnConflictStrategy.IGNORE returns -1 on conflict

        val allBookmarks = bookmarkDao.getAll().first()
        assertThat(allBookmarks).hasSize(1)
        assertThat(allBookmarks[0].title).isEqualTo("Original Title")
        assertThat(allBookmarks[0].timestamp).isEqualTo(100L)
    }

    @Test
    fun deleteBookmark() = runTest {
        val bookmark = Bookmark(url = "file:///to_delete", title = "Delete Me", timestamp = 300L)
        val insertedId = bookmarkDao.insert(bookmark)
        
        // Create a bookmark object with the correct ID for deletion
        val bookmarkToDelete = Bookmark(id = insertedId, url = bookmark.url, title = bookmark.title, timestamp = bookmark.timestamp)
        bookmarkDao.delete(bookmarkToDelete)

        val allBookmarks = bookmarkDao.getAll().first()
        assertThat(allBookmarks).isEmpty()
    }

    @Test
    fun getByUrl() = runTest {
        val bookmark = Bookmark(url = "file:///search_me", title = "Searchable", timestamp = 400L)
        bookmarkDao.insert(bookmark)

        val foundBookmark = bookmarkDao.getByUrl("file:///search_me")
        assertThat(foundBookmark).isNotNull()
        assertThat(foundBookmark?.title).isEqualTo("Searchable")

        val notFoundBookmark = bookmarkDao.getByUrl("file:///non_existent")
        assertThat(notFoundBookmark).isNull()
    }
}
