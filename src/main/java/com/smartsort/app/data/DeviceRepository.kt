package com.smartsort.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException
import kotlin.coroutines.coroutineContext
import kotlin.math.min

sealed interface DeviceEvent {
    data object Connecting : DeviceEvent
    data object Connected : DeviceEvent
    data class Status(val status: BinStatus, val raw: String) : DeviceEvent
    data class Lost(val reason: String) : DeviceEvent
}

/**
 * TCP 连接管理：连接 → 切手动模式 → 持续读 500ms 状态帧 → 掉线退避重连。
 * 固件只处理 link 0，因此单连接即可。
 */
class DeviceRepository {

    private var socket: Socket? = null

    @Volatile
    private var output: OutputStream? = null

    private val writeLock = Mutex()

    fun stream(host: String, port: Int): Flow<DeviceEvent> = flow {
        var attempt = 0
        while (coroutineContext.isActive) {
            emit(DeviceEvent.Connecting)
            val client = Socket()
            try {
                client.connect(InetSocketAddress(host, port), CONNECT_TIMEOUT_MS)
                client.soTimeout = READ_TIMEOUT_MS
                client.tcpNoDelay = true
                socket = client
                output = client.getOutputStream()

                attempt = 0
                emit(DeviceEvent.Connected)
                // 固件 s_mode 初值为 1（自动），必须先切手动，远程指令才生效
                send(BinProtocol.modeManual())

                val input = client.getInputStream()
                val splitter = FrameSplitter()
                val buffer = ByteArray(1024)

                while (coroutineContext.isActive) {
                    val read = try {
                        input.read(buffer)
                    } catch (e: SocketTimeoutException) {
                        // 500ms 应有一帧，超过 1.5s 无数据视为掉线
                        -1
                    }
                    if (read <= 0) {
                        emit(DeviceEvent.Lost("设备无响应"))
                        break
                    }
                    val chunk = String(buffer, 0, read, Charsets.UTF_8)
                    for (frame in splitter.feed(chunk)) {
                        val status = BinProtocol.parse(frame)
                        if (status != null) {
                            emit(DeviceEvent.Status(status, frame))
                        }
                    }
                }
            } catch (e: IOException) {
                emit(DeviceEvent.Lost(e.message ?: "连接失败"))
            } finally {
                runCatching { client.close() }
                // 只清理自己：重连时旧流可能晚 1.5s 才走到这里，
                // 无脑置 null 会把新连接的 output 抹掉，之后所有指令静默丢弃。
                if (socket === client) {
                    socket = null
                    output = null
                }
            }

            attempt++
            val backoff = min(MAX_BACKOFF_MS, 1000L shl min(attempt, 3))
            delay(backoff)
        }
    }

    suspend fun send(command: String): Boolean = withContext(Dispatchers.IO) {
        writeLock.withLock {
            val out = output ?: return@withLock false
            try {
                out.write(command.toByteArray(Charsets.UTF_8))
                out.flush()
                true
            } catch (e: IOException) {
                false
            }
        }
    }

    fun close() {
        runCatching { socket?.close() }
        socket = null
        output = null
    }

    private companion object {
        const val CONNECT_TIMEOUT_MS = 3000
        const val READ_TIMEOUT_MS = 1500
        const val MAX_BACKOFF_MS = 8000L
    }
}
