plugins {
    id("kotlin")
    alias(libs.plugins.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

dependencies {

    // Modules
    api(projects.core.domain)

    // Coroutines
    implementation(libs.coroutines.android)

}
