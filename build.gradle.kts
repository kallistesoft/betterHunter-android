// Plugins are declared here once so every module loads them from the same class loader.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kover) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.roborazzi) apply false
    alias(libs.plugins.betterhunter.kover)
}

// Unit test coverage of the app's modules, merged (see KoverConventionPlugin): ./gradlew koverVerifyUnit.
// Not :core:testing (test doubles) nor :smoketest (a device test of the release build).
dependencies {
    listOf(
        ":app",
        ":core:data",
        ":core:designsystem",
        ":core:flags",
        ":core:model",
        ":core:ui",
        ":core:update",
        ":feature:about",
        ":feature:exams",
        ":feature:home",
        ":feature:onboarding",
        ":feature:quiz",
        ":feature:settings",
    ).forEach { kover(project(it)) }
}
