package com.zubki.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DayRing(done: Int, total: Int, modifier: Modifier = Modifier) {
    val p = palette
    val complete = done == total

    val progress by animateFloatAsState(
        targetValue = done.toFloat() / total,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessLow),
        label = "ring",
    )
    val color by animateColorAsState(
        targetValue = if (complete) p.mintDeep else p.mint,
        label = "ringColor",
    )

    // Когда оба раза отмечены, кольцо коротко «подпрыгивает»
    val pulse = remember { Animatable(1f) }
    LaunchedEffect(complete) {
        if (complete) {
            pulse.animateTo(1.06f, tween(120))
            pulse.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }
    }

    Box(
        modifier = modifier
            .size(210.dp)
            .graphicsLayer {
                scaleX = pulse.value
                scaleY = pulse.value
            },
        contentAlignment = Alignment.Center,
    ) {
        Ring(progress, color, Modifier.fillMaxSize())

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AnimatedContent(
                targetState = done,
                transitionSpec = {
                    val dir = if (targetState > initialState) 1 else -1
                    (slideInVertically { dir * it / 2 } + fadeIn()) togetherWith
                        (slideOutVertically { -dir * it / 2 } + fadeOut())
                },
                label = "count",
            ) { n ->
                Text(
                    text = "$n/$total",
                    color = p.text,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Crossfade(targetState = done, label = "caption") { n ->
                Text(
                    text = caption(n, total),
                    color = p.textSoft,
                    fontSize = 14.sp,
                )
            }
        }
    }
}

// Кольцо прогресса: серая дорожка и цветная дуга поверх неё
@Composable
fun Ring(progress: Float, color: Color, modifier: Modifier = Modifier) {
    val track = palette.track

    Canvas(modifier) {
        val stroke = 20.dp.toPx()
        val topLeft = Offset(stroke / 2, stroke / 2)
        val arcSize = Size(size.width - stroke, size.height - stroke)
        val style = Stroke(width = stroke, cap = StrokeCap.Round)

        drawArc(track, 0f, 360f, false, topLeft, arcSize, style = style)

        val sweep = 360f * progress.coerceIn(0f, 1f)
        if (sweep > 0f) {
            drawArc(color, -90f, sweep, false, topLeft, arcSize, style = style)
        }
    }
}

private fun caption(done: Int, total: Int) = when (done) {
    0 -> "пока пусто"
    total -> "на сегодня всё"
    else -> "осталась одна"
}
