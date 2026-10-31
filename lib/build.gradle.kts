plugins {
  id("com.android.library")
  id("kotlin-android")
  id("maven-publish")
}

group = "com.github.rosh-brid"
version = "2.0.0"

android {
  namespace = "lib"
  compileSdk = 36

  defaultConfig {
    multiDexEnabled = true
    minSdk = 24
  }
  
  publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
  
  sourceSets {
        getByName("main") {
            jniLibs.srcDir("src/main/jniLibs")
        }
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
    implementation("androidx.constraintlayout:constraintlayout:2.2.2")
    implementation("androidx.appcompat:appcompat:1.8.0")
    implementation("com.google.android.material:material:1.14.0")
    implementation("androidx.activity:activity:1.14.0-alpha03")
    implementation("androidx.documentfile:documentfile:1.0.1")
    implementation("com.github.bumptech.glide:glide:4.16.0")
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])

                groupId = "com.github.rosh-brid" 
                artifactId = "lib"
                version = "2.0.0"
            }
        }
    }
}