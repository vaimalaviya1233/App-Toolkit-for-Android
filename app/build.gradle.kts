import java.util.Properties

plugins {
    alias(notation = libs.plugins.android.application)
    alias(notation = libs.plugins.kotlin.android)
    alias(notation = libs.plugins.compose.compiler)
    alias(notation = libs.plugins.about.libraries)
    alias(notation = libs.plugins.googlePlayServices)
    alias(notation = libs.plugins.googleFirebase)
    alias(notation = libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.d4rk.android.apps.apptoolkit"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.d4rk.android.apps.apptoolkit"
        minSdk = 23
        targetSdk = 36
        versionCode = 26
        versionName = "1.0.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        @Suppress("UnstableApiUsage") androidResources.localeFilters += listOf(
            "ar-rEG" , "bg-rBG" , "bn-rBD" , "de-rDE" , "en" , "es-rGQ" , "es-rMX" , "fil-rPH" , "fr-rFR" , "hi-rIN" , "hu-rHU" , "in-rID" , "it-rIT" , "ja-rJP" , "ko-rKR" , "pl-rPL" , "pt-rBR" , "ro-rRO" , "ru-rRU" , "sv-rSE" , "th-rTH" , "tr-rTR" , "uk-rUA" , "ur-rPK" , "vi-rVN" , "zh-rTW"
        )
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release")

        val signingProps = Properties()
        val signingFile = rootProject.file("signing.properties")

        if (signingFile.exists()) {
            signingProps.load(signingFile.inputStream())

            signingConfigs.getByName("release").apply {
                storeFile = file(signingProps["STORE_FILE"].toString())
                storePassword = signingProps["STORE_PASSWORD"].toString()
                keyAlias = signingProps["KEY_ALIAS"].toString()
                keyPassword = signingProps["KEY_PASSWORD"].toString()
            }
        }
        else {
            android.buildTypes.getByName("release").signingConfig = null
        }
    }

    buildTypes {
        release {
            val signingFile = rootProject.file("signing.properties")
            signingConfig = if (signingFile.exists()) {
                signingConfigs.getByName("release")
            } else {
                null
            }
            isDebuggable = false
        }
        debug {
            isDebuggable = true
        }
    }

    buildTypes.forEach { buildType ->
        with(receiver = buildType) {
            multiDexEnabled = true
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(getDefaultProguardFile(name = "proguard-android-optimize.txt") , "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions {
        jvmTarget = "21"
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    packaging {
        resources {
            excludes.add("META-INF/INDEX.LIST")
            excludes.add("META-INF/io.netty.versions.properties")
        }
    }
}

dependencies {
    implementation(dependencyNotation = project(path = ":apptoolkit"))
}