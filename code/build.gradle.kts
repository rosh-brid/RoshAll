plugins {
  id("com.android.library")
  id("kotlin-android")
}

android {
  namespace = "com.example.code"
  compileSdk = 34

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
  // Core Android dependencies
  implementation("androidx.annotation:annotation:1.7.0")
}