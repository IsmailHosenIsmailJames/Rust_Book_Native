package com.ismail.rustbook.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ismail.rustbook.data.database.dao.BookmarkDao
import com.ismail.rustbook.data.database.dao.LastPageStateDao
import com.ismail.rustbook.data.database.dao.UserPreferenceDao
import com.ismail.rustbook.data.database.entities.Bookmark
import com.ismail.rustbook.data.database.entities.LastPageState
import com.ismail.rustbook.data.database.entities.UserPreference

@Database(
    entities = [LastPageState::class, Bookmark::class, UserPreference::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun lastPageStateDao(): LastPageStateDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun userPreferenceDao(): UserPreferenceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rust_book_database" 
                )
                // We might add migrations later if needed, for now, keeping it simple.
                // .addMigrations(MIGRATION_1_2, ...) 
                // For initial setup, fallbackToDestructiveMigration can be useful if schema changes often during dev
                // .fallbackToDestructiveMigration() // Consider this for early development
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
