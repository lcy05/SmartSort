package com.smartsort.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** TCP 流无消息边界，切帧逻辑必须容忍噪声、半帧与粘包。 */
class FrameSplitterTest {

    @Test
    fun feed_multipleFramesInOneChunk_returnsAllFrames() {
        val splitter = FrameSplitter()
        assertEquals(
            listOf("{\"tongk\":1}", "{\"tongk\":0}"),
            splitter.feed("noise{\"tongk\":1}{\"tongk\":0}")
        )
    }

    @Test
    fun feed_partialFrame_isHeldUntilComplete() {
        val splitter = FrameSplitter()
        assertTrue(splitter.feed("{\"tongk\":1,\"tongy\":").isEmpty())
        assertEquals(
            listOf("{\"tongk\":1,\"tongy\":0}", "{\"tongq\":1}"),
            splitter.feed("0}{\"tongq\":1}")
        )
    }

    @Test
    fun feed_nestedBraces_stayInsideOneFrame() {
        val splitter = FrameSplitter()
        assertEquals(listOf("{\"a\":{\"b\":1}}"), splitter.feed("{\"a\":{\"b\":1}}"))
    }

    @Test
    fun feed_chunkWithoutFrameHeader_isDiscarded() {
        val splitter = FrameSplitter()
        assertTrue(splitter.feed("AT+OK\r\nnoise").isEmpty())
    }

    @Test
    fun feed_oversizedIncompleteFrame_isDropped() {
        val splitter = FrameSplitter(maxBuffer = 16)
        assertTrue(splitter.feed("{\"a\":\"" + "x".repeat(40)).isEmpty())
        assertEquals(listOf("{\"b\":1}"), splitter.feed("{\"b\":1}"))
    }
}
