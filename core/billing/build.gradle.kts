plugins {
    alias(libs.plugins.betterhunter.android.library)
    alias(libs.plugins.betterhunter.hilt)
}

android {
    namespace = "ca.kallistesoft.hunt.core.billing"
}

// Remove ads (P15): Google Play Billing, kept behind this module's interfaces.
dependencies {
    api(project(":core:ads"))
    implementation(project(":core:data"))
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.play.billing)

    testImplementation(project(":core:testing"))
    testImplementation(libs.mockk)
}
