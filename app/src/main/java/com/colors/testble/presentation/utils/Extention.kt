package com.colors.testble.presentation.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun getCurrentFormattedTime(): String {
    val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
    return dateFormat.format(Date(System.currentTimeMillis()))
}