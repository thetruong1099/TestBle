package com.colors.testble.data.local.dao

import androidx.room.Dao
import com.colors.testble.data.base.BaseDao
import com.colors.testble.data.local.entity.MessageSendEntity

@Dao
interface MessageSendDao : BaseDao<MessageSendEntity> {
}