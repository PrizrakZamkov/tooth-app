package com.zubki.app.reminders

import com.zubki.app.data.Slot

// Тексты уведомлений. Каждый раз берётся случайный, чтобы не приедались
object Texts {

    private val morning = listOf(
        "Зубы сами себя не почистят",
        "Щётка ждёт у раковины",
        "Две минуты, и можно жить дальше",
        "Пока не убежал: щётка, паста, готово",
        "Свежий рот — хорошее начало дня",
        "Нажми «Почистил», когда закончишь",
    )

    private val evening = listOf(
        "День закончился, зубы ещё нет",
        "Последнее дело на сегодня",
        "Щётка опять скучает",
        "Две минуты, и можно ложиться",
        "Почисти и спокойно спи",
        "Перед сном, как обычно",
    )

    fun title(slot: Slot) = if (slot == Slot.Morning) "Утренняя чистка" else "Вечерняя чистка"

    fun body(slot: Slot) = (if (slot == Slot.Morning) morning else evening).random()
}
