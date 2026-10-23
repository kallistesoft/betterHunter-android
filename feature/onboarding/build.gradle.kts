plugins {
    alias(libs.plugins.betterhunter.android.feature)
}

android {
    namespace = "ca.kallistesoft.hunt.feature.onboarding"
}

dependencies {
    implementation(libs.androidx.activity.compose)
}
