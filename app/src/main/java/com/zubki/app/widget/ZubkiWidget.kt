package com.zubki.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.Toast
import com.zubki.app.R
import com.zubki.app.data.Marks
import com.zubki.app.data.ReminderSettings
import com.zubki.app.data.slotForNow
import java.time.LocalDate

// Виджет 1×1: показывает, сколько чисток отмечено сегодня, а по нажатию отмечает текущую
class ZubkiWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        manager.updateAppWidget(ids, buildViews(context))
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action != ACTION_TAP) return

        val today = LocalDate.now()
        val slot = slotForNow(ReminderSettings(context).enabledSlots())
        if (Marks.doneAt(context, today, slot) == null) {
            Marks.mark(context, today, slot) // заодно обновит и сам виджет
        } else {
            Toast.makeText(context, "${slot.title}: уже отмечено", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val ACTION_TAP = "com.zubki.app.WIDGET_TAP"

        fun refresh(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, ZubkiWidget::class.java))
            if (ids.isNotEmpty()) manager.updateAppWidget(ids, buildViews(context))
        }

        private fun buildViews(context: Context): RemoteViews {
            val today = LocalDate.now()
            val slots = ReminderSettings(context).enabledSlots()
            val total = slots.size
            val done = slots.count { Marks.doneAt(context, today, it) != null }

            val (background, textColor) = when (done) {
                0 -> R.drawable.widget_bg_idle to R.color.widget_text
                total -> R.drawable.widget_bg_done to R.color.widget_text_done
                else -> R.drawable.widget_bg_part to R.color.widget_text
            }

            val tap = PendingIntent.getBroadcast(
                context,
                0,
                Intent(context, ZubkiWidget::class.java).setAction(ACTION_TAP),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

            return RemoteViews(context.packageName, R.layout.widget).apply {
                setTextViewText(R.id.widget_text, "$done/$total")
                setTextColor(R.id.widget_text, context.getColor(textColor))
                setInt(R.id.widget_root, "setBackgroundResource", background)
                setOnClickPendingIntent(R.id.widget_root, tap)
            }
        }
    }
}
