package io.sortmyplate.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class OAuthDeepLinkResult(
    val state: String,
    val exchangeCode: String?,
    val error: String?
)

object OAuthDeepLinkBus {
    private val _pendingCallback = MutableStateFlow<OAuthDeepLinkResult?>(null)
    val pendingCallback: StateFlow<OAuthDeepLinkResult?> = _pendingCallback.asStateFlow()

    fun emitUri(rawUri: String) {
        val queryPart = rawUri.substringAfter("?", "")
        if (queryPart.isBlank()) return

        val params = queryPart.split("&").mapNotNull { pair ->
            val idx = pair.indexOf("=")
            if (idx <= 0) null
            else pair.substring(0, idx) to decodeQueryValue(pair.substring(idx + 1))
        }.toMap()

        val state = params["state"] ?: return
        val exchangeCode = params["exchange_code"]
        val error = params["error"]
        _pendingCallback.value = OAuthDeepLinkResult(
            state = state,
            exchangeCode = exchangeCode,
            error = error
        )
    }

    fun consume() {
        _pendingCallback.value = null
    }

    private fun decodeQueryValue(raw: String): String {
        val plusReplaced = raw.replace('+', ' ')
        val out = StringBuilder(plusReplaced.length)
        var i = 0
        while (i < plusReplaced.length) {
            val ch = plusReplaced[i]
            if (ch == '%' && i + 2 < plusReplaced.length) {
                val hex = plusReplaced.substring(i + 1, i + 3).toIntOrNull(16)
                if (hex != null) {
                    out.append(hex.toChar())
                    i += 3
                    continue
                }
            }
            out.append(ch)
            i += 1
        }
        return out.toString()
    }
}

expect fun getOAuthPlatformTag(): String

expect fun generateOAuthState(): String

expect fun getOAuthDeepLinkRedirect(): String?

expect fun openGoogleOAuthBrowser(url: String): Boolean

data class OAuthBrowserStart(
    val state: String,
    val authUrl: String? = null,
    val unconfiguredMessage: String? = null,
    val errorMessage: String? = null
)

sealed interface InAppGoogleSignInOutcome {
    data class IdToken(val idToken: String) : InAppGoogleSignInOutcome
    data class GoogleAccessToken(val accessToken: String) : InAppGoogleSignInOutcome
    data class DeepLinkCallback(val state: String, val exchangeCode: String) : InAppGoogleSignInOutcome
    data class AwaitingSessionPoll(val state: String, val message: String) : InAppGoogleSignInOutcome
    data object Cancelled : InAppGoogleSignInOutcome
    data class Error(val message: String, val isOAuthUnconfigured: Boolean = false) : InAppGoogleSignInOutcome
}

expect suspend fun signInInAppWithGoogle(
    googleClientId: String?,
    requestOAuthUrl: suspend (state: String, platform: String, redirectTo: String?) -> OAuthBrowserStart
): InAppGoogleSignInOutcome
