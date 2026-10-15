plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

// На GitHub номер сборки растёт сам, поэтому новый APK всегда ставится поверх старого
val buildNumber = (System.getenv("GITHUB_RUN_NUMBER") ?: "1").toInt()

android {
    namespace = "com.zubki.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.zubki.app"
        minSdk = 26
        targetSdk = 35
        versionCode = buildNumber
        versionName = "0.1"
    }

    // Ключ лежит в репозитории специально: приложение личное,
    // а постоянный ключ нужен только чтобы обновления ставились без удаления
    signingConfigs {
        create("zubki") {
            storeFile = file("zubki.keystore")
            storePassword = "zubki123"
            keyAlias = "zubki"
            keyPassword = "zubki123"
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("zubki")
            isMinifyEnabled = false
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

    lint {
        checkReleaseBuilds = false
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.core:core-ktx:1.15.0")

    testImplementation("junit:junit:4.13.2")
}
