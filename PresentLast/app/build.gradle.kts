plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.matibag.presentlast"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.matibag.presentlast"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Correctly enable BuildConfig generation
    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        getByName("debug") {
            // Points to Localhost (Emulator)
            buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:3000/\"")
        }

        // New Staging Variant
        create("staging") {
            // Matches debug config so you don't need signing keys
            initWith(getByName("debug"))

            // Points to Production URL (Cloudflare)
            buildConfigField("String", "API_BASE_URL", "\"https://presentlms.taifunesenkari.workers.dev/\"")

            // Adds a suffix so you can have both Debug and Staging apps installed at once
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-staging"
        }

        getByName("release") {
            buildConfigField("String", "API_BASE_URL", "\"https://presentlms.taifunesenkari.workers.dev/\"")
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.material.v190)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.zxing.android.embedded)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
}