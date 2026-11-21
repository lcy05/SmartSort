package com.smartsort.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

/**
 * 固件按固定偏移解析数值，冒号后的空格属于协议的一部分。
 * 本测试锁定指令字面量，避免改动时被无意破坏。
 */
class BinProtocolTest {

    @Test
    fun modeSwitch_usesSingleSpaceAfterColon() {
        assertEquals("{\"mode\": 0}", BinProtocol.modeManual())
    }

    @Test
    fun singleBinCommands_targetOnlyRequestedKey() {
        assertEquals("{\"mode\": 0, \"gaik\": 1}", BinProtocol.open(0))
        assertEquals("{\"mode\": 0, \"gaiy\": 0}", BinProtocol.close(1))
        assertEquals("{\"mode\": 0, \"gaiq\": 1}", BinProtocol.open(2))
    }

    @Test
    fun globalCommands_keepFirmwareKeyOrder() {
        assertEquals("{\"mode\": 0, \"gaik\": 1, \"gaiy\": 1, \"gaiq\": 1}", BinProtocol.allOpen())
        assertEquals("{\"mode\": 0, \"gaik\": 0, \"gaiy\": 0, \"gaiq\": 0}", BinProtocol.allClose())
    }

    @Test
    fun noCommand_usesCompactColon() {
        val commands = listOf(
            BinProtocol.modeManual(),
            BinProtocol.open(0),
            BinProtocol.close(0),
            BinProtocol.allOpen(),
            BinProtocol.allClose()
        )
        commands.forEach { command ->
            assertFalse("冒号后缺少空格：$command", REGEX_COMPACT_COLON.containsMatchIn(command))
        }
    }

    private companion object {
        val REGEX_COMPACT_COLON = Regex("\":[0-9]")
    }
}
