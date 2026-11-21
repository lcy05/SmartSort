package com.smartsort.app.data

import org.json.JSONObject

/**
 * 设备状态快照。available = true 表示该桶未满、可投放（固件字段 tongX == 1）。
 */
data class BinStatus(
    val k: Boolean,
    val y: Boolean,
    val q: Boolean
) {
    fun availableAt(index: Int): Boolean = when (index) {
        0 -> k
        1 -> y
        else -> q
    }
}

/**
 * 与 STM32 固件（BSP/esp8266.c）对接的协议层，固件零改动。
 *
 * 关键约束：固件 Esp_ParseNum() 用 `p += strlen(key) + 3` 固定偏移取值，
 * 冒号后必须恰好一个空格。`{"mode":0}` 会被解析成 0（误判为关盖），
 * 只有 `{"mode": 0}` 才正确。因此下方所有指令都携带 ": " 分隔。
 */
object BinProtocol {

    /** 桶顺序与固件一致：0=可回收(k)、1=有害(y)、2=其他(q)。 */
    private val KEYS = arrayOf("gaik", "gaiy", "gaiq")

    /** 切手动模式：固件 s_mode 初值为 1（自动），远程指令会被忽略。 */
    fun modeManual(): String = "{\"mode\": 0}"

    fun open(index: Int): String = "{\"mode\": 0, \"${KEYS[index]}\": 1}"

    fun close(index: Int): String = "{\"mode\": 0, \"${KEYS[index]}\": 0}"

    fun allOpen(): String = "{\"mode\": 0, \"gaik\": 1, \"gaiy\": 1, \"gaiq\": 1}"

    fun allClose(): String = "{\"mode\": 0, \"gaik\": 0, \"gaiy\": 0, \"gaiq\": 0}"

    /**
     * 解析 500ms 状态帧：{"tongk":1,"tongy":0,"tongq":1,"rubbish":0}
     * 返回 null 表示不是状态帧（可能是回显或噪声）。
     */
    fun parse(frame: String): BinStatus? {
        return try {
            val obj = JSONObject(frame)
            if (!obj.has("tongk") && !obj.has("tongy") && !obj.has("tongq")) return null
            BinStatus(
                k = obj.optInt("tongk", 1) == 1,
                y = obj.optInt("tongy", 1) == 1,
                q = obj.optInt("tongq", 1) == 1
            )
        } catch (e: Exception) {
            null
        }
    }
}
