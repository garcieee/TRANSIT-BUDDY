plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.transitbuddysignup"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.transitbuddysignup"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Enable MultiDex - required for MongoDB driver
        multiDexEnabled = true
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
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    
    // Additional R8/D8 options for record desugaring
    buildFeatures {
        buildConfig = true
    }
    
    packagingOptions {
        resources {
            excludes += listOf(
                "META-INF/INDEX.LIST",
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.md",
                "META-INF/license.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.md",
                "META-INF/notice.txt",
                "META-INF/ASL2.0",
                "META-INF/native-image/org.mongodb/bson/native-image.properties"
            )
            pickFirsts += listOf(
                "META-INF/native-image/org.mongodb/bson/native-image.properties"
            )
        }
    }
}

dependencies {
    // Android standard libraries
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    
    // MongoDB dependencies - simple configuration without record codec
    implementation("org.mongodb:mongodb-driver-sync:4.4.2") {
        // Exclude problematic modules
        exclude(group = "org.mongodb", module = "bson-record-codec")
    }
    
    // Multidex support (required for large number of methods)
    implementation("androidx.multidex:multidex:2.0.1")
    
    // Core library desugaring (for Java 8+ features)
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.3")
    
    // Testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}