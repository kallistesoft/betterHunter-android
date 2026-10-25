plugins {
    alias(libs.plugins.betterhunter.android.library)
    alias(libs.plugins.betterhunter.android.compose)
}

android {
    namespace = "ca.kallistesoft.hunt.core.testing"
}

// Shared test doubles, rules and screenshot helpers (testImplementation in the modules that use them).
dependencies {
    api(project(":core:ads"))
    api(project(":core:billing"))
    api(project(":core:data"))
    api(project(":core:designsystem"))
    api(project(":core:flags"))
    api(libs.androidx.compose.ui.test.junit4)
    api(libs.junit)
    api(libs.kotlinx.coroutines.test)
    api(libs.robolectric)
    api(libs.roborazzi)
    api(libs.roborazzi.compose)
    api(libs.turbine)
}
