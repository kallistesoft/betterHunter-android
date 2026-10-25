package ca.kallistesoft.hunt

import android.content.Intent
import android.os.Bundle
import android.view.KeyboardShortcutGroup
import android.view.Menu
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import ca.kallistesoft.hunt.core.ads.AdConsent
import ca.kallistesoft.hunt.core.billing.Purchases
import ca.kallistesoft.hunt.core.designsystem.theme.BetterHunterTheme
import ca.kallistesoft.hunt.core.update.ForceUpdateScreen
import ca.kallistesoft.hunt.feature.quiz.quizShortcutGroup
import ca.kallistesoft.hunt.navigation.ReminderLink
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/** Single activity. AppCompat only provides the per-app language API; the UI is all Compose. */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel: MainActivityViewModel by viewModels()

    @Inject lateinit var adConsent: AdConsent

    @Inject lateinit var purchases: Purchases

    /** The back stack a reminder's notification asks for, until the app has opened it. */
    private var deepLink by mutableStateOf<List<NavKey>?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        // Only a fresh launch: after a rotation or process death the restored back stack wins.
        if (savedInstanceState == null) deepLink = ReminderLink.backStack(intent)
        splash.setKeepOnScreenCondition { viewModel.uiState.value is MainUiState.Loading }
        enableEdgeToEdge()
        // Ads consent (UMP) on each launch: the form shows only where it's required; ads start once allowed.
        adConsent.gatherConsent(this)
        // Remove ads (P15): prices, and purchases made or refunded while the app was closed.
        purchases.refresh()

        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            LifecycleEventEffect(Lifecycle.Event.ON_START) { viewModel.onForeground() }
            BetterHunterTheme {
                when (val ready = state) {
                    MainUiState.Loading -> Unit
                    is MainUiState.Ready ->
                        if (ready.updateRequired) {
                            ForceUpdateScreen(storePackage = BuildConfig.STORE_PACKAGE)
                        } else {
                            BetterHunterApp(
                                firstRun = ready.firstRun,
                                versionName = BuildConfig.VERSION_NAME,
                                deepLink = deepLink,
                                onDeepLinkOpened = { deepLink = null },
                            )
                        }
                }
            }
        }
    }

    /** A reminder tapped while the app is running. */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        ReminderLink.backStack(intent)?.let { deepLink = it }
    }

    /** Lists the quiz's keyboard shortcuts in the system's Keyboard Shortcut Helper (Meta + /). */
    override fun onProvideKeyboardShortcuts(data: MutableList<KeyboardShortcutGroup>, menu: Menu?, deviceId: Int) {
        super.onProvideKeyboardShortcuts(data, menu, deviceId)
        data += quizShortcutGroup(this)
    }
}
