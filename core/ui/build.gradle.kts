plugins {
    alias(libs.plugins.betterhunter.android.library)
    alias(libs.plugins.betterhunter.android.compose)
}

android {
    namespace = "ca.kallistesoft.hunt.core.ui"
}

dependencies {
    api(project(":core:model"))
    api(project(":core:designsystem"))
    implementation(libs.androidx.browser)
    implementation(libs.androidx.core.ktx)
    implementation(libs.coil.compose)
}
