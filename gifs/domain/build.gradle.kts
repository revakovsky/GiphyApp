plugins {
    id("kotlin")
    alias(libs.plugins.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {

    // Modules
    api(projects.core.domain)

    // Coroutines
    implementation(libs.coroutines.android)

}
