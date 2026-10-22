package ca.kallistesoft.hunt.di

/**
 * Settings that differ between build types. Each build type provides its own from `src/debug` or `src/release`
 * (`BuildTypeModule`), so the code never branches on the build type.
 */
data class BuildTypeConfig(
    /** Force update rules (see README). */
    val updateConfigUrl: String,
    /** Minimum time between two Remote Config fetches. */
    val featureFlagFetchIntervalSeconds: Long,
)
