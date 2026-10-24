package ca.kallistesoft.hunt

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import app.cash.turbine.test
import ca.kallistesoft.hunt.core.flags.FeatureFlag
import ca.kallistesoft.hunt.core.flags.FeatureFlags
import ca.kallistesoft.hunt.core.model.HunterType
import ca.kallistesoft.hunt.core.model.Province
import ca.kallistesoft.hunt.core.model.UserProgress
import ca.kallistesoft.hunt.core.model.Weapon
import ca.kallistesoft.hunt.core.testing.FakeUserDataRepository
import ca.kallistesoft.hunt.core.testing.MainDispatcherRule
import ca.kallistesoft.hunt.core.testing.awaitItemMatching
import ca.kallistesoft.hunt.core.update.ForceUpdateRepository
import ca.kallistesoft.hunt.core.update.ForceUpdateSettings
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class MainActivityViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val folder = TemporaryFolder()

    private val userData = FakeUserDataRepository()
    private val flags = CountingFeatureFlags()

    /** Remote Config says the minimum versionCode is [minVersionCode]; this build is versionCode 1. */
    private var minVersionCode: Long? = null

    private fun TestScope.viewModel() = MainActivityViewModel(
        userData,
        ForceUpdateRepository(
            PreferenceDataStoreFactory.create(scope = backgroundScope) { File(folder.root, "force_update.preferences_pb") },
            source = { minVersionCode },
            settings = ForceUpdateSettings(parameter = "min_version_code", currentVersionCode = 1, storePackage = "test"),
        ),
        flags,
    )

    @Test
    fun `the splash stays until the first screen is known`() = runTest {
        assertEquals(MainUiState.Loading, viewModel().uiState.value)
    }

    @Test
    fun `onboarding comes first until it's done`() = runTest {
        val viewModel = viewModel()
        viewModel.uiState.test {
            assertEquals(MainUiState.Ready(firstRun = true, updateRequired = false), awaitItemMatching { it != MainUiState.Loading })

            userData.saveProfile(HunterType.HUNTER, setOf(Weapon.BOW), Province.QC)
            assertEquals(MainUiState.Ready(firstRun = false, updateRequired = false), awaitItem())
        }
    }

    @Test
    fun `a build below the fetched minimum must update, and flags are refreshed too`() = runTest {
        userData.progress.value = UserProgress().withProfile(HunterType.HUNTER, setOf(Weapon.BOW), Province.QC)
        minVersionCode = 2
        val viewModel = viewModel()
        viewModel.uiState.test {
            assertEquals(MainUiState.Ready(firstRun = false, updateRequired = false), awaitItemMatching { it != MainUiState.Loading })

            viewModel.onForeground()
            assertEquals(MainUiState.Ready(firstRun = false, updateRequired = true), awaitItem())
            assertEquals(1, flags.refreshes)
        }
    }

    @Test
    fun `a failed fetch blocks nothing`() = runTest {
        val viewModel = viewModel()
        viewModel.uiState.test {
            awaitItemMatching { it != MainUiState.Loading }
            viewModel.onForeground()
            expectNoEvents()
            assertEquals(1, flags.refreshes)
        }
    }

    @Test
    fun `opening the app brings ignored reminders back to every day`() = runTest {
        userData.progress.value = UserProgress(ignoredReminders = 4)

        viewModel().onForeground()

        assertEquals(0, userData.progress.value.ignoredReminders)
    }

    private class CountingFeatureFlags : FeatureFlags {
        var refreshes = 0
        override val values = flowOf(FeatureFlag.entries.associateWith { it.defaultValue })

        override suspend fun refresh() {
            refreshes++
        }
    }
}
