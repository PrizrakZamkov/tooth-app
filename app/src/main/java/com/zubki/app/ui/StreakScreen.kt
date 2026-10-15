package com.zubki.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zubki.app.data.Slot
import com.zubki.app.data.Store
import com.zubki.app.data.StreakInfo
import com.zubki.app.data.streakInfo
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun StreakScreen(store: Store, today: LocalDate) {
    val info = streakInfo(store.markedDays(), today)

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            StreakCard(info)
            MonthCard(store, today)
            BadgesCard(info.best)
        }
    }
}

@Composable
private fun StreakCard(info: StreakInfo) {
    val p = palette

    SoftCard {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            AnimatedContent(
                targetState = info.current,
                transitionSpec = {
                    (slideInVertically { it / 2 } + fadeIn()) togetherWith
                        (slideOutVertically { -it / 2 } + fadeOut())
                },
                label = "streak",
            ) { n ->
                Text(
                    text = "$n",
                    color = p.mintDeep,
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Text(
                text = "${pluralDays(info.current)} подряд",
                color = p.text,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = if (info.paused) {
                    "вчера пропущено, серия на паузе. Отметься сегодня, и она продолжится"
                } else {
                    "лучшая серия: ${info.best}"
                },
                color = p.textSoft,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun MonthCard(store: Store, today: LocalDate) {
    val p = palette
    val current = YearMonth.from(today)
    var month by remember { mutableStateOf(current) }

    SoftCard {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            ArrowButton("‹", enabled = true) { month = month.minusMonths(1) }
            Text(
                text = monthTitle(month),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                color = p.text,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
            )
            ArrowButton("›", enabled = month < current) { month = month.plusMonths(1) }
        }

        Spacer(Modifier.height(14.dp))

        Row(Modifier.fillMaxWidth()) {
            listOf("пн", "вт", "ср", "чт", "пт", "сб", "вс").forEach { name ->
                Text(
                    text = name,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = p.textSoft,
                    fontSize = 12.sp,
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        AnimatedContent(
            targetState = month,
            modifier = Modifier.fillMaxWidth(),
            transitionSpec = {
                val dir = if (targetState > initialState) 1 else -1
                (slideInHorizontally { dir * it / 3 } + fadeIn()) togetherWith
                    (slideOutHorizontally { -dir * it / 3 } + fadeOut())
            },
            label = "month",
        ) { m ->
            MonthGrid(store, m, today)
        }
    }
}

@Composable
private fun ArrowButton(symbol: String, enabled: Boolean, onClick: () -> Unit) {
    val p = palette

    Box(
        modifier = Modifier
            .size(40.dp)
            .then(if (enabled) Modifier.pressable(onClick) else Modifier)
            .clip(CircleShape)
            .background(p.track),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = symbol,
            color = if (enabled) p.text else p.textSoft.copy(alpha = 0.4f),
            fontSize = 22.sp,
        )
    }
}

@Composable
private fun MonthGrid(store: Store, month: YearMonth, today: LocalDate) {
    val offset = month.atDay(1).dayOfWeek.value - 1 // понедельник = 0
    val length = month.lengthOfMonth()
    val rows = (offset + length + 6) / 7

    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(rows) { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(7) { col ->
                    val number = row * 7 + col - offset + 1
                    Box(Modifier.weight(1f).aspectRatio(1f)) {
                        if (number in 1..length) {
                            DayCell(store, month.atDay(number), today)
                        }
                    }
                }
            }
        }
    }
}

// Цвет клетки: оба раза отмечены, один раз или ни разу. Внизу две точки: утро и вечер
@Composable
private fun DayCell(store: Store, day: LocalDate, today: LocalDate) {
    val p = palette
    val shape = RoundedCornerShape(12.dp)
    val morning = store.doneAt(day, Slot.Morning) != null
    val evening = store.doneAt(day, Slot.Evening) != null
    val future = day.isAfter(today)

    val background by animateColorAsState(
        targetValue = when {
            morning && evening -> p.mint
            morning || evening -> p.cardDone
            else -> p.track.copy(alpha = 0.5f)
        },
        label = "dayBg",
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(shape)
            .background(background)
            .then(if (day == today) Modifier.border(2.dp, p.mintDeep, shape) else Modifier),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "${day.dayOfMonth}",
            color = when {
                morning && evening -> p.onMint
                future -> p.textSoft.copy(alpha = 0.5f)
                else -> p.text
            },
            fontSize = 14.sp,
            fontWeight = if (day == today) FontWeight.Bold else FontWeight.Medium,
        )

        if (!future) {
            Spacer(Modifier.height(3.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                Dot(if (morning) p.sun else p.track)
                Dot(if (evening) p.moon else p.track)
            }
        }
    }
}

@Composable
private fun Dot(color: Color) {
    Box(
        Modifier
            .size(5.dp)
            .clip(CircleShape)
            .background(color),
    )
}

@Composable
private fun BadgesCard(best: Int) {
    val p = palette

    SoftCard {
        Text(
            text = "Значки",
            color = p.text,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            listOf(7, 30, 100).forEach { goal ->
                Badge(goal, earned = best >= goal)
            }
        }
    }
}

// Полученный значок «выпрыгивает» при появлении экрана
@Composable
private fun Badge(goal: Int, earned: Boolean) {
    val p = palette
    val scale = remember(earned) { Animatable(if (earned) 0.6f else 1f) }

    LaunchedEffect(earned) {
        if (earned) scale.animateTo(1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow))
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                }
                .clip(CircleShape)
                .background(if (earned) p.mint else p.track),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "$goal",
                color = if (earned) p.onMint else p.textSoft,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(text = "дней", color = p.textSoft, fontSize = 12.sp)
    }
}

private fun monthTitle(month: YearMonth): String {
    val text = month.atDay(1).format(DateTimeFormatter.ofPattern("LLLL yyyy", Locale.forLanguageTag("ru")))
    return text.replaceFirstChar { it.uppercase() }
}

private fun pluralDays(n: Int): String {
    val lastTwo = n % 100
    val last = n % 10
    return when {
        lastTwo in 11..14 -> "дней"
        last == 1 -> "день"
        last in 2..4 -> "дня"
        else -> "дней"
    }
}
