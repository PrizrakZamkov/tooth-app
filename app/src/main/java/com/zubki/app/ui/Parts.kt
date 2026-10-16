package com.zubki.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Скруглённая карточка с лёгкой тенью
@Composable
fun SoftCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(28.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 3.dp, shape = shape, clip = false)
            .clip(shape)
            .background(palette.card)
            .padding(20.dp),
        content = content,
    )
}

// Кнопка-таблетка: либо мятная, либо спокойная
@Composable
fun PillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    filled: Boolean = true,
) {
    val p = palette

    Text(
        text = text,
        color = if (filled) p.onMint else p.text,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center,
        modifier = modifier
            .pressable(onClick)
            .clip(CircleShape)
            .background(if (filled) p.mint else p.cardDone)
            .padding(horizontal = 28.dp, vertical = 14.dp),
    )
}

// Переключатель в цветах приложения
@Composable
fun ZubkiSwitch(checked: Boolean, onChange: (Boolean) -> Unit, enabled: Boolean = true) {
    val p = palette

    Switch(
        checked = checked,
        onCheckedChange = onChange,
        enabled = enabled,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = p.mintDeep,
            checkedBorderColor = p.mintDeep,
            uncheckedThumbColor = p.textSoft,
            uncheckedTrackColor = p.track,
            uncheckedBorderColor = p.textSoft.copy(alpha = 0.4f),
            disabledCheckedThumbColor = Color.White,
            disabledCheckedTrackColor = p.mintDeep.copy(alpha = 0.5f),
            disabledCheckedBorderColor = p.mintDeep.copy(alpha = 0.5f),
        ),
    )
}
