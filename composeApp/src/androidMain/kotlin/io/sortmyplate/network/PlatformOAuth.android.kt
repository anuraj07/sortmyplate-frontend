package io.sortmyplate.network

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import java.lang.ref.WeakReference
import java.security.SecureRandom

internal object AndroidOAuthHolder {
    var appContext: Context? = null
    private var activityRef: WeakReference<Activity>? = null

    fun setActivity(activity: Activity) {
        activityRef = WeakReference(activity)
    }

    fun currentActivity(): Activity? {
        val candidate = activityRef?.get() ?: return null
        return if (!candidate.isFinishing && !candidate.isDestroyed) candidate else null
    }
}

actual fun getOAuthPlatformTag(): String = "android"

actual fun generateOAuthState(): String {
    val bytes = ByteArray(24)
    SecureRandom().nextBytes(bytes)
    return bytes.joinToString("") { "%02x".format(it) }
}

actual fun getOAuthDeepLinkRedirect(): String? = "sortmyplate://auth/callback"

actual fun openGoogleOAuthBrowser(url: String): Boolean {
    val activity = AndroidOAuthHolder.currentActivity()
    if (activity != null) {
        val launchedCustomTab = runCatching {
            val schemeParams = CustomTabColorSchemeParams.Builder()
                .setToolbarColor(0xFF13251B.toInt())
                .build()
            val customTabsIntent = CustomTabsIntent.Builder()
                .setDefaultColorSchemeParams(schemeParams)
                .setShowTitle(true)
                .build()
            customTabsIntent.launchUrl(activity, Uri.parse(url))
            true
        }.getOrDefault(false)
        if (launchedCustomTab) return true
    }

    val context = activity ?: AndroidOAuthHolder.appContext ?: return false
    return runCatching {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        true
    }.getOrDefault(false)
}

actual suspend fun signInInAppWithGoogle(
    googleClientId: String?,
    requestOAuthUrl: suspend (state: String, platform: String, redirectTo: String?) -> OAuthBrowserStart
): InAppGoogleSignInOutcome {
    val activity = AndroidOAuthHolder.currentActivity()

    // Step 1: Try native in-app Google Credential Manager bottom sheet (zero browser switch)
    if (activity != null && !googleClientId.isNullOrBlank()) {
        try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(googleClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val response = CredentialManager.create(activity).getCredential(
                context = activity,
                request = request
            )

            val credential = response.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleCred = GoogleIdTokenCredential.createFrom(credential.data)
                if (googleCred.idToken.isNotBlank()) {
                    return InAppGoogleSignInOutcome.IdToken(googleCred.idToken)
                }
            }
        } catch (_: GetCredentialCancellationException) {
            return InAppGoogleSignInOutcome.Cancelled
        } catch (_: GetCredentialException) {
            // Fall through cleanly to in-app Custom Tabs OAuth sheet (e.g. emulator without Google account)
        } catch (_: Throwable) {
            // Fall through to in-app Custom Tabs OAuth sheet
        }
    }

    // Step 2: Launch in-app Custom Tabs modal overlay using backend OAuth flow
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
            message = "Sign in with Google inside the overlay tab to sync your profile."
        )
    } else {
        InAppGoogleSignInOutcome.Error("Unable to open Google sign-in sheet on this device.")
    }
}
