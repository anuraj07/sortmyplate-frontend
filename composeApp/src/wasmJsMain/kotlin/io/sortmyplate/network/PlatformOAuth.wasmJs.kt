package io.sortmyplate.network

import kotlinx.browser.window
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.random.Random
import kotlin.time.TimeSource

private fun smpStartGisInPage(
    clientId: String,
    callback: (String, String) -> Unit
): Boolean = js("typeof window.smpStartGoogleSignIn === 'function' ? window.smpStartGoogleSignIn(clientId, callback) : false")

actual fun getOAuthPlatformTag(): String = "wasm"

actual fun generateOAuthState(): String {
    val seedMix = TimeSource.Monotonic.markNow().elapsedNow().inWholeNanoseconds xor Random.nextLong()
    val seeded = Random(seedMix)
    val hexChars = "0123456789abcdef"
    return buildString(48) {
        repeat(48) {
            append(hexChars[seeded.nextInt(16)])
        }
    }
}

actual fun getOAuthDeepLinkRedirect(): String? = null

actual fun openGoogleOAuthBrowser(url: String): Boolean {
    return runCatching {
        val width = 500
        val height = 620
        val left = (window.screenX + (window.outerWidth - width) / 2).coerceAtLeast(0)
        val top = (window.screenY + (window.outerHeight - height) / 2).coerceAtLeast(0)
        val features = "width=$width,height=$height,left=$left,top=$top,menubar=no,toolbar=no,status=no"
        window.open(url, "sortmyplate_google_oauth", features) != null
    }.getOrDefault(false)
}

actual suspend fun signInInAppWithGoogle(
    googleClientId: String?,
    requestOAuthUrl: suspend (state: String, platform: String, redirectTo: String?) -> OAuthBrowserStart
): InAppGoogleSignInOutcome {
    // Step 1: Try Google Identity Services in-page sign-in (keeps user on the website)
    if (!googleClientId.isNullOrBlank()) {
        val gisOutcome = suspendCancellableCoroutine<InAppGoogleSignInOutcome?> { continuation ->
            val started = runCatching {
                smpStartGisInPage(googleClientId) { resultType, value ->
                    if (!continuation.isActive) return@smpStartGisInPage
                    when (resultType) {
                        "id_token" -> continuation.resume(InAppGoogleSignInOutcome.IdToken(value))
                        "access_token" -> continuation.resume(InAppGoogleSignInOutcome.GoogleAccessToken(value))
                        "cancelled" -> continuation.resume(InAppGoogleSignInOutcome.Cancelled)
                        else -> continuation.resume(InAppGoogleSignInOutcome.Error(value))
                    }
                }
            }.getOrDefault(false)

            if (!started && continuation.isActive) {
                continuation.resume(null)
            }
        }
        if (gisOutcome != null) {
            return gisOutcome
        }
    }

    // Step 2: Centered OAuth popup window leaving the Wasm tab intact
    val state = generateOAuthState()
    val browserStart = requestOAuthUrl(state, getOAuthPlatformTag(), getOAuthDeepLinkRedirect())
    if (!browserStart.unconfiguredMessage.isNullOrBlank()) {
        return InAppGoogleSignInOutcome.Error(browserStart.unconfiguredMessage, isOAuthUnconfigured = true)
    }
    val authUrl = browserStart.authUrl
    if (authUrl.isNullOrBlank()) {
        return InAppGoogleSignInOutcome.Error(browserStart.errorMessage ?: "Could not start Google Sign-In")
    }

    return if (openGoogleOAuthBrowser(authUrl)) {
        InAppGoogleSignInOutcome.AwaitingSessionPoll(
            state = browserStart.state,
            message = "Complete Google Sign-In in the popup window while keeping your tab open."
        )
    } else {
        InAppGoogleSignInOutcome.Error("Popup window was blocked. Please allow sign-in windows and retry.")
    }
}
