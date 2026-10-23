// Release smoke test: drives the app's qa build (the release build: R8, shrinking, release code; debug-signed so it
// installs) from the outside with UiAutomator, to catch what only shows up after R8 (stripped serialization, missing
// classes) before a Play upload. Run: ./gradlew :smoketest:connectedQaAndroidTest
plugins {
    alias(libs.plugins.android.test)
}

android {
    namespace = "ca.kallistesoft.hunt.smoketest"
    compileSdk = 37

    defaultConfig {
        minSdk = 26
        targetSdk = 37
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        create("qa") {
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += "release"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    targetProjectPath = ":app"
    // The app under test isn't debuggable: the test runs in its own process and only drives the UI.
    experimentalProperties["android.experimental.self-instrumenting"] = true
}

// Only the qa variant has a release-like app to test.
androidComponents {
    beforeVariants { it.enable = it.buildType == "qa" }
}

dependencies {
    implementation(libs.androidx.test.ext.junit)
    implementation(libs.androidx.test.runner)
    implementation(libs.androidx.test.uiautomator)
}
