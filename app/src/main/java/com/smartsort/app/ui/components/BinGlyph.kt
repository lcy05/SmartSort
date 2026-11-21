package com.smartsort.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import com.smartsort.app.ui.theme.Palette

/**
 * 纯矢量垃圾桶图标，盖子绕左上铰链旋转。open=true 时翻开 58°。
 * 形状与 HTML 原型一致，保证设计稿与真机观感统一。
 */
@Composable
fun BinGlyph(
    index: Int,
    open: Boolean,
    modifier: Modifier = Modifier
) {
    val angle by animateFloatAsState(
        targetValue = if (open) -58f else 0f,
        animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
        label = "lid"
    )
    val body = Palette.binBody(index)
    val lid = Palette.binLid(index)

    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val top = h * 0.30f

        val tub = Path().apply {
            moveTo(w * 0.23f, top)
            lineTo(w * 0.77f, top)
            lineTo(w * 0.72f, h * 0.93f)
            quadraticBezierTo(w * 0.715f, h * 0.99f, w * 0.65f, h * 0.99f)
            lineTo(w * 0.35f, h * 0.99f)
            quadraticBezierTo(w * 0.285f, h * 0.99f, w * 0.28f, h * 0.93f)
            close()
        }
        drawPath(tub, body)
        drawRect(
            color = Color.Black.copy(alpha = 0.10f),
            topLeft = Offset(w * 0.23f, top),
            size = Size(w * 0.54f, h * 0.05f)
        )

        val rib = Color.White.copy(alpha = 0.30f)
        drawLine(rib, Offset(w * 0.40f, h * 0.46f), Offset(w * 0.38f, h * 0.86f), strokeWidth = w * 0.045f, cap = StrokeCap.Round)
        drawLine(rib, Offset(w * 0.50f, h * 0.46f), Offset(w * 0.50f, h * 0.86f), strokeWidth = w * 0.045f, cap = StrokeCap.Round)
        drawLine(rib, Offset(w * 0.60f, h * 0.46f), Offset(w * 0.62f, h * 0.86f), strokeWidth = w * 0.045f, cap = StrokeCap.Round)

        rotate(degrees = angle, pivot = Offset(w * 0.21f, h * 0.27f)) {
            drawRoundRect(
                color = lid,
                topLeft = Offset(w * 0.17f, h * 0.13f),
                size = Size(w * 0.66f, h * 0.11f),
                cornerRadius = CornerRadius(h * 0.055f)
            )
            drawRoundRect(
                color = lid,
                topLeft = Offset(w * 0.40f, h * 0.055f),
                size = Size(w * 0.20f, h * 0.08f),
                cornerRadius = CornerRadius(h * 0.04f)
            )
        }
    }
}
