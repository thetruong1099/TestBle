package com.colors.testble.data.local.dao

import androidx.room.Dao
import com.colors.testble.data.base.BaseDao
import com.colors.testble.data.local.entity.MessageReceiveEntity

@Dao
interface MessageReceiveDao : BaseDao<MessageReceiveEntity> {
}