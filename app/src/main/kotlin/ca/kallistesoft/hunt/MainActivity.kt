package ca.kallistesoft.hunt

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ca.kallistesoft.hunt.core.designsystem.theme.BetterHunterTheme
import ca.kallistesoft.hunt.core.update.ForceUpdateScreen
import dagger.hilt.android.AndroidEntryPoint

/** Single activity. AppCompat only provides the per-app language API; the UI is all Compose. */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        splash.setKeepOnScreenCondition { viewModel.uiState.value is MainUiState.Loading }
        enableEdgeToEdge()

        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            LifecycleEventEffect(Lifecycle.Event.ON_START) { viewModel.refreshUpdateRules() }
            BetterHunterTheme {
                when (val ready = state) {
                    MainUiState.Loading -> Unit
                    is MainUiState.Ready ->
                        if (ready.updateRequired) {
                            ForceUpdateScreen(storePackage = BuildConfig.STORE_PACKAGE)
                        } else {
                            BetterHunterApp(firstRun = ready.firstRun, versionName = BuildConfig.VERSION_NAME)
                        }
                }
            }
        }
    }
}
