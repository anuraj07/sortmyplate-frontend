package io.sortmyplate

import androidx.compose.ui.window.ComposeUIViewController
import io.sortmyplate.network.OAuthDeepLinkBus
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController {
    App()
}

fun handleIncomingUrl(url: String) {
    OAuthDeepLinkBus.emitUri(url)
}
