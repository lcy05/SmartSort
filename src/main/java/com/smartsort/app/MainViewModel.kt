package com.smartsort.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smartsort.app.data.AppSettings
import com.smartsort.app.data.BinProtocol
import com.smartsort.app.data.DeviceEvent
import com.smartsort.app.data.DeviceRepository
import com.smartsort.app.data.NetRules
import com.smartsort.app.data.SettingsDraft
import com.smartsort.app.data.SettingsStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class Conn { ONLINE, CONNECTING, OFFLINE }

data class BinUi(
    val name: String,
    val available: Boolean = true,
    val openInferred: Boolean = false
)

private val BIN_NAMES = listOf("可回收物", "有害垃圾", "其他垃圾")

data class UiState(
    val conn: Conn = Conn.OFFLINE,
    val bins: List<BinUi> = BIN_NAMES.map { BinUi(it) },
    val focus: Int? = null,
    val autoClose: Boolean = true,
    val seconds: Int = 5,
    val remain: Float = 5f,
    val keepOpen: Boolean = false,
    val ssid: String = "ESP25",
    val pwd: String = "12345678",
    val host: String = "192.168.4.1",
    val port: Int = 8080,
    val lastFrame: String = "-"
) {
    val online: Boolean get() = conn == Conn.ONLINE
}

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = DeviceRepository()
    private val store = SettingsStore(app)

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    private val _toasts = Channel<String>(Channel.BUFFERED)
    val toasts = _toasts.receiveAsFlow()

    private var connJob: Job? = null
    private var timerJob: Job? = null
    private var saveJob: Job? = null
    private var pendingDraft: SettingsDraft? = null

    /** 最近一次 connect() 实际使用的 host/port，用来判断字段改动要不要重连 */
    private var connTarget: Pair<String, Int>? = null

    init {
        viewModelScope.launch {
            val saved = store.load()
            _state.update {
                it.copy(
                    ssid = saved.ssid, pwd = saved.pwd, host = saved.host, port = saved.port,
                    autoClose = saved.autoClose, seconds = saved.seconds, remain = saved.seconds.toFloat()
                )
            }
            connect()
        }
    }

    fun connect() {
        connJob?.cancel()
        // 主动关 socket，让阻塞 read 立刻退出；否则旧连接还能残留 1.5s
        repo.close()
        // 重连拿不到盖位，聚焦与倒计时一并作废，避免倒计时冻死在界面上
        forceClose()
        val snapshot = _state.value
        connTarget = snapshot.host to snapshot.port
        connJob = viewModelScope.launch {
            repo.stream(snapshot.host, snapshot.port)
                .flowOn(Dispatchers.IO)
                .collect { event -> handle(event) }
        }
    }

    private fun handle(event: DeviceEvent) {
        when (event) {
            DeviceEvent.Connecting -> _state.update { it.copy(conn = Conn.CONNECTING) }

            DeviceEvent.Connected -> {
                _state.update { it.copy(conn = Conn.ONLINE) }
                val s = _state.value
                toast("已连接 ${s.host}:${s.port}")
            }

            is DeviceEvent.Status -> _state.update { st ->
                val bins = st.bins.mapIndexed { index, bin ->
                    val available = event.status.availableAt(index)
                    bin.copy(available = available, openInferred = if (available) bin.openInferred else false)
                }
                st.copy(conn = Conn.ONLINE, bins = bins, lastFrame = event.raw)
            }

            is DeviceEvent.Lost -> {
                _state.update { it.copy(conn = Conn.OFFLINE) }
                forceClose()
            }
        }
    }

    fun openBin(index: Int) {
        val s = _state.value
        val target = s.bins[index]
        if (!s.online) {
            toast("设备未连接，请先连接")
            return
        }
        if (!target.available) {
            toast("${target.name}已满溢，固件拒绝开盖")
            return
        }
        viewModelScope.launch { repo.send(BinProtocol.open(index)) }
        _state.update { st ->
            st.copy(
                bins = st.bins.mapIndexed { i, bin -> if (i == index) bin.copy(openInferred = true) else bin },
                focus = index,
                keepOpen = false,
                remain = st.seconds.toFloat()
            )
        }
        if (s.autoClose) startTimer() else stopTimer()
    }

    fun closeBin(index: Int) {
        if (!_state.value.online) {
            toast("设备未连接")
            return
        }
        viewModelScope.launch { repo.send(BinProtocol.close(index)) }
        _state.update { st ->
            st.copy(bins = st.bins.mapIndexed { i, bin -> if (i == index) bin.copy(openInferred = false) else bin })
        }
        if (_state.value.focus == index) detachFocus()
        toast("已下发关盖 · ${_state.value.bins[index].name}")
    }

    fun keepOpen() {
        stopTimer()
        _state.update { it.copy(keepOpen = true, remain = 0f) }
        toast("已保持打开 · 取消自动关盖")
    }

    /**
     * 三桶全开：不受「自动关盖」影响。
     * 规则：自动关盖只作用于单桶聚焦开盖，全开必须由用户手动全关。
     * 因此这里同时取消任何进行中的倒计时并退出聚焦，避免旧计时器到点补发关盖。
     */
    fun allOpen() {
        val s = _state.value
        if (!s.online) {
            toast("设备未连接")
            return
        }
        val blocked = s.bins.filter { !it.available }.map { it.name }
        viewModelScope.launch { repo.send(BinProtocol.allOpen()) }
        stopTimer()
        _state.update { st ->
            st.copy(
                focus = null,
                keepOpen = false,
                remain = st.seconds.toFloat(),
                bins = st.bins.map { if (it.available) it.copy(openInferred = true) else it }
            )
        }
        toast(
            if (blocked.isEmpty()) "已下发全开 · 不自动关盖，请手动全关"
            else "已下发全开 · ${blocked.joinToString("、")} 已满跳过"
        )
    }

    fun allClose() {
        if (!_state.value.online) {
            toast("设备未连接")
            return
        }
        viewModelScope.launch { repo.send(BinProtocol.allClose()) }
        stopTimer()
        _state.update { st ->
            st.copy(focus = null, keepOpen = false, bins = st.bins.map { it.copy(openInferred = false) })
        }
        toast("已下发全关 · 三桶关盖")
    }

    // ---- 设置：改动即自动保存，连接参数变了自动重连 ----

    /** 每次改动都记下草稿并重置防抖计时；700ms 静默后才真正落盘。 */
    fun onDraft(draft: SettingsDraft) {
        pendingDraft = draft
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(SAVE_DEBOUNCE_MS)
            commit()
        }
    }

    /** 设置页关闭时立刻提交，跳过防抖，避免「改完就返回」丢改动。 */
    fun flush() {
        if (pendingDraft == null) return
        saveJob?.cancel()
        saveJob = viewModelScope.launch { commit() }
    }

    private suspend fun commit() {
        val draft = pendingDraft ?: return
        val old = _state.value

        // 非法输入沿用上次保存值：既不写盘也不重连，输入框里保留用户原文
        val host = draft.host.trim().takeIf { NetRules.isValidHost(it) } ?: old.host
        val port = draft.portText.trim().toIntOrNull()?.takeIf { it in 1..65535 } ?: old.port

        val next = AppSettings(
            ssid = draft.ssid,
            pwd = draft.pwd,
            host = host,
            port = port,
            autoClose = draft.autoClose,
            seconds = draft.seconds
        )
        if (next == old.toSettings()) return

        store.save(next)
        _state.update {
            it.copy(
                ssid = next.ssid, pwd = next.pwd, host = next.host, port = next.port,
                autoClose = next.autoClose, seconds = next.seconds,
                remain = if (it.focus == null) next.seconds.toFloat() else it.remain
            )
        }
        applyTimerRules(old.toSettings(), next)

        val target = connTarget
        if (target == null || target.first != next.host || target.second != next.port) {
            connect()
        }
    }

    /** 自动关盖开关、倒计时时长与正在跑的计时器保持一致。 */
    private fun applyTimerRules(old: AppSettings, next: AppSettings) {
        val s = _state.value
        if (old.autoClose && !next.autoClose) {
            stopTimer()
            if (s.focus != null) _state.update { it.copy(keepOpen = true, remain = 0f) }
        } else if (!old.autoClose && next.autoClose) {
            if (s.focus != null && !s.keepOpen) startTimer()
        }
        // 时长变了且计时中：按新时长重开，否则 FocusOverlay 的环形进度会跳变
        if (old.seconds != next.seconds && s.focus != null && timerJob?.isActive == true) {
            startTimer()
        }
    }

    private fun UiState.toSettings() = AppSettings(ssid, pwd, host, port, autoClose, seconds)

    /** 仅在单桶聚焦态启动；全开（focus == null）一律不启动倒计时。 */
    private fun startTimer() {
        stopTimer()
        if (_state.value.focus == null) return
        val total = _state.value.seconds.toFloat()
        timerJob = viewModelScope.launch {
            var left = total
            while (left > 0f) {
                delay(100)
                left -= 0.1f
                val value = if (left > 0f) left else 0f
                _state.update { it.copy(remain = value) }
            }
            val index = _state.value.focus ?: return@launch
            repo.send(BinProtocol.close(index))
            _state.update { st ->
                st.copy(
                    bins = st.bins.mapIndexed { i, bin -> if (i == index) bin.copy(openInferred = false) else bin },
                    focus = null,
                    keepOpen = false,
                    remain = st.seconds.toFloat()
                )
            }
            toast("倒计时结束 · 已下发关盖")
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun detachFocus() {
        stopTimer()
        _state.update { it.copy(focus = null, keepOpen = false, remain = it.seconds.toFloat()) }
    }

    private fun forceClose() {
        stopTimer()
        _state.update { st ->
            st.copy(
                focus = null,
                keepOpen = false,
                bins = st.bins.map { it.copy(openInferred = false) }
            )
        }
    }

    private fun toast(message: String) {
        _toasts.trySend(message)
    }

    override fun onCleared() {
        stopTimer()
        saveJob?.cancel()
        connJob?.cancel()
        repo.close()
        super.onCleared()
    }

    private companion object {
        /** 停止输入多久后落盘，兼顾「别打断打字」和「别丢改动」 */
        const val SAVE_DEBOUNCE_MS = 700L
    }
}
