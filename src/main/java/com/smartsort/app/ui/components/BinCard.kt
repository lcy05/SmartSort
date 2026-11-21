package com.smartsort.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartsort.app.BinUi
import com.smartsort.app.ui.theme.Palette

@Composable
fun BinCard(
    bin: BinUi,
    index: Int,
    enabled: Boolean,
    onOpen: () -> Unit,
    onClose: () -> Unit,
    onGlyphPositioned: (Offset) -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    val full = !bin.available
    val background = if (bin.available) Palette.Card else Palette.FullCard
    val borderColor = if (bin.available) Palette.Line else Palette.FullGlow

    // 满溢：荧光红边框 + 外发光呼吸，扫视时第一眼能定位
    val glow = rememberInfiniteTransition(label = "fullGlow")
    val glowAlpha by glow.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(850), RepeatMode.Reverse),
        label = "glowAlpha"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (full) (5.dp * glowAlpha) else 0.dp,
                shape = shape,
                ambientColor = Palette.FullGlow,
                spotColor = Palette.FullGlow
            )
            .clip(shape)
            .background(background)
            .border(
                width = if (full) 1.5.dp else 1.dp,
                color = if (full) Palette.FullGlow.copy(alpha = glowAlpha) else borderColor,
                shape = shape
            )
            .clickable(enabled = enabled && bin.available) { onOpen() }
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BinGlyph(
            index = index,
            open = bin.openInferred,
            modifier = Modifier
                .size(38.dp, 46.dp)
                .onGloballyPositioned { onGlyphPositioned(it.boundsInRoot().center) }
        )

        Spacer(Modifier.width(10.dp))

        Column(Modifier.weight(1f)) {
            Text(
                text = bin.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Palette.Ink
            )
            Spacer(Modifier.height(5.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Chip(
                    text = if (bin.available) "可投放" else "已满溢",
                    background = if (bin.available) Palette.OkBg else Palette.FullBg,
                    content = if (bin.available) Palette.OkFg else Palette.FullFg
                )
                Chip(
                    text = if (bin.openInferred) "开盖中" else "已关闭",
                    background = if (bin.openInferred) Palette.ActBg else Palette.OffBg,
                    content = if (bin.openInferred) Palette.ActFg else Palette.OffFg
                )
            }
        }

        Spacer(Modifier.width(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            BinActionButton(
                text = "开盖",
                up = true,
                enabled = enabled && bin.available,
                primary = true,
                onClick = onOpen
            )
            BinActionButton(
                text = "关盖",
                up = false,
                enabled = enabled,
                primary = false,
                onClick = onClose
            )
        }
    }
}

@Composable
private fun Chip(text: String, background: androidx.compose.ui.graphics.Color, content: androidx.compose.ui.graphics.Color) {
    Text(
        text = text,
        fontSize = 11.sp,
        color = content,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(background)
            .padding(horizontal = 7.dp, vertical = 2.dp)
    )
}

@Composable
private fun BinActionButton(
    text: String,
    up: Boolean,
    enabled: Boolean,
    primary: Boolean,
    onClick: () -> Unit
) {
    val bg = if (enabled && primary) Palette.Primary else Palette.OffBg
    val fg = when {
        !enabled -> Palette.Ink3
        primary -> Color.White
        else -> Palette.Ink2
    }
    Column(
        modifier = Modifier
            .size(50.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .clickable(enabled = enabled, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Canvas(Modifier.size(22.dp)) {
            val stroke = 1.9.dp.toPx()
            // 桶口弧线
            val mouth = Path().apply {
                moveTo(size.width * 0.18f, size.height * 0.47f)
                quadraticBezierTo(size.width * 0.5f, size.height * 0.63f, size.width * 0.82f, size.height * 0.47f)
            }
            drawPath(mouth, fg, style = Stroke(width = stroke, cap = StrokeCap.Round))

            val cx = size.width * 0.5f
            if (up) {
                // 箭头在桶口之上：盖子抬起
                drawLine(fg, Offset(cx, size.height * 0.51f), Offset(cx, size.height * 0.19f), stroke, StrokeCap.Round)
                drawLine(fg, Offset(size.width * 0.34f, size.height * 0.34f), Offset(cx, size.height * 0.19f), stroke, StrokeCap.Round)
                drawLine(fg, Offset(size.width * 0.66f, size.height * 0.34f), Offset(cx, size.height * 0.19f), stroke, StrokeCap.Round)
            } else {
                // 箭头在桶口之下：盖子落下
                drawLine(fg, Offset(cx, size.height * 0.56f), Offset(cx, size.height * 0.88f), stroke, StrokeCap.Round)
                drawLine(fg, Offset(size.width * 0.34f, size.height * 0.73f), Offset(cx, size.height * 0.88f), stroke, StrokeCap.Round)
                drawLine(fg, Offset(size.width * 0.66f, size.height * 0.73f), Offset(cx, size.height * 0.88f), stroke, StrokeCap.Round)
            }
        }
        Spacer(Modifier.height(2.dp))
        Text(text = text, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = fg)
    }
}
