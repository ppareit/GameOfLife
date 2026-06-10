plugins {
    id("com.android.application")
}

android {
    namespace = "be.ppareit.gameoflife"
    compileSdk = 35

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "be.ppareit.gameoflife"
        minSdk = 23
        targetSdk = 35
        versionCode = 20002
        versionName = "2.0.2"
    }
}

dependencies {
    implementation("androidx.drawerlayout:drawerlayout:1.1.1")
}
