package com.smartsort.app.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class AppSettings(
    val ssid: String = "ESP25",
    val pwd: String = "12345678",
    val host: String = "192.168.4.1",
    val port: Int = 8080,
    val autoClose: Boolean = true,
    val seconds: Int = 5
)

/**
 * 配置项为六个标量，使用 SharedPreferences 承载即可：
 * 读写均在 IO 调度器上完成，不引入额外的序列化运行时与原生库，有利于控制安装体积。
 */
class SettingsStore(context: Context) {

    private val sp = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    suspend fun load(): AppSettings = withContext(Dispatchers.IO) {
        AppSettings(
            ssid = sp.getString(KEY_SSID, null) ?: "ESP25",
            pwd = sp.getString(KEY_PWD, null) ?: "12345678",
            host = sp.getString(KEY_HOST, null) ?: "192.168.4.1",
            port = sp.getInt(KEY_PORT, 8080),
            autoClose = sp.getBoolean(KEY_AUTO, true),
            seconds = sp.getInt(KEY_SECONDS, 5)
        )
    }

    suspend fun save(settings: AppSettings) = withContext(Dispatchers.IO) {
        sp.edit()
            .putString(KEY_SSID, settings.ssid)
            .putString(KEY_PWD, settings.pwd)
            .putString(KEY_HOST, settings.host)
            .putInt(KEY_PORT, settings.port)
            .putBoolean(KEY_AUTO, settings.autoClose)
            .putInt(KEY_SECONDS, settings.seconds)
            .commit()
    }

    private companion object {
        const val FILE = "smartsort_settings"
        const val KEY_SSID = "ssid"
        const val KEY_PWD = "pwd"
        const val KEY_HOST = "host"
        const val KEY_PORT = "port"
        const val KEY_AUTO = "auto_close"
        const val KEY_SECONDS = "seconds"
    }
}
