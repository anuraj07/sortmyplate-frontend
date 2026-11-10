package io.sortmyplate.network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.darwin.Darwin
import kotlin.experimental.ExperimentalNativeApi

actual fun getServerOrigin(): String = "https://j6tgemptpq36.ramnaymcloud.com"

@OptIn(ExperimentalNativeApi::class)
actual fun isDebugBuild(): Boolean = Platform.isDebugBinary

// Uses Darwin (NSURLSession) explicitly for iOS native network profiling and ATS integration
actual fun createPlatformHttpClient(configure: HttpClientConfig<*>.() -> Unit): HttpClient {
    return HttpClient(Darwin) {
        engine {
            configureRequest {
                setAllowsCellularAccess(true)
            }
        }
        configure()
    }
}
