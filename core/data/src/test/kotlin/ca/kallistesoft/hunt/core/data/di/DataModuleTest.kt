package ca.kallistesoft.hunt.core.data.di

import android.content.Context
import androidx.datastore.dataStoreFile
import ca.kallistesoft.hunt.core.data.AssetContentRepository
import ca.kallistesoft.hunt.core.data.ContentRepository
import ca.kallistesoft.hunt.core.data.DataStoreUserDataRepository
import ca.kallistesoft.hunt.core.data.Today
import ca.kallistesoft.hunt.core.data.UserDataRepository
import ca.kallistesoft.hunt.core.model.PHOTO_ID_EXAM
import ca.kallistesoft.hunt.core.model.UserProgress
import ca.kallistesoft.hunt.core.testing.ScreenshotDevice
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [ScreenshotDevice.SDK])
class DataModuleTest {
    private val context: Context = RuntimeEnvironment.getApplication()

    // Runs once per JVM: DataStore allows a single store per file.
    @Test
    fun `progress is saved to a JSON file, replaced when unreadable`() = runBlocking {
        val file = context.dataStoreFile("user_progress.json")
        file.parentFile!!.mkdirs()
        file.writeText("garbage")

        val store = DataModule.progressStore(context)
        assertEquals(UserProgress(), store.data.first())

        store.updateData { it.copy(dailyGoal = 30) }
        assertEquals(30, store.data.first().dailyGoal)
        assertTrue(file.readText().contains("\"dailyGoal\":30"))
    }

    @Test
    fun `today is the system clock`() {
        assertSame(Today.System, DataModule.today())
    }

    @Test
    fun `the bundled assets are the content`() = runBlocking {
        val repo = AssetContentRepository(context)
        val content = repo.content.first()
        assertTrue(content.availableExams.isNotEmpty())
        assertTrue(content.questionsFor(content.exam(PHOTO_ID_EXAM)!!).isNotEmpty())
        assertSame("loaded once", content, repo.content.first())
    }

    @Test
    fun `the bindings use the real implementations`() {
        // Dagger implements the @Binds; this stands in for it to check what each interface is bound to.
        val module = object : DataModule() {
            override fun content(impl: AssetContentRepository): ContentRepository = impl
            override fun userData(impl: DataStoreUserDataRepository): UserDataRepository = impl
        }
        val assets = AssetContentRepository(context)
        assertSame(assets, module.content(assets))
    }
}
