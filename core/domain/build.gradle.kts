plugins {
    id("kotlin")
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

dependencies {

    implementation(libs.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

}
