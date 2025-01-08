plugins {
    alias(notation = libs.plugins.android.library)
    alias(notation = libs.plugins.kotlin.android)
    alias(notation = libs.plugins.compose.compiler)
    alias(notation = libs.plugins.about.libraries)
    `maven-publish`
}

android {

    namespace = "com.d4rk.android.libs.apptoolkit"
    compileSdk = 35

    defaultConfig {
        minSdk = 23
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile(name = "proguard-android-optimize.txt") , "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {

    //AndroidX
    implementation(dependencyNotation = libs.androidx.core.ktx)
    implementation(dependencyNotation = libs.androidx.appcompat)
    implementation(dependencyNotation = libs.androidx.core.splashscreen)
    implementation(dependencyNotation = libs.androidx.multidex)
    implementation(dependencyNotation = libs.androidx.work.runtime.ktx)

    // Compose
    implementation(dependencyNotation = platform(libs.androidx.compose.bom))
    implementation(dependencyNotation = libs.androidx.ui)
    implementation(dependencyNotation = libs.androidx.activity.compose)
    implementation(dependencyNotation = libs.androidx.ui.graphics)
    implementation(dependencyNotation = libs.androidx.compose.runtime)
    implementation(dependencyNotation = libs.androidx.runtime.livedata)
    implementation(dependencyNotation = libs.androidx.ui.tooling.preview)
    implementation(dependencyNotation = libs.androidx.material3)
    implementation(dependencyNotation = libs.androidx.material.icons.extended)
    implementation(dependencyNotation = libs.datastore.preferences)
    implementation(dependencyNotation = libs.androidx.datastore.preferences)
    implementation(dependencyNotation = libs.androidx.foundation)
    implementation(dependencyNotation = libs.androidx.navigation.compose)

    // Ktor
    implementation(dependencyNotation = platform(libs.ktor.bom))
    implementation(dependencyNotation = libs.ktor.client.android)
    implementation(dependencyNotation = libs.ktor.client.serialization)
    implementation(dependencyNotation = libs.ktor.client.logging)
    implementation(dependencyNotation = libs.ktor.client.content.negotiation)
    implementation(dependencyNotation = libs.ktor.serialization.kotlinx.json)

    // About
    implementation(dependencyNotation = libs.aboutlibraries)
    implementation(dependencyNotation = libs.core)


    implementation(libs.material)
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "com.github.D4rK7355608"
            artifactId = "AppToolkit"
            version = "0.0.5"

            afterEvaluate {
                from(components["release"])
            }
        }
    }
}