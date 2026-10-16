package com.zubki.app.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zubki.app.data.ReminderSettings
import com.zubki.app.data.Store
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TodayScreen(store: Store, settings: ReminderSettings, today: LocalDate, onOpenTimer: () -> Unit) {
    val p = palette
    val slots = settings.enabledSlots()
    val doneCount = slots.count { store.doneAt(today, it) != null }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = dateTitle(today),
                color = p.text,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(Modifier.height(32.dp))
            DayRing(done = doneCount, total = slots.size)
            Spacer(Modifier.height(36.dp))

            Column(
                modifier = Modifier.fillMaxWidth().animateContentSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                slots.forEach { slot ->
                    SlotCard(slot, store.doneAt(today, slot)) { store.toggle(today, slot) }
                }
            }

            Spacer(Modifier.height(20.dp))
            PillButton("Таймер на 2 минуты", onOpenTimer, filled = false)
        }
    }
}

private fun dateTitle(day: LocalDate): String {
    val text = day.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.forLanguageTag("ru")))
    return text.replaceFirstChar { it.uppercase() }
}
