plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
    namespace = "rosh.rosh"
    compileSdk = 36

    defaultConfig {
        applicationId = "rosh.rosh"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    
    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }
    
    sourceSets {
        getByName("main") {
            jniLibs.srcDirs("src/main/jniLibs")
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
    implementation("androidx.constraintlayout:constraintlayout:2.2.2")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.activity:activity:1.10.1")
    implementation(project(":lib"))
    implementation(project(":terminal"))
    implementation(project(":berkas"))
    implementation(project(":browser"))
}
