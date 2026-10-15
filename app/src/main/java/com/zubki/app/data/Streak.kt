package com.zubki.app.data

import java.time.LocalDate

data class StreakInfo(val current: Int, val best: Int, val paused: Boolean)

// Серия считается по дням, в которые отмечена хотя бы одна чистка.
// Один пропущенный день серию не рвёт (это пауза), два подряд обнуляют её.
// Сегодняшний день пропуском не считается: он ещё не закончился.
fun streakInfo(days: Set<LocalDate>, today: LocalDate): StreakInfo {
    if (days.isEmpty()) return StreakInfo(0, 0, false)

    var day = days.reduce { a, b -> if (a.isBefore(b)) a else b }
    var run = 0
    var best = 0
    var misses = 0

    while (!day.isAfter(today)) {
        if (day in days) {
            run++
            misses = 0
            if (run > best) best = run
        } else if (day != today) {
            misses++
            if (misses >= 2) run = 0
        }
        day = day.plusDays(1)
    }

    return StreakInfo(current = run, best = best, paused = run > 0 && misses == 1)
}
