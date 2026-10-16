package com.zubki.app.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.time.LocalTime

// Настройки напоминаний: включены ли они и во сколько приходят (время хранится в минутах от полуночи)
class ReminderSettings(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences("settings", Context.MODE_PRIVATE)

    var remindersOn by mutableStateOf(prefs.getBoolean("on", true))
        private set

    private var morning by mutableStateOf(prefs.getInt("morning", 8 * 60))
    private var evening by mutableStateOf(prefs.getInt("evening", 21 * 60))

    fun timeOf(slot: Slot): LocalTime {
        val minutes = if (slot == Slot.Morning) morning else evening
        return LocalTime.of(minutes / 60, minutes % 60)
    }

    fun setTime(slot: Slot, time: LocalTime) {
        val minutes = time.hour * 60 + time.minute
        if (slot == Slot.Morning) {
            morning = minutes
            prefs.edit().putInt("morning", minutes).apply()
        } else {
            evening = minutes
            prefs.edit().putInt("evening", minutes).apply()
        }
    }

    fun enableReminders(on: Boolean) {
        remindersOn = on
        prefs.edit().putBoolean("on", on).apply()
    }
}
