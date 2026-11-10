package io.sortmyplate.network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.js.Js

actual fun getServerOrigin(): String = "https://j6tgemptpq36.ramnaymcloud.com"

// Uses Browser Fetch engine for WasmJs (visible in Browser DevTools Network tab)
actual fun createPlatformHttpClient(config: HttpClientConfig<*>.() -> Unit): HttpClient {
    return HttpClient(Js) {
        config()
    }
}
