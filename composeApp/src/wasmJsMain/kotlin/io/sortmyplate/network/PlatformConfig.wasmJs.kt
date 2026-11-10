package io.sortmyplate.network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.js.Js
import kotlinx.browser.window

actual fun getServerOrigin(): String = "https://j6tgemptpq36.ramnaymcloud.com"

actual fun isDebugBuild(): Boolean {
    val host = window.location.hostname.lowercase()
    return host == "localhost" ||
        host == "127.0.0.1" ||
        host == "0.0.0.0" ||
        window.location.search.contains("debug=1")
}

// Uses Browser Fetch engine for WasmJs (visible in Browser DevTools Network tab)
actual fun createPlatformHttpClient(configure: HttpClientConfig<*>.() -> Unit): HttpClient {
    return HttpClient(Js) {
        configure()
    }
}
