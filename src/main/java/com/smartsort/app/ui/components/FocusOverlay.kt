package com.smartsort.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartsort.app.BinUi
import com.smartsort.app.ui.theme.Palette

/**
 * 聚焦开盖层：图标从卡片原位放大到屏幕中心（FLIP），背景内容由外部 blur。
 * 自动模式显示倒计时 + 保持打开；手动/已保持打开只显示关闭。
 */
@Composable
fun FocusOverlay(
    index: Int,
    bin: BinUi,
    remain: Float,
    total: Float,
    autoClose: Boolean,
    keepOpen: Boolean,
    origin: Offset?,
    onClose: () -> Unit,
    onKeepOpen: () -> Unit
) {
    val progress = remember { Animatable(0f) }
    var glyphCenter by remember { mutableStateOf(Offset.Zero) }
    var delta by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(origin, glyphCenter) {
        if (origin != null && glyphCenter != Offset.Zero) {
            delta = origin - glyphCenter
            progress.snapTo(0f)
        }
        progress.animateTo(1f, tween(durationMillis = 440, easing = FastOutSlowInEasing))
    }

    val counting = autoClose && !keepOpen
    val hint = when {
        counting -> "倒计时结束自动关盖，可点「保持打开」取消"
        keepOpen -> "已取消自动关盖，请手动关闭"
        else -> "当前为手动模式，不会自动关盖"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xE6F4F6F3))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClose() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            // 桶名悬浮在桶身中间，不再压在盖子上方
            Box(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .size(130.dp, 150.dp)
                    .onGloballyPositioned {
                        if (glyphCenter == Offset.Zero) glyphCenter = it.boundsInRoot().center
                    }
                    .graphicsLayer {
                        val p = progress.value
                        translationX = delta.x * (1f - p)
                        translationY = delta.y * (1f - p)
                        val scale = 0.35f + 0.65f * p
                        scaleX = scale
                        scaleY = scale
                        alpha = 0.35f + 0.65f * p
                    },
                contentAlignment = Alignment.Center
            ) {
                BinGlyph(
                    index = index,
                    open = true,
                    modifier = Modifier.fillMaxSize()
                )
                Text(
                    text = bin.name,
                    style = TextStyle(
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 0.5.sp,
                        shadow = Shadow(
                            color = Color(0x66000000),
                            offset = Offset(0f, 1f),
                            blurRadius = 5f
                        )
                    ),
                    modifier = Modifier
                        .offset(y = 18.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0x3814211A))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                )
            }
            Text(
                text = hint,
                fontSize = 11.5.sp,
                color = Palette.Ink2,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp, start = 8.dp, end = 8.dp)
            )
            Spacer(Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                if (counting) {
                    CountdownButton(remain = remain, total = total, onClick = onClose)
                    KeepOpenButton(onClick = onKeepOpen)
                } else {
                    CloseButton(subtitle = if (keepOpen) "已保持打开" else "手动模式", onClick = onClose)
                }
            }
        }
    }
}

@Composable
private fun KeepOpenButton(onClick: () -> Unit) {
    Text(
        text = "保持打开",
        fontSize = 12.5.sp,
        fontWeight = FontWeight.Medium,
        color = Palette.Ink2,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Palette.Card)
            .border(1.dp, Palette.Line, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    )
}
