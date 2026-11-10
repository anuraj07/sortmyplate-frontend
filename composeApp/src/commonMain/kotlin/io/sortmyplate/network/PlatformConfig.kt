package io.sortmyplate.network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig

expect fun getServerOrigin(): String

expect fun isDebugBuild(): Boolean

expect fun createPlatformHttpClient(configure: HttpClientConfig<*>.() -> Unit): HttpClient
