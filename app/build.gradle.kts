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

// AdMob IDs (FOLLOW-UP.md, section 2). Public identifiers, not secrets. Only the release build serves real ads;
// debug and qa (emulators, smoke tests) always use Google's sample IDs, which serve test ads only.
val admobRelease = mapOf(
    "appId" to "ca-app-pub-7557144664039757~4450187296", // AdMob app "BetterHunter"
    "interstitial" to "ca-app-pub-7557144664039757/7902432864", // Ad unit "Quiz finished" (cap: 1 per 3 minutes)
    "rewarded" to "ca-app-pub-7557144664039757/8107883780", // Ad unit "Mock retake" (rewarded, P14; no frequency cap)
)
val admobTest = mapOf(
    "appId" to "ca-app-pub-3940256099942544~3347511713",
    "interstitial" to "ca-app-pub-3940256099942544/1033173712",
    "rewarded" to "ca-app-pub-3940256099942544/5224354917",
)

/** The AdMob App ID goes in the manifest (`${admobAppId}`), the ad units in BuildConfig. */
fun com.android.build.api.dsl.ApplicationBuildType.admob(ids: Map<String, String>) {
    manifestPlaceholders["admobAppId"] = ids.getValue("appId")
    buildConfigField("String", "ADMOB_INTERSTITIAL_ID", "\"${ids.getValue("interstitial")}\"")
    buildConfigField("String", "ADMOB_REWARDED_ID", "\"${ids.getValue("rewarded")}\"")
}

android {
    namespace = "ca.kallistesoft.hunt"

    defaultConfig {
        applicationId = "ca.kallistesoft.hunt"
        // Every Play upload needs a higher versionCode; CI can pass -PversionCode=<n>.
        versionCode = (findProperty("versionCode") as String?)?.toInt() ?: 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // Flow tests: each test in its own process with the app's data cleared, so every test starts at first launch.
        testInstrumentationRunnerArguments["clearPackageData"] = "true"

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

    // Code that differs by build type lives in src/debug and src/release (never `if (BuildConfig.DEBUG)`).
    buildTypes {
        debug {
            // Installs next to the Play version, with its own name and update rules.
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            admob(admobTest)
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // Unsigned when no keystore is configured (e.g. pull requests); Play App Signing re-signs uploads.
            signingConfig = signingConfigs.findByName("release")
            admob(admobRelease)
        }
        // The release build (R8, shrinking, release code) signed with the debug key, so the release smoke test
        // (:smoketest) can install it on an emulator. Never uploaded.
        create("qa") {
            initWith(getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += "release"
            // Installed on emulators by the smoke test: never request real ads from it.
            admob(admobTest)
        }
    }

    // qa is the release build: it uses the release code (src/release).
    sourceSets.named("qa") {
        kotlin.directories += "src/release/kotlin"
    }

    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
        animationsDisabled = true
        // Robolectric tests (src/test) run the real app with its resources and content assets.
        unitTests.isIncludeAndroidResources = true
    }

    buildFeatures {
        buildConfig = true
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    implementation(project(":core:ads"))
    implementation(project(":core:billing"))
    implementation(project(":core:data"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:flags"))
    implementation(project(":core:ui"))
    implementation(project(":core:update"))
    implementation(project(":feature:about"))
    implementation(project(":feature:exams"))
    implementation(project(":feature:home"))
    implementation(project(":feature:onboarding"))
    implementation(project(":feature:quiz"))
    implementation(project(":feature:settings"))

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
    implementation(libs.androidx.work.runtime)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(project(":core:testing"))
    testImplementation(libs.androidx.datastore.preferences)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.androidx.work.testing)

    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestUtil(libs.androidx.test.orchestrator)
    androidTestUtil(libs.androidx.test.services)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
