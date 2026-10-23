package ca.kallistesoft.hunt

import androidx.work.testing.WorkManagerTestInitHelper

/**
 * The real app, with a test WorkManager: Robolectric doesn't run androidx.startup, so WorkManager would be missing
 * when [BetterHunterApplication.onCreate] plans the reminders.
 */
class TestBetterHunterApplication : BetterHunterApplication() {
    override fun onCreate() {
        WorkManagerTestInitHelper.initializeTestWorkManager(this)
        super.onCreate()
    }
}
