plugins {
    alias(libs.plugins.betterhunter.android.library)
    alias(libs.plugins.betterhunter.hilt)
}

android {
    namespace = "ca.kallistesoft.hunt.core.ads"
}

// AdMob (P8): the Google Mobile Ads SDK and Google's consent SDK (UMP), kept behind this module's interfaces.
dependencies {
    implementation(project(":core:flags"))
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.play.services.ads)
    implementation(libs.ump)

    testImplementation(project(":core:testing"))
    testImplementation(libs.mockk)
}
