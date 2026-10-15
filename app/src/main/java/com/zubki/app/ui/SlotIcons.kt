package com.zubki.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import com.zubki.app.data.Slot
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SlotIcon(slot: Slot, modifier: Modifier = Modifier) {
    val p = palette
    Canvas(modifier) {
        when (slot) {
            Slot.Morning -> drawSun(p.sun)
            Slot.Evening -> drawMoon(p.moon)
        }
    }
}

private fun DrawScope.drawSun(color: Color) {
    val s = size.minDimension
    drawCircle(color, radius = s * 0.22f, center = center)

    repeat(8) { i ->
        val angle = Math.toRadians(i * 45.0)
        val dx = cos(angle).toFloat()
        val dy = sin(angle).toFloat()
        drawLine(
            color = color,
            start = Offset(center.x + dx * s * 0.34f, center.y + dy * s * 0.34f),
            end = Offset(center.x + dx * s * 0.46f, center.y + dy * s * 0.46f),
            strokeWidth = s * 0.07f,
            cap = StrokeCap.Round,
        )
    }
}

private fun DrawScope.drawMoon(color: Color) {
    val s = size.minDimension

    // Месяц: большой круг, из которого вырезан круг поменьше
    val cutout = Path().apply {
        addOval(Rect(center = Offset(s * 0.64f, s * 0.38f), radius = s * 0.28f))
    }
    clipPath(cutout, clipOp = ClipOp.Difference) {
        drawCircle(color, radius = s * 0.36f, center = Offset(s * 0.48f, s * 0.54f))
    }

    // Звёздочка в вырезе
    drawCircle(color, radius = s * 0.05f, center = Offset(s * 0.78f, s * 0.22f))
}
