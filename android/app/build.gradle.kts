plugins {
    id("com.android.application")
    id("kotlin-android")
    // The Flutter Gradle Plugin must be applied after the Android and Kotlin Gradle plugins.
    id("dev.flutter.flutter-gradle-plugin")
}

import java.io.FileInputStream
import java.util.Properties

android {
    namespace = "com.example.fastlane"
    compileSdk = flutter.compileSdkVersion
    ndkVersion = flutter.ndkVersion

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }

    // Load signing configuration from key.properties
    val keyPropertiesFile = rootProject.file("key.properties")
    val keyProperties = Properties()
    val hasKeyProperties = keyPropertiesFile.exists()
    
    if (hasKeyProperties) {
        keyProperties.load(FileInputStream(keyPropertiesFile))
        println("Loaded signing configuration from: ${keyPropertiesFile.absolutePath}")
    } else {
        println("WARNING: key.properties file not found at ${keyPropertiesFile.absolutePath}")
        println("Release builds will use debug signing (not suitable for production)")
    }

    signingConfigs {
        if (hasKeyProperties) {
            create("release") {
                val storeFileValue = keyProperties["storeFile"] as String
                // Resolve keystore path relative to android/app/ directory
                val keystoreFile = file(storeFileValue)
                
                if (!keystoreFile.exists()) {
                    throw GradleException("Keystore file not found: ${keystoreFile.absolutePath}. Please ensure the keystore file exists at the specified location.")
                }
                
                storeFile = keystoreFile
                storePassword = keyProperties["storePassword"]?.toString()
                keyAlias = keyProperties["keyAlias"]?.toString()
                keyPassword = keyProperties["keyPassword"]?.toString()
                
                println("Using keystore: ${keystoreFile.absolutePath}")
                println("Key alias: ${keyProperties["keyAlias"]}")
            }
        }
    }

    defaultConfig {
        // TODO: Specify your own unique Application ID (https://developer.android.com/studio/build/application-id.html).
        applicationId = "com.example.fastlane"
        // You can update the following values to match your application needs.
        // For more information, see: https://flutter.dev/to/review-gradle-config.
        minSdk = flutter.minSdkVersion
        targetSdk = flutter.targetSdkVersion
        versionCode = flutter.versionCode
        versionName = flutter.versionName
        
        // Enable resource optimization
        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        release {
            // Only use release signing config if it exists
            if (hasKeyProperties && signingConfigs.findByName("release") != null) {
                signingConfig = signingConfigs.getByName("release")
                println("Release build will be signed with release keystore")
            } else {
                println("WARNING: Release build will use debug signing (not suitable for production)")
            }
            // Enable R8 code shrinking and obfuscation
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

flutter {
    source = "../.."
}
