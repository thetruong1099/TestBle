package com.colors.testble.di

import com.colors.testble.data.local.entity.LogEntity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocalDBModule {
    @Singleton
    @Provides
    fun provideRealm(): Realm {
        val config = RealmConfiguration.Builder(
            schema = setOf(
                LogEntity::class
            )
        ).name("ble.realm")
            .schemaVersion(1)
            .compactOnLaunch()
            .build()

        return Realm.open(config)
    }
}