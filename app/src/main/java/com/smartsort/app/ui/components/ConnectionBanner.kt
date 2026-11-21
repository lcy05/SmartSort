package com.smartsort.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartsort.app.Conn
import com.smartsort.app.ui.theme.Palette

@Composable
fun ConnectionBanner(conn: Conn, onReconnect: () -> Unit) {
    val background = when (conn) {
        Conn.ONLINE -> Palette.PrimaryContainer
        Conn.CONNECTING -> Palette.WarnBg
        Conn.OFFLINE -> Palette.OffBg
    }
    val content = when (conn) {
        Conn.ONLINE -> Palette.Primary
        Conn.CONNECTING -> Palette.WarnFg
        Conn.OFFLINE -> Palette.OffFg
    }
    val label = when (conn) {
        Conn.ONLINE -> "设备在线"
        Conn.CONNECTING -> "正在连接设备…"
        Conn.OFFLINE -> "设备离线 · 请确认已连热点"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(background)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(6.dp).clip(CircleShape).background(content))
        Spacer(Modifier.width(8.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = content,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = if (conn == Conn.ONLINE) "重连" else "连接",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = content,
            modifier = Modifier
                .clip(RoundedCornerShape(7.dp))
                .background(Color.White.copy(alpha = 0.7f))
                .clickable { onReconnect() }
                .padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}
