package com.colors.testble.di

import android.content.Context
import androidx.room.Room
import com.colors.testble.data.local.database.BLEDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocalDBModule {
    @Singleton
    @Provides
    fun provideColorsNovelDatabase(@ApplicationContext context: Context): BLEDatabase = Room.databaseBuilder(
        context,
        BLEDatabase::class.java,
        "ble_database"
    ).build()
}