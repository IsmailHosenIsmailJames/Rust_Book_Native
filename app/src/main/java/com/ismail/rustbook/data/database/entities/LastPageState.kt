package com.ismail.rustbook.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "last_page_state")
data class LastPageState(
    @PrimaryKey
    val id: Int = 0, // Fixed ID, e.g., 0, as we only need one row
    val url: String,
    val scrollX: Int,
    val scrollY: Int
)
