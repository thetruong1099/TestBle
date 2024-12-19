package com.colors.testble.data.local.entity

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.RealmUUID
import io.realm.kotlin.types.annotations.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LogEntity : RealmObject {
    @PrimaryKey
    var id: String = RealmUUID.random().toString()
    var message: String = ""
    var time: String = getCurrentFormattedTime()
}

fun getCurrentFormattedTime(): String {
    val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
    return dateFormat.format(Date(System.currentTimeMillis()))
}