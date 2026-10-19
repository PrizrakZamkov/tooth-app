package com.zubki.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.zubki.app.data.ReminderSettings
import com.zubki.app.data.Store
import com.zubki.app.reminders.Reminders
import com.zubki.app.ui.App
import com.zubki.app.ui.ZubkiTheme
import com.zubki.app.widget.ZubkiWidget
import java.time.LocalDate

class MainActivity : ComponentActivity() {

    private lateinit var store: Store
    private lateinit var settings: ReminderSettings
    private var today by mutableStateOf(LocalDate.now())
    private var notificationsOn by mutableStateOf(true)

    private val askPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            notificationsOn = notificationsEnabled()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        store = Store(this)
        settings = ReminderSettings(this)

        Reminders.createChannel(this)
        Reminders.scheduleAll(this)
        askNotificationPermission()

        setContent {
            ZubkiTheme {
                App(store, settings, today, notificationsOn)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Приложение могло пролежать в фоне с вечера: обновляем дату и виджет,
        // а заодно проверяем, не выключили ли уведомления в настройках телефона
        today = LocalDate.now()
        store.reload()
        notificationsOn = notificationsEnabled()
        ZubkiWidget.refresh(this)
    }

    private fun notificationsEnabled() =
        NotificationManagerCompat.from(this).areNotificationsEnabled()

    // Начиная с Android 13 разрешение на уведомления нужно спрашивать
    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT < 33) return
        val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (!granted) askPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
