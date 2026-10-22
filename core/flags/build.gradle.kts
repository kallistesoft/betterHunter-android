plugins {
    alias(libs.plugins.betterhunter.android.library)
    alias(libs.plugins.betterhunter.hilt)
}

android {
    namespace = "ca.kallistesoft.hunt.core.flags"
}

dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.config)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)
}
