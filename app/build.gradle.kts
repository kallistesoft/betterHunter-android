import java.util.Properties

plugins {
    alias(libs.plugins.betterhunter.android.application)
    alias(libs.plugins.betterhunter.android.compose)
    alias(libs.plugins.betterhunter.hilt)
    alias(libs.plugins.kotlin.serialization)
}

// Firebase (feature flags) is set up only when the project's config file is present, so the app also builds without
// it (pull requests, forks): flags then keep their defaults. A file under src/debug or src/release wins for that build.
if (listOf("", "src/debug/", "src/release/").any { file("${it}google-services.json").exists() }) {
    apply(plugin = libs.plugins.google.services.get().pluginId)
}

// Release signing comes from keystore.properties (local, never committed) or environment variables (CI).
val keystoreProperties = Properties().apply {
    rootProject.file("keystore.properties").takeIf { it.exists() }?.inputStream()?.use(::load)
}

fun signingValue(key: String, env: String): String? = keystoreProperties.getProperty(key) ?: System.getenv(env)

android {
    namespace = "ca.kallistesoft.hunt"

    defaultConfig {
        applicationId = "ca.kallistesoft.hunt"
        // Every Play upload needs a higher versionCode; CI can pass -PversionCode=<n>.
        versionCode = (findProperty("versionCode") as String?)?.toInt() ?: 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Force update: the oldest versionCode allowed to run is read from this file (see README).
        buildConfigField("String", "UPDATE_CONFIG_URL", "\"https://hunt.kallistesoft.ca/android/update.json\"")
        // Play listing opened when in-app updates aren't available (also used by the debug build).
        buildConfigField("String", "STORE_PACKAGE", "\"ca.kallistesoft.hunt\"")
    }

    signingConfigs {
        val storeFile = signingValue("storeFile", "BH_KEYSTORE_FILE")
        if (storeFile != null) {
            create("release") {
                this.storeFile = rootProject.file(storeFile)
                storePassword = signingValue("storePassword", "BH_KEYSTORE_PASSWORD")
                keyAlias = signingValue("keyAlias", "BH_KEY_ALIAS")
                keyPassword = signingValue("keyPassword", "BH_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        debug {
            // Installs next to the Play version, with its own name and update rules.
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            buildConfigField("String", "UPDATE_CONFIG_URL", "\"https://hunt.kallistesoft.ca/android/update-debug.json\"")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // Unsigned when no keystore is configured (e.g. pull requests); Play App Signing re-signs uploads.
            signingConfig = signingConfigs.findByName("release")
        }
    }

    buildFeatures {
        buildConfig = true
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:flags"))
    implementation(project(":core:ui"))
    implementation(project(":core:update"))
    implementation(project(":feature:about"))
    implementation(project(":feature:exams"))
    implementation(project(":feature:home"))
    implementation(project(":feature:quiz"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.profileinstaller)
    implementation(libs.kotlinx.serialization.json)

    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
