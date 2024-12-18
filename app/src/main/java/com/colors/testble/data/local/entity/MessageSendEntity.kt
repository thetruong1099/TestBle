package com.colors.testble.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "message_send_table")
data class MessageSendEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") val id: Int = 0,
    @ColumnInfo(name = "message") val message: String,
    @ColumnInfo(name = "time") val time: String = getCurrentFormattedTime()
) {
    companion object {
        private fun getCurrentFormattedTime(): String {
            val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
            return dateFormat.format(Date(System.currentTimeMillis()))
        }
    }
}
