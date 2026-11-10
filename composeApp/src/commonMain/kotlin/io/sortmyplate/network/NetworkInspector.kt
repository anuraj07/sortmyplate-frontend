package io.sortmyplate.network

import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.observer.ResponseObserver
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.client.utils.EmptyContent
import io.ktor.http.Headers
import io.ktor.http.HeadersBuilder
import io.ktor.http.content.OutgoingContent
import io.ktor.http.content.TextContent
import io.ktor.http.encodedPath
import io.ktor.util.AttributeKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlin.time.TimeMark
import kotlin.time.TimeSource

enum class NetworkLogStatus {
    PENDING,
    SUCCESS,
    CLIENT_ERROR,
    SERVER_ERROR,
    FAILED
}

data class NetworkTransaction(
    val id: Long,
    val method: String,
    val url: String,
    val path: String,
    val requestHeaders: Map<String, String> = emptyMap(),
    val requestBody: String? = null,
    val statusCode: Int? = null,
    val statusDescription: String? = null,
    val responseHeaders: Map<String, String> = emptyMap(),
    val responseBody: String? = null,
    val errorMessage: String? = null,
    val durationMs: Long? = null,
    val status: NetworkLogStatus = NetworkLogStatus.PENDING
)

private val TX_ID_KEY = AttributeKey<Long>("SortMyPlateNetTxId")
private val START_MARK_KEY = AttributeKey<TimeMark>("SortMyPlateNetStartMark")
private const val MAX_STORED_TRANSACTIONS = 50
private const val MAX_BODY_CHARS = 16000

object NetworkInspectorStore {
    private val _idCounter = MutableStateFlow(0L)
    private val _transactions = MutableStateFlow<List<NetworkTransaction>>(emptyList())
    val transactions: StateFlow<List<NetworkTransaction>> = _transactions.asStateFlow()

    private val prettyJsonFormat = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    internal fun nextId(): Long = _idCounter.updateAndGet { it + 1L }

    internal fun recordRequest(
        id: Long,
        method: String,
        url: String,
        path: String,
        headers: Map<String, String>,
        body: String?
    ) {
        val formattedBody = formatPayload(body)
        val entry = NetworkTransaction(
            id = id,
            method = method,
            url = url,
            path = path.ifBlank { "/" },
            requestHeaders = headers,
            requestBody = formattedBody,
            status = NetworkLogStatus.PENDING
        )
        _transactions.update { list ->
            (listOf(entry) + list).take(MAX_STORED_TRANSACTIONS)
        }
        println("[KMP-NET #$id] --> $method $url")
    }

    internal fun recordResponse(
        id: Long,
        statusCode: Int,
        statusDescription: String,
        durationMs: Long?,
        headers: Map<String, String>,
        body: String?
    ) {
        val statusBucket = when (statusCode) {
            in 200..299 -> NetworkLogStatus.SUCCESS
            in 400..499 -> NetworkLogStatus.CLIENT_ERROR
            in 500..599 -> NetworkLogStatus.SERVER_ERROR
            else -> NetworkLogStatus.SUCCESS
        }
        val formattedBody = formatPayload(body)

        _transactions.update { list ->
            list.map { tx ->
                if (tx.id == id) {
                    tx.copy(
                        statusCode = statusCode,
                        statusDescription = statusDescription,
                        durationMs = durationMs,
                        responseHeaders = headers,
                        responseBody = formattedBody,
                        status = statusBucket
                    )
                } else {
                    tx
                }
            }
        }
        println("[KMP-NET #$id] <-- $statusCode $statusDescription (${durationMs ?: 0} ms)")
    }

    internal fun recordFailure(
        id: Long,
        durationMs: Long?,
        errorMessage: String
    ) {
        _transactions.update { list ->
            list.map { tx ->
                if (tx.id == id) {
                    tx.copy(
                        durationMs = durationMs,
                        errorMessage = errorMessage,
                        status = NetworkLogStatus.FAILED
                    )
                } else {
                    tx
                }
            }
        }
        println("[KMP-NET #$id] <-- FAILED (${durationMs ?: 0} ms): $errorMessage")
    }

    fun clear() {
        _transactions.value = emptyList()
    }

    private fun formatPayload(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        val clipped = if (raw.length > MAX_BODY_CHARS) {
            raw.take(MAX_BODY_CHARS) + "\n... [truncated ${raw.length - MAX_BODY_CHARS} chars]"
        } else {
            raw
        }
        val trimmed = clipped.trim()
        if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) {
            return clipped
        }
        return runCatching {
            val element = prettyJsonFormat.parseToJsonElement(trimmed)
            prettyJsonFormat.encodeToString(JsonElement.serializer(), element)
        }.getOrElse { clipped }
    }
}

private fun maskHeaderValue(name: String, value: String): String {
    val lower = name.lowercase()
    if (lower != "authorization" && lower != "x-api-key" && lower != "cookie" && lower != "set-cookie") {
        return value
    }
    if (lower == "authorization" && value.startsWith("Bearer ", ignoreCase = true)) {
        val token = value.substring(7).trim()
        val suffix = if (token.length > 6) token.takeLast(6) else "***"
        return "Bearer ••••••$suffix"
    }
    return if (value.length > 8) "••••••${value.takeLast(4)}" else "••••••"
}

private fun extractResponseHeaders(headers: Headers): Map<String, String> {
    return headers.entries().associate { (key, list) ->
        key to maskHeaderValue(key, list.joinToString(", "))
    }
}

private fun extractRequestHeaders(builder: HeadersBuilder): Map<String, String> {
    return builder.entries().associate { (key, list) ->
        key to maskHeaderValue(key, list.joinToString(", "))
    }
}

val NetworkInspectorPlugin = createClientPlugin("SortMyPlateNetworkInspector") {
    on(Send) { request ->
        val txId = NetworkInspectorStore.nextId()
        val startMark = TimeSource.Monotonic.markNow()
        request.attributes.put(TX_ID_KEY, txId)
        request.attributes.put(START_MARK_KEY, startMark)

        val requestBodyText = when (val body = request.body) {
            is TextContent -> body.text
            is OutgoingContent.ByteArrayContent -> body.bytes().decodeToString()
            is EmptyContent -> null
            else -> null
        }

        NetworkInspectorStore.recordRequest(
            id = txId,
            method = request.method.value,
            url = request.url.buildString(),
            path = request.url.encodedPath,
            headers = extractRequestHeaders(request.headers),
            body = requestBodyText
        )

        try {
            proceed(request)
        } catch (t: Throwable) {
            val elapsed = startMark.elapsedNow().inWholeMilliseconds
            NetworkInspectorStore.recordFailure(
                id = txId,
                durationMs = elapsed,
                errorMessage = t.message ?: t.toString()
            )
            throw t
        }
    }
}

fun HttpClientConfig<*>.installNetworkInspector() {
    if (!isDebugBuild()) return
    install(NetworkInspectorPlugin)
    install(ResponseObserver) {
        onResponse { response: HttpResponse ->
            val attrs = response.call.request.attributes
            val txId = attrs.getOrNull(TX_ID_KEY) ?: return@onResponse
            val elapsed = attrs.getOrNull(START_MARK_KEY)?.elapsedNow()?.inWholeMilliseconds
            val responseBodyText = runCatching { response.bodyAsText() }.getOrNull()

            NetworkInspectorStore.recordResponse(
                id = txId,
                statusCode = response.status.value,
                statusDescription = response.status.description,
                durationMs = elapsed,
                headers = extractResponseHeaders(response.headers),
                body = responseBodyText
            )
        }
    }
}
