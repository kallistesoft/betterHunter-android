plugins {
    alias(libs.plugins.betterhunter.android.library)
}

android {
    namespace = "ca.kallistesoft.hunt.core.testing"
}

// Shared test doubles and rules for ViewModel tests (testImplementation in feature modules).
dependencies {
    api(project(":core:data"))
    api(project(":core:flags"))
    api(libs.junit)
    api(libs.kotlinx.coroutines.test)
    api(libs.turbine)
}
