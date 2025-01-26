plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.revakovskyi.giphy.core.network"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            buildConfigField(
                type = "String",
                name = "BASE_URL",
                value = "\"https://api.giphy.com/v1/gifs/\""
            )
            buildConfigField(
                type = "String",
                name = "API_KEY",
                value = "\"KAOSFbLWfyLorjDogqvJbUFxa74btjK5\""
            )
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            buildConfigField(
                type = "String",
                name = "BASE_URL",
                value = "\"https://api.giphy.com/v1/gifs/\""
            )
            buildConfigField(
                type = "String",
                name = "API_KEY",
                value = "\"KAOSFbLWfyLorjDogqvJbUFxa74btjK5\""
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = libs.versions.jvmTargetVersion.get()
    }
    buildFeatures {
        buildConfig = true
    }
}

dependencies {

    // Core
    implementation(libs.bundles.android.library.core)

    // Network
    implementation(libs.bundles.okHttp)
    implementation(libs.bundles.retrofit2)

    // Koin
    implementation(libs.bundles.koin)

}