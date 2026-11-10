package io.sortmyplate

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.sortmyplate.network.AndroidOAuthHolder
import io.sortmyplate.network.OAuthDeepLinkBus

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidOAuthHolder.appContext = applicationContext
        AndroidOAuthHolder.setActivity(this)
        intent?.dataString?.let { OAuthDeepLinkBus.emitUri(it) }
        enableEdgeToEdge()
        setContent {
            App()
        }
    }

    override fun onResume() {
        super.onResume()
        AndroidOAuthHolder.setActivity(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.dataString?.let { OAuthDeepLinkBus.emitUri(it) }
    }
}
