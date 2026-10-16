package com.zubki.app.ui

import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zubki.app.data.ReminderSettings
import com.zubki.app.data.Slot
import com.zubki.app.reminders.Reminders
import com.zubki.app.widget.ZubkiWidget
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun SettingsScreen(settings: ReminderSettings, notificationsOn: Boolean) {
    val p = palette
    val context = LocalContext.current

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "Напоминания",
                color = p.text,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 4.dp),
            )

            SoftCard {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "Присылать напоминания",
                            color = p.text,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "с кнопкой «Почистил» прямо в уведомлении",
                            color = p.textSoft,
                            fontSize = 13.sp,
                        )
                    }
                    ZubkiSwitch(
                        checked = settings.remindersOn,
                        onChange = { on ->
                            settings.enableReminders(on)
                            Reminders.scheduleAll(context)
                        },
                    )
                }
            }

            SoftCard {
                Text(
                    text = "Когда чистишь",
                    color = p.text,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Если чистишь раз в день, отключи лишнее время",
                    color = p.textSoft,
                    fontSize = 13.sp,
                )
                Spacer(Modifier.height(10.dp))
                TimeRow(Slot.Morning, settings)
                TimeRow(Slot.Evening, settings)
            }

            if (!notificationsOn) {
                SoftCard {
                    Text(
                        text = "Уведомления выключены",
                        color = p.text,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Без них напоминания не придут. Включить их можно в настройках телефона.",
                        color = p.textSoft,
                        fontSize = 14.sp,
                    )
                    Spacer(Modifier.height(14.dp))
                    PillButton("Открыть настройки", { openNotificationSettings(context) })
                }
            }
        }
    }
}

@Composable
private fun TimeRow(slot: Slot, settings: ReminderSettings) {
    val p = palette
    val context = LocalContext.current
    val on = settings.slotOn(slot)
    val time = settings.timeOf(slot)
    val lastOne = on && settings.enabledSlots().size == 1

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = slot.title,
            modifier = Modifier.weight(1f),
            color = if (on) p.text else p.textSoft,
            fontSize = 18.sp,
        )

        // Время нужно только тем, кому приходят напоминания
        AnimatedVisibility(visible = on && settings.remindersOn) {
            Text(
                text = time.format(DateTimeFormatter.ofPattern("HH:mm")),
                color = p.text,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(end = 12.dp)
                    .pressable { pickTime(context, slot, settings) }
                    .clip(CircleShape)
                    .background(p.cardDone)
                    .padding(horizontal = 20.dp, vertical = 10.dp),
            )
        }

        // Последнее включённое время выключить нельзя, переключатель в этом случае неактивен
        ZubkiSwitch(
            checked = on,
            onChange = { value ->
                settings.switchSlot(slot, value)
                Reminders.scheduleAll(context)
                ZubkiWidget.refresh(context)
            },
            enabled = !lastOne,
        )
    }
}

// Выбор времени: системное окно с часами
private fun pickTime(context: Context, slot: Slot, settings: ReminderSettings) {
    val time = settings.timeOf(slot)
    TimePickerDialog(
        context,
        { _, hour, minute ->
            settings.setTime(slot, LocalTime.of(hour, minute))
            Reminders.scheduleAll(context)
        },
        time.hour,
        time.minute,
        true,
    ).show()
}

private fun openNotificationSettings(context: Context) {
    val intent = Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        .putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
    context.startActivity(intent)
}
