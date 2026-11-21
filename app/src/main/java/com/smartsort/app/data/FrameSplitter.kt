package com.smartsort.app.data

/**
 * TCP 流没有消息边界，固件每 500ms 发一帧 JSON 且不带换行，
 * 因此用花括号配对切帧，并丢弃残缺/噪声数据。
 */
class FrameSplitter(private val maxBuffer: Int = 4096) {

    private val buffer = StringBuilder()

    fun feed(chunk: String): List<String> {
        if (chunk.isEmpty()) return emptyList()
        buffer.append(chunk)

        val frames = ArrayList<String>(2)
        while (true) {
            val start = buffer.indexOf("{")
            if (start < 0) {
                // 没有任何帧头，整段丢弃（可能是 AT 回显残留）
                buffer.setLength(0)
                break
            }
            if (start > 0) buffer.delete(0, start)

            var depth = 0
            var end = -1
            for (i in 0 until buffer.length) {
                when (buffer[i]) {
                    '{' -> depth++
                    '}' -> {
                        depth--
                        if (depth == 0) {
                            end = i
                            break
                        }
                    }
                }
            }

            if (end < 0) {
                // 半帧，等下一次 read
                if (buffer.length > maxBuffer) buffer.setLength(0)
                break
            }

            frames.add(buffer.substring(0, end + 1))
            buffer.delete(0, end + 1)
        }
        return frames
    }

    fun reset() = buffer.setLength(0)
}
