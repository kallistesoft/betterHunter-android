plugins {
    alias(libs.plugins.betterhunter.android.feature)
}

android {
    namespace = "ca.kallistesoft.hunt.feature.exams"
}

dependencies {
    implementation(project(":core:ads"))
}
