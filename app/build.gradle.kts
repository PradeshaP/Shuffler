plugins {
    alias(libs.plugins.android.application) // Assuming you are using version catalog
}

android {
    namespace = "com.example.shuffler"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.shuffler"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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

    buildFeatures {
        viewBinding = true // Enable View Binding
    }
}

dependencies {
    // For UI components
    implementation(libs.appcompat) // AndroidX AppCompat
    implementation(libs.material) // Material Components
    implementation(libs.constraintlayout) // Constraint Layout
    implementation(libs.navigation.fragment) // Navigation Fragment
    implementation(libs.navigation.ui) // Navigation UI
    implementation(libs.activity) // Activity KTX

    // Networking libraries
    implementation("com.squareup.retrofit2:retrofit:2.9.0") // Retrofit for networking
    implementation("com.squareup.retrofit2:converter-gson:2.9.0") // Gson converter for Retrofit
    implementation("com.squareup.okhttp3:okhttp:4.9.3") // OkHttp for HTTP client (optional)
    implementation("com.google.code.gson:gson:2.10.1") // Gson for JSON parsing (optional, included in converter)

    // Testing libraries
    testImplementation(libs.junit) // JUnit for unit testing
    androidTestImplementation(libs.ext.junit) // JUnit Extensions for Android
    androidTestImplementation(libs.espresso.core) // Espresso for UI testing
}