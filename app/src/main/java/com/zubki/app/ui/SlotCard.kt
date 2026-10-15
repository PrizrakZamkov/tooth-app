package com.zubki.app.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zubki.app.data.Slot

@Composable
fun SlotCard(slot: Slot, doneAt: String?, onClick: () -> Unit) {
    val p = palette
    val shape = RoundedCornerShape(28.dp)
    val background by animateColorAsState(
        targetValue = if (doneAt != null) p.cardDone else p.card,
        label = "cardBg",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .pressable(onClick)
            .shadow(elevation = 3.dp, shape = shape, clip = false)
            .clip(shape)
            .background(background)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SlotIcon(slot, Modifier.size(44.dp))
        Spacer(Modifier.width(16.dp))

        Column(Modifier.weight(1f)) {
            Text(
                text = slot.title,
                color = p.text,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Crossfade(targetState = doneAt, label = "subtitle") { time ->
                Text(
                    text = if (time != null) "почистил в $time" else "нажми, когда почистил",
                    color = p.textSoft,
                    fontSize = 14.sp,
                )
            }
        }

        CheckCircle(done = doneAt != null)
    }
}

@Composable
private fun CheckCircle(done: Boolean) {
    val p = palette
    val fill by animateColorAsState(
        targetValue = if (done) p.mintDeep else p.mintDeep.copy(alpha = 0f),
        label = "fill",
    )
    val edge by animateColorAsState(
        targetValue = if (done) p.mintDeep else p.textSoft.copy(alpha = 0.4f),
        label = "edge",
    )
    val tick by animateFloatAsState(
        targetValue = if (done) 1f else 0f,
        animationSpec = tween(280),
        label = "tick",
    )
    val pop by animateFloatAsState(
        targetValue = if (done) 1f else 0.9f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label = "pop",
    )

    Canvas(Modifier.size(40.dp).scale(pop)) {
        val radius = size.minDimension / 2 - 1.dp.toPx()
        drawCircle(fill, radius)
        drawCircle(edge, radius, style = Stroke(width = 2.dp.toPx()))

        if (tick > 0f) {
            val check = Path().apply {
                moveTo(size.width * 0.29f, size.height * 0.52f)
                lineTo(size.width * 0.44f, size.height * 0.67f)
                lineTo(size.width * 0.72f, size.height * 0.36f)
            }
            // Галочка «дорисовывается» вместе с tick от 0 до 1
            val measure = PathMeasure()
            measure.setPath(check, false)
            val part = Path()
            measure.getSegment(0f, measure.length * tick, part, true)

            drawPath(
                path = part,
                color = Color.White,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
        }
    }
}

// Карточка слегка сжимается под пальцем и пружинит обратно
@Composable
fun Modifier.pressable(onClick: () -> Unit): Modifier {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label = "press",
    )

    return this
        .scale(scale)
        .clickable(interactionSource = source, indication = null, onClick = onClick)
}
