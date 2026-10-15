package com.zubki.app.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.time.LocalDate

enum class Slot(val code: String, val title: String) {
    Morning("m", "Утро"),
    Evening("e", "Вечер"),
}

// Отметки для экрана. Сами они лежат в Marks, а здесь копия в виде состояния Compose
class Store(private val context: Context) {

    private val prefs = Marks.prefs(context)

    var marks by mutableStateOf(readAll())
        private set

    // Если отметку поставят не отсюда (уведомление, виджет), экран обновится сам
    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
        marks = readAll()
    }

    init {
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    fun doneAt(day: LocalDate, slot: Slot): String? = marks[Marks.idOf(day, slot)]

    fun toggle(day: LocalDate, slot: Slot) {
        if (doneAt(day, slot) != null) {
            Marks.unmark(context, day, slot)
        } else {
            Marks.mark(context, day, slot)
        }
    }

    // Дни, в которые отмечена хотя бы одна чистка
    fun markedDays(): Set<LocalDate> =
        marks.keys.map { LocalDate.parse(it.substringBefore('_')) }.toSet()

    private fun readAll(): Map<String, String> =
        prefs.all.mapNotNull { (key, value) -> (value as? String)?.let { key to it } }.toMap()
}
