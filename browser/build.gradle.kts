plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    namespace = "rosh.browser"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
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
    implementation("androidx.annotation:annotation:1.7.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.2")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.activity:activity:1.10.1")
    implementation(project(":lib"))
}