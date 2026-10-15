package com.zubki.app.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.zubki.app.data.Marks
import com.zubki.app.data.ReminderSettings
import com.zubki.app.widget.ZubkiWidget
import java.time.LocalDate

// Сработал будильник: показываем напоминание и ставим следующее
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (!ReminderSettings(context).remindersOn) return
        val slot = Reminders.slotOf(intent) ?: return

        if (intent.action == Reminders.ACTION_DAILY) Reminders.scheduleAll(context)
        Reminders.show(context, slot)
        ZubkiWidget.refresh(context)
    }
}

// Кнопки в уведомлении
class ActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val slot = Reminders.slotOf(intent) ?: return
        val day = intent.getStringExtra(Reminders.EXTRA_DAY)?.let { LocalDate.parse(it) } ?: LocalDate.now()

        when (intent.action) {
            Reminders.ACTION_DONE -> {
                Marks.mark(context, day, slot)
                Reminders.showConfirm(context, slot, day)
            }
            Reminders.ACTION_LATER -> {
                Reminders.snooze(context, slot)
                Reminders.cancelNotification(context, slot)
            }
        }
    }
}

// После перезагрузки телефона и обновления приложения будильники нужно поставить заново
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Reminders.scheduleAll(context)
        ZubkiWidget.refresh(context)
    }
}
