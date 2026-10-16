package com.zubki.app.ui

import android.os.SystemClock
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zubki.app.data.ReminderSettings
import com.zubki.app.data.Store
import com.zubki.app.data.slotForNow
import java.time.LocalDate
import kotlinx.coroutines.delay

private const val TOTAL_MS = 120_000L
private const val QUARTER_MS = 30_000L

private val hints = listOf(
    "верхние зубы, слева",
    "верхние зубы, справа",
    "нижние зубы, справа",
    "нижние зубы, слева",
)

// Таймер на две минуты: по 30 секунд на каждую четверть рта
@Composable
fun TimerScreen(store: Store, settings: ReminderSettings, today: LocalDate, onClose: () -> Unit) {
    val p = palette
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current

    var remaining by remember { mutableStateOf(TOTAL_MS) }
    var running by remember { mutableStateOf(false) }

    val finished = remaining <= 0L
    val started = remaining < TOTAL_MS
    val quarter = ((TOTAL_MS - remaining) / QUARTER_MS).toInt().coerceIn(0, 3)
    val slot = slotForNow(settings.enabledSlots())
    val alreadyDone = store.doneAt(today, slot) != null

    // Пока идёт отсчёт, экран не гаснет
    DisposableEffect(running) {
        view.keepScreenOn = running
        onDispose { view.keepScreenOn = false }
    }

    // Время считаем по часам телефона, а не по числу шагов цикла, чтобы оно не уплывало
    LaunchedEffect(running) {
        if (!running) return@LaunchedEffect
        val startedAt = SystemClock.elapsedRealtime()
        val base = remaining
        while (remaining > 0) {
            delay(50)
            remaining = (base - (SystemClock.elapsedRealtime() - startedAt)).coerceAtLeast(0L)
        }
        running = false
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    // Лёгкая отдача, когда пора переходить к следующей четверти
    LaunchedEffect(quarter) {
        if (quarter > 0 && !finished) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    val seconds = ((remaining + 999) / 1000).toInt()
    val timeText = "${seconds / 60}:${(seconds % 60).toString().padStart(2, '0')}"
    val hint = when {
        finished -> if (alreadyDone) "готово, ${slot.title.lowercase()} уже отмечено" else "готово"
        !started -> "по 30 секунд на каждую четверть"
        else -> hints[quarter]
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(Modifier.fillMaxWidth()) {
            if (!finished) {
                Text(
                    text = "Назад",
                    color = p.textSoft,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .pressable(onClose)
                        .padding(8.dp),
                )
            }
        }

        Spacer(Modifier.weight(1f))

        Box(Modifier.size(260.dp), contentAlignment = Alignment.Center) {
            Ring(
                progress = 1f - remaining.toFloat() / TOTAL_MS,
                color = if (finished) p.mintDeep else p.mint,
                modifier = Modifier.fillMaxSize(),
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = timeText,
                    color = p.text,
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                )
                Crossfade(targetState = hint, label = "hint") { text ->
                    Text(
                        text = text,
                        color = p.textSoft,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 36.dp),
                    )
                }
            }
        }

        Spacer(Modifier.weight(1f))

        Column(
            modifier = Modifier.widthIn(max = 320.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (finished) {
                if (alreadyDone) {
                    PillButton("Закрыть", onClose, Modifier.fillMaxWidth())
                } else {
                    PillButton(
                        text = "Отметить: ${slot.title.lowercase()}",
                        onClick = {
                            store.toggle(today, slot)
                            onClose()
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    PillButton("Закрыть без отметки", onClose, Modifier.fillMaxWidth(), filled = false)
                }
            } else {
                PillButton(
                    text = if (running) "Пауза" else if (started) "Дальше" else "Старт",
                    onClick = { running = !running },
                    modifier = Modifier.fillMaxWidth(),
                )
                if (started) {
                    PillButton(
                        text = "Сначала",
                        onClick = {
                            running = false
                            remaining = TOTAL_MS
                        },
                        modifier = Modifier.fillMaxWidth(),
                        filled = false,
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}
