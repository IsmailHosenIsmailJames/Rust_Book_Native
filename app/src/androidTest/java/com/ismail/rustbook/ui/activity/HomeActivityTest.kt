package com.ismail.rustbook.ui.activity

import android.content.Context
import android.content.Intent
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.assertIsDisplayed
import com.ismail.rustbook.data.database.AppDatabase
import com.ismail.rustbook.data.database.dao.BookmarkDao
import com.ismail.rustbook.data.database.entities.Bookmark
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class HomeActivityTest {

    // Use a specific Intent to control the starting page for HomeActivity
    private val testLanguage = "en"
    private val testFileName = "test_page.html"
    private val testFileTitle = "Test Page Title"
    private lateinit var testFile: File
    private lateinit var expectedFileUrl: String


    @get:Rule
    val composeTestRule = createAndroidComposeRule<HomeActivity>(
        createIntent = { context ->
            // Prepare the file first
            testFile = File(context.filesDir, "$testLanguage/$testFileName")
            testFile.parentFile?.mkdirs()
            FileOutputStream(testFile).use {
                it.write("<html><head><title>$testFileTitle</title></head><body>Test Content</body></html>".toByteArray())
            }
            expectedFileUrl = testFile.toURI().toString() // e.g., file:///data/user/0/.../en/test_page.html

            Intent(context, HomeActivity::class.java).apply {
                // Corresponds to HomeActivityNavigation(rootIndex = "$language/book/index.html")
                // We'll make our test file the rootIndex for predictability
                putExtra("HomeActivityNavigation_rootIndex", "$testLanguage/$testFileName")
            }
        }
    )

    private lateinit var db: AppDatabase
    private lateinit var bookmarkDao: BookmarkDao

    @Before
    fun setup() {
        Intents.init() // Initialize Intents before each test if you plan to verify Intents
        val context = ApplicationProvider.getApplicationContext<Context>()
        
        // Using an in-memory database for tests
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries() // Allow queries on the main thread for testing simplicity
            .build()
        bookmarkDao = db.bookmarkDao()

        // Clear bookmarks before each test to ensure a clean state
        runBlocking {
            val bookmarks = bookmarkDao.getAll().first()
            bookmarks.forEach { bookmarkDao.delete(it) }
        }

        // The composeTestRule with createIntent will launch HomeActivity with our test file.
        // We need to ensure HomeActivity uses *this* db instance.
        // This is a common challenge in Android testing.
        // For this test, we'll assume HomeActivity's AppDatabase.getDatabase(context)
        // will effectively use a fresh DB due to test context or it's been managed
        // by DI (though we are not setting up Hilt here).
        // A more robust way would be to inject the 'db' instance into HomeActivity.
        // For now, we rely on the fact that HomeActivity uses its own DB instance,
        // and we will verify against that same DB structure by re-acquiring it if needed,
        // or by simply observing UI changes that depend on DB state.
        // The key is that our DAO operations here are on `bookmarkDao` from our in-memory `db`.
        // We will verify UI based on actions taken, which indirectly tests DB interaction.
    }

    @After
    @Throws(IOException::class)
    fun tearDown() {
        db.close()
        Intents.release() // Release Intents after each test
        if (::testFile.isInitialized && testFile.exists()) {
            testFile.delete() // Clean up the test file
            testFile.parentFile?.delete() // Clean up the language directory if empty
        }
    }

    @Test
    fun testBookmarkPageAndVerifyInList() {
        // 1. Wait for WebView to be loaded and content to be available.
        // The URL should be our test file URL.
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            val currentWebViewUrl = composeTestRule.activity.webViewInstance?.url
            currentWebViewUrl == expectedFileUrl
        }
        // Additional wait for JS interface to report title (which happens in onPageFinished)
         composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.activity.currentPageTitle.value == testFileTitle
        }


        // 2. Click "More options" menu icon
        composeTestRule.onNodeWithContentDescription("More options").performClick()

        // 3. Click "Bookmark this page"
        composeTestRule.onNodeWithText("Bookmark this page").performClick()

        // 4. Verify Toast (difficult directly) and check DB.
        // Wait a moment for the DB operation to complete.
        // Using runBlocking here for a direct DAO check for simplicity in this test logic.
        // In a real scenario, you might observe UI changes or use IdlingResources.
        var newBookmark: Bookmark? = null
        composeTestRule.waitUntil(timeoutMillis = 2000) {
            runBlocking { // db operations are suspend functions
                newBookmark = bookmarkDao.getByUrl(expectedFileUrl)
            }
            newBookmark != null
        }
        assertThat(newBookmark).isNotNull()
        assertThat(newBookmark?.title).isEqualTo(testFileTitle)
        // The URL stored in the bookmark should be the absolute path without the "file://" scheme
        // if our saveCurrentState logic correctly processes it.
        // Our current saveCurrentState stores file.absolutePath, which does not include "file://".
        // And expectedFileUrl includes "file://". So we need to compare accordingly or adjust.
        // Let's assume the bookmark stores the absolute path.
        val expectedStoredUrl = File(expectedFileUrl.substring("file://".length)).absolutePath
        assertThat(newBookmark?.url).isEqualTo(expectedStoredUrl)


        // 5. Click "More options" again to open menu for "View Bookmarks"
        composeTestRule.onNodeWithContentDescription("More options").performClick()

        // 6. Click "View Bookmarks"
        composeTestRule.onNodeWithText("View Bookmarks").performClick()

        // 7. Verify the BookmarkListScreen is shown and contains the new bookmark
        composeTestRule.onNodeWithText("Bookmarks").assertIsDisplayed() // Title of BookmarkListScreen
        composeTestRule.onNodeWithText(testFileTitle).assertIsDisplayed()
        // In BookmarkListScreen, we display the URL as is from the bookmark entity
        composeTestRule.onNodeWithText(expectedStoredUrl).assertIsDisplayed()

        // (Optional) Click the bookmark to go back and verify WebView loads it
        composeTestRule.onNodeWithText(testFileTitle).performClick() // This clicks the item in BookmarkListScreen

        // Wait for the webViewUrl state in HomeActivity to update and then for WebView to load
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.activity.webViewUrl.value == expectedStoredUrl && // Check the state variable
            composeTestRule.activity.webViewInstance?.url == expectedFileUrl // Check actual WebView URL
        }
        assertThat(composeTestRule.activity.webViewInstance?.url).isEqualTo(expectedFileUrl)
    }
}
