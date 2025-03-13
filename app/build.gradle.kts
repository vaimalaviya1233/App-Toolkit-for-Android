plugins {
    alias(notation = libs.plugins.android.application)
    alias(notation = libs.plugins.kotlin.android)
    alias(notation = libs.plugins.compose.compiler)
    alias(notation = libs.plugins.about.libraries)
}

android {
    namespace = "com.d4rk.android.libs.apptoolkit"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.d4rk.android.libs.apptoolkit"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isDebuggable = false
        }

        debug {
            isDebuggable = true
        }
    }

    buildTypes.forEach { buildType ->
        with(buildType) {
            multiDexEnabled = true
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(getDefaultProguardFile(name = "proguard-android-optimize.txt") , "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_23
        targetCompatibility = JavaVersion.VERSION_23
    }

    kotlinOptions {
        jvmTarget = "23"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(dependencyNotation = project(path = ":apptoolkit"))
}
