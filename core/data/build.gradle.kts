plugins {
    alias(libs.plugins.betterhunter.android.library)
    alias(libs.plugins.betterhunter.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "ca.kallistesoft.hunt.core.data"
    // Robolectric tests read the bundled assets.
    testOptions.unitTests.isIncludeAndroidResources = true
}

dependencies {
    api(project(":core:model"))
    implementation(libs.androidx.datastore)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(project(":core:testing"))
}
