package com.colors.testble.data.local.datasource

import android.util.Log
import com.colors.testble.data.local.entity.LogEntity
import io.realm.kotlin.Realm
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object RealmDataSource {
    fun insertLog(realm: Realm, log: LogEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            realm.write { copyToRealm(log) }
            Log.d("devLog", "log: ${log.message}")
        }
    }
}