package io.sortmyplate.network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.okhttp.OkHttp
import java.util.concurrent.TimeUnit

actual fun getServerOrigin(): String = "https://j6tgemptpq36.ramnaymcloud.com"

// Uses OkHttp explicitly so Android Studio App Inspection -> Network Inspector captures all traffic
actual fun createPlatformHttpClient(configure: HttpClientConfig<*>.() -> Unit): HttpClient {
    return HttpClient(OkHttp) {
        engine {
            config {
                connectTimeout(15, TimeUnit.SECONDS)
                readTimeout(30, TimeUnit.SECONDS)
                writeTimeout(30, TimeUnit.SECONDS)
            }
        }
        configure()
    }
}
