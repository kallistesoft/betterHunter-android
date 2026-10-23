plugins {
    alias(libs.plugins.betterhunter.android.feature)
}

android {
    namespace = "ca.kallistesoft.hunt.feature.settings"
}

dependencies {
    implementation(project(":core:ads"))
}
