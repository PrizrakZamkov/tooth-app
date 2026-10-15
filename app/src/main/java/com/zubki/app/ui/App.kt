package com.zubki.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zubki.app.data.ReminderSettings
import com.zubki.app.data.Store
import java.time.LocalDate

private enum class Page(val title: String) {
    Today("Сегодня"),
    Streak("Серия"),
    Settings("Настройки"),
}

// Корень интерфейса: три вкладки внизу и таймер поверх них
@Composable
fun App(store: Store, settings: ReminderSettings, today: LocalDate, notificationsOn: Boolean) {
    var page by remember { mutableStateOf(Page.Today) }
    var timerOpen by remember { mutableStateOf(false) }

    BackHandler(enabled = timerOpen) { timerOpen = false }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.bg)
            .safeDrawingPadding(),
    ) {
        Crossfade(targetState = timerOpen, label = "timer") { open ->
            if (open) {
                TimerScreen(store, today, onClose = { timerOpen = false })
            } else {
                Column(Modifier.fillMaxSize()) {
                    Box(Modifier.weight(1f)) {
                        Crossfade(targetState = page, label = "page") { current ->
                            when (current) {
                                Page.Today -> TodayScreen(store, today, onOpenTimer = { timerOpen = true })
                                Page.Streak -> StreakScreen(store, today)
                                Page.Settings -> SettingsScreen(settings, notificationsOn)
                            }
                        }
                    }
                    TabBar(selected = page, onSelect = { page = it })
                }
            }
        }
    }
}

// Вкладки-таблетки: мятная подложка плавно переезжает под выбранную
@Composable
private fun TabBar(selected: Page, onSelect: (Page) -> Unit) {
    val p = palette
    val pages = Page.entries

    BoxWithConstraints(
        modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .fillMaxWidth()
            .shadow(elevation = 3.dp, shape = CircleShape, clip = false)
            .clip(CircleShape)
            .background(p.card)
            .padding(4.dp),
    ) {
        val tabWidth = maxWidth / pages.size
        val offset by animateDpAsState(
            targetValue = tabWidth * selected.ordinal,
            animationSpec = spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMedium),
            label = "tabOffset",
        )

        Box(
            Modifier
                .offset(x = offset)
                .width(tabWidth)
                .height(44.dp)
                .clip(CircleShape)
                .background(p.mint),
        )

        Row {
            pages.forEach { page ->
                val textColor by animateColorAsState(
                    targetValue = if (page == selected) p.onMint else p.textSoft,
                    label = "tabText",
                )
                Box(
                    modifier = Modifier
                        .width(tabWidth)
                        .height(44.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { onSelect(page) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = page.title,
                        color = textColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}
