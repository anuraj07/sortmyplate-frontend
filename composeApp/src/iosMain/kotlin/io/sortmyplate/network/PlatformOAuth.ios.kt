package io.sortmyplate.network

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AuthenticationServices.ASPresentationAnchor
import platform.AuthenticationServices.ASWebAuthenticationPresentationContextProvidingProtocol
import platform.AuthenticationServices.ASWebAuthenticationSession
import platform.AuthenticationServices.ASWebAuthenticationSessionErrorCodeCanceledLogin
import platform.AuthenticationServices.ASWebAuthenticationSessionErrorDomain
import platform.Foundation.NSURL
import platform.Foundation.NSURLComponents
import platform.Foundation.NSURLQueryItem
import platform.Foundation.NSUUID
import platform.UIKit.UIApplication
import platform.UIKit.UIWindow
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import kotlin.coroutines.resume

private var activeWebAuthSession: ASWebAuthenticationSession? = null
private var activePresentationProvider: KeyWindowPresentationProvider? = null

private class KeyWindowPresentationProvider : NSObject(), ASWebAuthenticationPresentationContextProvidingProtocol {
    @Suppress("DEPRECATION")
    override fun presentationAnchorForWebAuthenticationSession(
        session: ASWebAuthenticationSession
    ): ASPresentationAnchor {
        val app = UIApplication.sharedApplication
        val keyWindow = app.keyWindow
        if (keyWindow != null) return keyWindow
        val firstWindow = app.windows.firstOrNull() as? UIWindow
        return firstWindow ?: UIWindow()
    }
}

actual fun getOAuthPlatformTag(): String = "ios"

actual fun generateOAuthState(): String {
    val a = NSUUID().UUIDString().replace("-", "").lowercase()
    val b = NSUUID().UUIDString().replace("-", "").lowercase()
    return "$a$b".take(48)
}

actual fun getOAuthDeepLinkRedirect(): String? = "sortmyplate://auth/callback"

actual fun openGoogleOAuthBrowser(url: String): Boolean {
    val nsUrl = NSURL.URLWithString(url) ?: return false
    UIApplication.sharedApplication.openURL(
        nsUrl,
        options = emptyMap<Any?, Any?>(),
        completionHandler = null
    )
    return true
}

actual suspend fun signInInAppWithGoogle(
    googleClientId: String?,
    requestOAuthUrl: suspend (state: String, platform: String, redirectTo: String?) -> OAuthBrowserStart
): InAppGoogleSignInOutcome {
    val state = generateOAuthState()
    val browserStart = requestOAuthUrl(state, getOAuthPlatformTag(), getOAuthDeepLinkRedirect())
    if (!browserStart.unconfiguredMessage.isNullOrBlank()) {
        return InAppGoogleSignInOutcome.Error(browserStart.unconfiguredMessage, isOAuthUnconfigured = true)
    }
    val authUrl = browserStart.authUrl
    if (authUrl.isNullOrBlank()) {
        return InAppGoogleSignInOutcome.Error(browserStart.errorMessage ?: "Could not start Google Sign-In")
    }

    val nsUrl = NSURL.URLWithString(authUrl)
        ?: return InAppGoogleSignInOutcome.Error("Invalid Google OAuth URL returned by server.")

    return suspendCancellableCoroutine { continuation ->
        dispatch_async(dispatch_get_main_queue()) {
            val provider = KeyWindowPresentationProvider()
            val session = ASWebAuthenticationSession(
                uRL = nsUrl,
                callbackURLScheme = "sortmyplate"
            ) { callbackUrl, error ->
                activeWebAuthSession = null
                activePresentationProvider = null

                if (!continuation.isActive) return@ASWebAuthenticationSession

                if (error != null) {
                    val isCancelled = error.domain == ASWebAuthenticationSessionErrorDomain &&
                        error.code == ASWebAuthenticationSessionErrorCodeCanceledLogin
                    if (isCancelled) {
                        continuation.resume(InAppGoogleSignInOutcome.Cancelled)
                    } else {
                        continuation.resume(
                            InAppGoogleSignInOutcome.Error(error.localizedDescription)
                        )
                    }
                    return@ASWebAuthenticationSession
                }

                val rawCallback = callbackUrl?.absoluteString
                if (rawCallback.isNullOrBlank()) {
                    continuation.resume(
                        InAppGoogleSignInOutcome.AwaitingSessionPoll(
                            state = browserStart.state,
                            message = "Syncing Google Sign-In session..."
                        )
                    )
                    return@ASWebAuthenticationSession
                }

                val components = NSURLComponents.componentsWithString(rawCallback)
                val queryMap = buildMap<String, String> {
                    components?.queryItems?.forEach { item ->
                        val q = item as? NSURLQueryItem
                        val k = q?.name
                        val v = q?.value
                        if (!k.isNullOrBlank() && v != null) {
                            put(k, v)
                        }
                    }
                }

                val errParam = queryMap["error"]
                if (!errParam.isNullOrBlank()) {
                    continuation.resume(InAppGoogleSignInOutcome.Error("Google Sign-In failed: $errParam"))
                    return@ASWebAuthenticationSession
                }

                val returnedState = queryMap["state"] ?: browserStart.state
                val exchangeCode = queryMap["exchange_code"]
                if (!exchangeCode.isNullOrBlank()) {
                    continuation.resume(
                        InAppGoogleSignInOutcome.DeepLinkCallback(
                            state = returnedState,
                            exchangeCode = exchangeCode
                        )
                    )
                } else {
                    continuation.resume(
                        InAppGoogleSignInOutcome.AwaitingSessionPoll(
                            state = returnedState,
                            message = "Finishing Google Sign-In..."
                        )
                    )
                }
            }

            session.presentationContextProvider = provider
            session.prefersEphemeralWebBrowserSession = false
            activePresentationProvider = provider
            activeWebAuthSession = session

            if (!session.start()) {
                activeWebAuthSession = null
                activePresentationProvider = null
                if (continuation.isActive) {
                    continuation.resume(
                        InAppGoogleSignInOutcome.Error("Could not present in-app Apple authentication sheet.")
                    )
                }
            }
        }

        continuation.invokeOnCancellation {
            dispatch_async(dispatch_get_main_queue()) {
                activeWebAuthSession?.cancel()
                activeWebAuthSession = null
                activePresentationProvider = null
            }
        }
    }
}
