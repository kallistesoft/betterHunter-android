plugins {
    alias(libs.plugins.betterhunter.android.feature)
}

android {
    namespace = "ca.kallistesoft.hunt.feature.quiz"
}

dependencies {
    implementation(project(":core:flags"))
    implementation(libs.androidx.activity.compose)
}
