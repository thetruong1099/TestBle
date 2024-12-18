package com.colors.testble.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.colors.testble.data.local.dao.MessageReceiveDao
import com.colors.testble.data.local.dao.MessageSendDao
import com.colors.testble.data.local.entity.MessageReceiveEntity
import com.colors.testble.data.local.entity.MessageSendEntity

@Database(
    entities = [MessageSendEntity::class, MessageReceiveEntity::class],
    version = 1,
    exportSchema = false
)
abstract class BLEDatabase : RoomDatabase() {
    abstract val messageSendDao: MessageSendDao
    abstract val messageReceiveDao: MessageReceiveDao
}