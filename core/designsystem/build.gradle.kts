plugins {
    alias(libs.plugins.betterhunter.android.library)
    alias(libs.plugins.betterhunter.android.compose)
    alias(libs.plugins.betterhunter.android.screenshot)
}

android {
    namespace = "ca.kallistesoft.hunt.core.designsystem"
}

dependencies {
    api(libs.androidx.compose.material.icons.extended)
}
