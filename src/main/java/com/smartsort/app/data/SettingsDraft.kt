package com.smartsort.app.data

/**
 * 设置页的编辑草稿。
 * host / port 刻意用 String：输入过程中的半成品（"192.168." / 空串）不能先被规范成 Int，
 * 否则「无效输入」这个信息在进入 ViewModel 之前就丢了。
 */
data class SettingsDraft(
    val ssid: String = "ESP25",
    val pwd: String = "12345678",
    val host: String = "192.168.4.1",
    val portText: String = "8080",
    val autoClose: Boolean = true,
    val seconds: Int = 5
)

/** 校验规则的唯一来源：UI 的 isError 和 ViewModel 的兜底都走这里。 */
object NetRules {

    fun isValidHost(raw: String): Boolean {
        val text = raw.trim()
        if (text == "0.0.0.0") return false
        val parts = text.split('.')
        if (parts.size != 4) return false
        return parts.all { p ->
            p.isNotEmpty() && p.length <= 3 && p.all { it.isDigit() } && p.toInt() in 0..255
        }
    }

    fun isValidPort(raw: String): Boolean {
        val v = raw.trim().toIntOrNull() ?: return false
        return v in 1..65535
    }
}
