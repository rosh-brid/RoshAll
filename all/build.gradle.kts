plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
    namespace = "rosh.all"
    compileSdk = 36

    defaultConfig {
        applicationId = "rosh.all"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    
    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }
    
    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget("17"))
        }
    }
}

dependencies {
    implementation(project(":lib"))
    implementation(project(":file_explorer"))
    implementation(project(":code"))
    implementation(project(":terminal"))
    implementation(project(":video"))
    implementation(project(":gambar"))
    implementation(project(":browser"))

    implementation("androidx.constraintlayout:constraintlayout:2.2.2")
    implementation("androidx.appcompat:appcompat:1.8.0")
    implementation("com.google.android.material:material:1.14.0")
    implementation("androidx.activity:activity:1.14.0-alpha03")
    implementation("androidx.documentfile:documentfile:1.1.0")
}
