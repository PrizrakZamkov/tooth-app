package com.zubki.app.data

import android.content.Context
import android.content.SharedPreferences
import com.zubki.app.widget.ZubkiWidget
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val timeFormat = DateTimeFormatter.ofPattern("HH:mm")

// Отметки о чистке: ключ "2026-10-12_m", значение "08:14" (во сколько нажали).
// Ставить их можно откуда угодно: с экрана, из уведомления, с виджета.
object Marks {

    fun prefs(context: Context): SharedPreferences =
        context.applicationContext.getSharedPreferences("marks", Context.MODE_PRIVATE)

    fun idOf(day: LocalDate, slot: Slot) = "${day}_${slot.code}"

    fun doneAt(context: Context, day: LocalDate, slot: Slot): String? =
        prefs(context).getString(idOf(day, slot), null)

    fun mark(context: Context, day: LocalDate, slot: Slot) {
        if (doneAt(context, day, slot) != null) return
        prefs(context).edit().putString(idOf(day, slot), LocalTime.now().format(timeFormat)).commit()
        ZubkiWidget.refresh(context)
    }

    fun unmark(context: Context, day: LocalDate, slot: Slot) {
        prefs(context).edit().remove(idOf(day, slot)).commit()
        ZubkiWidget.refresh(context)
    }
}

// До трёх часов дня считаем, что речь про утро, дальше про вечер.
// Если нужное время выключено, берём то, что осталось
fun slotForNow(enabled: List<Slot> = Slot.entries, now: LocalTime = LocalTime.now()): Slot {
    val wanted = if (now.hour < 15) Slot.Morning else Slot.Evening
    return if (wanted in enabled) wanted else enabled.firstOrNull() ?: wanted
}
