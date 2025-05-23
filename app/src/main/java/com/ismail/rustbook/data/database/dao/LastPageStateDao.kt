package com.ismail.rustbook.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ismail.rustbook.data.database.entities.LastPageState

@Dao
interface LastPageStateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(state: LastPageState)

    @Query("SELECT * FROM last_page_state WHERE id = 0")
    fun get(): LastPageState?
}
