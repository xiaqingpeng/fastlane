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
                
                // Try multiple possible locations for keystore file
                val possiblePaths = listOf(
                    file(storeFileValue), // android/app/release.keystore (relative to android/app/)
                    rootProject.file("app/$storeFileValue"), // android/app/release.keystore (from android/)
                    rootProject.file(storeFileValue), // android/release.keystore (from android/)
                    rootProject.file("../$storeFileValue") // release.keystore (from project root)
                )
                
                val keystoreFile = possiblePaths.firstOrNull { it.exists() }
                
                if (keystoreFile == null) {
                    val searchedPaths = possiblePaths.joinToString("\n  - ") { it.absolutePath }
                    throw GradleException(
                        "Keystore file not found: $storeFileValue\n" +
                        "Searched in:\n  - $searchedPaths\n" +
                        "Please ensure the keystore file exists in one of these locations."
                    )
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
            
            // Additional optimizations for smaller APK size
            isDebuggable = false
            isJniDebuggable = false
            isRenderscriptDebuggable = false
            renderscriptOptimLevel = 3
        }
    }
    
    // Packaging options to reduce APK size
    packaging {
        resources {
            excludes += listOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/license.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/notice.txt",
                "META-INF/ASL2.0",
                "META-INF/*.kotlin_module",
                "META-INF/AL2.0",
                "META-INF/LGPL2.1"
            )
        }
    }
}

flutter {
    source = "../.."
}
