plugins {
    alias(libs.plugins.betterhunter.android.library)
    alias(libs.plugins.betterhunter.android.compose)
    alias(libs.plugins.betterhunter.android.screenshot)
    alias(libs.plugins.betterhunter.hilt)
}

android {
    namespace = "ca.kallistesoft.hunt.core.update"
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:flags"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.play.app.update.ktx)
}
