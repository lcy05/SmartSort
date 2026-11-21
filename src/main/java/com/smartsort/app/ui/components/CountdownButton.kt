package com.smartsort.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartsort.app.ui.theme.Palette
import java.util.Locale

/** 自动关盖倒计时按钮：环形进度 + 剩余秒数（0.1s 精度）。 */
@Composable
fun CountdownButton(remain: Float, total: Float, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(92.dp)
            .clip(CircleShape)
            .background(Palette.Card)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 4.dp.toPx()
            val inset = stroke / 2f
            val arcSize = Size(size.width - stroke, size.height - stroke)
            val topLeft = Offset(inset, inset)
            drawArc(
                color = Palette.Primary.copy(alpha = 0.14f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            val fraction = if (total <= 0f) 0f else (remain / total).coerceIn(0f, 1f)
            drawArc(
                color = Palette.Primary,
                startAngle = -90f,
                sweepAngle = 360f * fraction,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = String.format(Locale.CHINA, "%.1f", remain),
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Palette.Ink
            )
            Text(text = "秒后关", fontSize = 11.sp, color = Palette.Ink3)
        }
    }
}

/** 手动模式 / 已保持打开时的纯关闭按钮。 */
@Composable
fun CloseButton(subtitle: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(92.dp)
            .clip(CircleShape)
            .background(Palette.Primary)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "关闭", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.White)
            Text(text = subtitle, fontSize = 11.sp, color = Color.White.copy(alpha = 0.72f))
        }
    }
}
