package com.smartsort.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartsort.app.UiState
import com.smartsort.app.ui.components.BinCard
import com.smartsort.app.ui.components.ConnectionBanner
import com.smartsort.app.ui.components.FocusOverlay
import com.smartsort.app.ui.components.GlobalControls
import com.smartsort.app.ui.theme.Palette

@Composable
fun MainScreen(
    state: UiState,
    onOpen: (Int) -> Unit,
    onClose: (Int) -> Unit,
    onAllOpen: () -> Unit,
    onAllClose: () -> Unit,
    onKeepOpen: () -> Unit,
    onReconnect: () -> Unit,
    onSettings: () -> Unit
) {
    val glyphPositions = remember { mutableStateMapOf<Int, Offset>() }
    val focused = state.focus != null

    Box(Modifier.fillMaxSize().background(Palette.Bg)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
                .blur(
                    radius = if (focused) 9.dp else 0.dp,
                    edgeTreatment = BlurredEdgeTreatment.Unbounded
                )
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "智能垃圾桶",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Palette.Ink,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "设置",
                    fontSize = 11.sp,
                    color = Palette.Ink2,
                    modifier = Modifier
                        .clip(RoundedCornerShape(9.dp))
                        .background(Palette.Card)
                        .border(1.dp, Palette.Line, RoundedCornerShape(9.dp))
                        .clickable { onSettings() }
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                )
            }

            ConnectionBanner(conn = state.conn, onReconnect = onReconnect)
            Spacer(Modifier.height(4.dp))

            SectionLabel("垃圾桶状态")

            state.bins.forEachIndexed { index, bin ->
                BinCard(
                    bin = bin,
                    index = index,
                    enabled = state.online,
                    onOpen = { onOpen(index) },
                    onClose = { onClose(index) },
                    onGlyphPositioned = { center -> glyphPositions[index] = center }
                )
                Spacer(Modifier.height(8.dp))
            }

            SectionLabel("全局控制")
            GlobalControls(
                enabled = state.online,
                onAllOpen = onAllOpen,
                onAllClose = onAllClose
            )
            Spacer(Modifier.height(16.dp))
        }

        val focusIndex = state.focus
        if (focusIndex != null) {
            FocusOverlay(
                index = focusIndex,
                bin = state.bins[focusIndex],
                remain = state.remain,
                total = state.seconds.toFloat(),
                autoClose = state.autoClose,
                keepOpen = state.keepOpen,
                origin = glyphPositions[focusIndex],
                onClose = { onClose(focusIndex) },
                onKeepOpen = onKeepOpen
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 7.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Text(text = text, fontSize = 11.sp, color = Palette.Ink3)
    }
}
