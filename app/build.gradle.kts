import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val releaseSigningVariables = listOf(
    "NATALIA_RELEASE_STORE_FILE",
    "NATALIA_RELEASE_STORE_PASSWORD",
    "NATALIA_RELEASE_KEY_ALIAS",
    "NATALIA_RELEASE_KEY_PASSWORD",
)
val releaseSigningValues = releaseSigningVariables.associateWith { System.getenv(it)?.takeIf(String::isNotBlank) }
val hasReleaseSigning = releaseSigningValues.values.all { it != null }

android {
    namespace = "com.tigstaking.natalia"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.tigstaking.natalia"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    if (hasReleaseSigning) {
        signingConfigs {
            create("release") {
                storeFile = rootProject.file(releaseSigningValues.getValue("NATALIA_RELEASE_STORE_FILE")!!)
                storePassword = releaseSigningValues.getValue("NATALIA_RELEASE_STORE_PASSWORD")
                keyAlias = releaseSigningValues.getValue("NATALIA_RELEASE_KEY_ALIAS")
                keyPassword = releaseSigningValues.getValue("NATALIA_RELEASE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        getByName("release") {
            if (hasReleaseSigning) signingConfig = signingConfigs.getByName("release")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        managedDevices {
            localDevices {
                create("pixel2api35") {
                    device = "Pixel 2"
                    apiLevel = 35
                    systemImageSource = "google"
                }
            }
        }
    }
}

val verifyReleaseSigning by tasks.registering {
    doLast {
        val missing = releaseSigningVariables.filter { releaseSigningValues[it] == null }
        check(missing.isEmpty()) {
            "Release APK signing is not configured. Set the required NATALIA_RELEASE_* environment variables; see docs/LOCAL_DEVELOPMENT.md."
        }
        val storePath = releaseSigningValues.getValue("NATALIA_RELEASE_STORE_FILE")!!
        check(rootProject.file(storePath).isFile) {
            "Release keystore file does not exist at the configured NATALIA_RELEASE_STORE_FILE path."
        }
    }
}

tasks.configureEach {
    if (name == "packageRelease" || name == "bundleRelease") dependsOn(verifyReleaseSigning)
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2025.03.00"))
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.datastore:datastore-preferences:1.1.4")
    implementation("com.google.android.gms:play-services-location:21.4.0")
    implementation("org.maplibre.gl:android-sdk-opengl:13.6.1")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20240303")
    androidTestImplementation(platform("androidx.compose:compose-bom:2025.03.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
}
