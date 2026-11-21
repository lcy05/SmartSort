package com.smartsort.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartsort.app.UiState
import com.smartsort.app.data.NetRules
import com.smartsort.app.data.SettingsDraft
import com.smartsort.app.ui.theme.Palette

@Composable
fun SettingsScreen(
    state: UiState,
    onBack: () -> Unit,
    onDraft: (SettingsDraft) -> Unit
) {
    var ssid by remember(state.ssid) { mutableStateOf(state.ssid) }
    var pwd by remember(state.pwd) { mutableStateOf(state.pwd) }
    var host by remember(state.host) { mutableStateOf(state.host) }
    var port by remember(state.port) { mutableStateOf(state.port.toString()) }
    var autoClose by remember(state.autoClose) { mutableStateOf(state.autoClose) }
    var seconds by remember(state.seconds) { mutableStateOf(state.seconds) }

    /** 每次改动都带上「本次新值」再交给 ViewModel，避免慢一拍 */
    fun emit(
        s: String = ssid,
        p: String = pwd,
        h: String = host,
        pt: String = port,
        a: Boolean = autoClose,
        sec: Int = seconds
    ) = onDraft(SettingsDraft(s, p, h, pt, a, sec))

    val hostError = host.isNotBlank() && !NetRules.isValidHost(host)
    val portError = port.isNotBlank() && !NetRules.isValidPort(port)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Palette.Bg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "返回",
                fontSize = 11.sp,
                color = Palette.Ink2,
                modifier = Modifier
                    .clip(RoundedCornerShape(9.dp))
                    .background(Palette.Card)
                    .border(1.dp, Palette.Line, RoundedCornerShape(9.dp))
                    .clickable { onBack() }
                    .padding(horizontal = 10.dp, vertical = 7.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(text = "设置", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Palette.Ink)
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            SettingsBox {
                BoxLabel("连接参数")
                Field("热点 SSID", ssid) { ssid = it; emit(s = it) }
                Field("热点密码", pwd, password = true) { pwd = it; emit(p = it) }
                Field(
                    label = "设备 IP",
                    value = host,
                    isError = hostError,
                    errorText = "格式应为 192.168.4.1 这类四段地址"
                ) { host = it; emit(h = it) }
                Field(
                    label = "端口",
                    value = port,
                    isError = portError,
                    errorText = "端口需为 1–65535"
                ) { port = it.filter { c -> c.isDigit() }; emit(pt = port) }
                Hint("SSID/密码用于手机连接设备热点，App 通过 IP:端口 建立 TCP 连接")
                Hint("改动自动保存，无需确认")
            }

            SettingsBox {
                BoxLabel("自动关盖")
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("开盖后自动关盖", fontSize = 12.5.sp, color = Palette.Ink, modifier = Modifier.weight(1f))
                    Switch(
                        checked = autoClose,
                        onCheckedChange = {
                            autoClose = it
                            emit(a = it)
                        }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("倒计时时长", fontSize = 12.5.sp, color = Palette.Ink, modifier = Modifier.weight(1f))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StepButton("−") {
                            if (seconds > 3) {
                                seconds--
                                emit(sec = seconds)
                            }
                        }
                        Text("$seconds 秒", fontSize = 12.5.sp, color = Palette.Ink2)
                        StepButton("+") {
                            if (seconds < 30) {
                                seconds++
                                emit(sec = seconds)
                            }
                        }
                    }
                }
                Hint("仅作用于单桶开盖的倒计时；三桶全开不自动关盖，需手动全关")
            }

            SettingsBox {
                Spacer(Modifier.height(2.dp))
                BoxLabel("原始报文")
                Mono(state.lastFrame)
                Spacer(Modifier.height(6.dp))
                Mono("上行 {\"mode\": 0, \"gaik\": 1} · 冒号后单空格")
                Hint("tongX 1=未满可投 0=满；rubbish 恒 0 无意义")
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun SettingsBox(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Palette.Card)
            .border(1.dp, Palette.Line, RoundedCornerShape(14.dp))
            .padding(horizontal = 13.dp, vertical = 12.dp),
        content = content
    )
}

@Composable
private fun BoxLabel(text: String) {
    Text(text = text, fontSize = 11.sp, color = Palette.Ink3, modifier = Modifier.padding(bottom = 2.dp))
}

@Composable
private fun Field(
    label: String,
    value: String,
    password: Boolean = false,
    isError: Boolean = false,
    errorText: String = "",
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 11.sp) },
        singleLine = true,
        isError = isError,
        textStyle = TextStyle(fontSize = 13.sp),
        visualTransformation = if (password) PasswordVisualTransformation() else VisualTransformation.None,
        supportingText = if (isError) {
            { Text(errorText, fontSize = 10.5.sp) }
        } else {
            null
        },
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
    )
}

@Composable
private fun Hint(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        color = Palette.Ink3,
        modifier = Modifier.padding(top = 6.dp)
    )
}

@Composable
private fun Mono(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        color = Palette.Ink2,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Palette.OffBg)
            .padding(8.dp)
    )
}

@Composable
private fun StepButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Palette.OffBg)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, fontSize = 13.sp, color = Palette.Ink2)
    }
}
