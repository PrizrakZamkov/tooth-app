package com.zubki.app.reminders

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.zubki.app.MainActivity
import com.zubki.app.R
import com.zubki.app.data.Marks
import com.zubki.app.data.ReminderSettings
import com.zubki.app.data.Slot
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

// Всё про напоминания: когда они приходят, как выглядят и что делают кнопки.
// Будильник ставится не «точно в минуту», поэтому уведомление может прийти с задержкой в пару минут.
object Reminders {

    const val ACTION_DAILY = "com.zubki.app.DAILY"
    const val ACTION_SNOOZE = "com.zubki.app.SNOOZE"
    const val ACTION_DONE = "com.zubki.app.DONE"
    const val ACTION_LATER = "com.zubki.app.LATER"
    const val EXTRA_SLOT = "slot"
    const val EXTRA_DAY = "day"

    private const val CHANNEL = "reminders"
    private const val SNOOZE_MINUTES = 15L
    private val accent = 0xFF3FA88B.toInt()

    fun slotOf(intent: Intent): Slot? {
        val code = intent.getStringExtra(EXTRA_SLOT)
        return Slot.entries.firstOrNull { it.code == code }
    }

    fun createChannel(context: Context) {
        val channel = NotificationChannel(CHANNEL, "Напоминания", NotificationManager.IMPORTANCE_HIGH)
        channel.description = "Напоминания почистить зубы"
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    // Ставит (или переставляет) ежедневные напоминания по текущим настройкам
    fun scheduleAll(context: Context) {
        val settings = ReminderSettings(context)
        Slot.entries.forEach { slot ->
            val intent = alarmIntent(context, slot, ACTION_DAILY, 100)
            if (settings.remindersOn) {
                val now = LocalDateTime.now()
                var next = now.toLocalDate().atTime(settings.timeOf(slot))
                if (!next.isAfter(now)) next = next.plusDays(1)
                setAlarm(context, intent, next)
            } else {
                context.getSystemService(AlarmManager::class.java).cancel(intent)
            }
        }
    }

    fun snooze(context: Context, slot: Slot) {
        val at = LocalDateTime.now().plusMinutes(SNOOZE_MINUTES)
        setAlarm(context, alarmIntent(context, slot, ACTION_SNOOZE, 200), at)
    }

    fun show(context: Context, slot: Slot) {
        // Если чистка уже отмечена, беспокоить не нужно
        if (Marks.doneAt(context, LocalDate.now(), slot) != null) return

        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return
        createChannel(context)

        val open = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(accent)
            .setContentTitle(Texts.title(slot))
            .setContentText(Texts.body(slot))
            .setContentIntent(open)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .addAction(0, "Почистил", actionIntent(context, slot, ACTION_DONE, 300))
            .addAction(0, "Через 15 минут", actionIntent(context, slot, ACTION_LATER, 400))
            .build()
        manager.notify(notificationId(slot), notification)
    }

    // После нажатия «Почистил» уведомление на пару секунд меняется на «Отмечено» и исчезает
    fun showConfirm(context: Context, slot: Slot, day: LocalDate) {
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return
        val time = Marks.doneAt(context, day, slot) ?: return
        createChannel(context)

        val notification = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(accent)
            .setContentTitle("Отмечено")
            .setContentText("${slot.title} в $time")
            .setOnlyAlertOnce(true)
            .setAutoCancel(true)
            .setTimeoutAfter(3000)
            .build()
        manager.notify(notificationId(slot), notification)
    }

    fun cancelNotification(context: Context, slot: Slot) {
        NotificationManagerCompat.from(context).cancel(notificationId(slot))
    }

    private fun notificationId(slot: Slot) = 1 + slot.ordinal

    private fun setAlarm(context: Context, intent: PendingIntent, at: LocalDateTime) {
        val millis = at.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        context.getSystemService(AlarmManager::class.java)
            .setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, intent)
    }

    // Номер у каждого PendingIntent свой, иначе утро и вечер затрут друг друга
    private fun alarmIntent(context: Context, slot: Slot, action: String, base: Int): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java)
            .setAction(action)
            .putExtra(EXTRA_SLOT, slot.code)
        return PendingIntent.getBroadcast(
            context, base + slot.ordinal, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    // Дата кладётся в кнопку заранее: если нажать после полуночи, отметка уйдёт в нужный день
    private fun actionIntent(context: Context, slot: Slot, action: String, base: Int): PendingIntent {
        val intent = Intent(context, ActionReceiver::class.java)
            .setAction(action)
            .putExtra(EXTRA_SLOT, slot.code)
            .putExtra(EXTRA_DAY, LocalDate.now().toString())
        return PendingIntent.getBroadcast(
            context, base + slot.ordinal, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
