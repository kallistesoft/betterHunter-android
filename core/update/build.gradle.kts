plugins {
    alias(libs.plugins.betterhunter.android.library)
    alias(libs.plugins.betterhunter.android.compose)
    alias(libs.plugins.betterhunter.android.screenshot)
    alias(libs.plugins.betterhunter.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "ca.kallistesoft.hunt.core.update"
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.play.app.update.ktx)
}
