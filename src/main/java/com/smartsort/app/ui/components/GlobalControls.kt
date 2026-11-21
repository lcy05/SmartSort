package com.smartsort.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartsort.app.ui.theme.Palette

@Composable
fun GlobalControls(enabled: Boolean, onAllOpen: () -> Unit, onAllClose: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        ActionButton(
            text = "三桶全开",
            enabled = enabled,
            primary = true,
            modifier = Modifier.weight(1f),
            onClick = onAllOpen
        )
        ActionButton(
            text = "三桶全关",
            enabled = enabled,
            primary = false,
            modifier = Modifier.weight(1f),
            onClick = onAllClose
        )
    }
}

@Composable
private fun ActionButton(
    text: String,
    enabled: Boolean,
    primary: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    val base = modifier.clip(shape)
    val styled = when {
        !enabled -> base.background(Palette.OffBg)
        primary -> base.background(Palette.Primary)
        else -> base.background(Palette.Card).border(1.dp, Palette.Line, shape)
    }
    Text(
        text = text,
        fontSize = 12.5.sp,
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Center,
        color = when {
            !enabled -> Palette.Ink3
            primary -> Color.White
            else -> Palette.Ink2
        },
        modifier = styled
            .clickable(enabled = enabled) { onClick() }
            .padding(vertical = 12.dp)
    )
}
