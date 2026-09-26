plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.goreecloud.keyboard"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.goreecloud.keyboard"
        minSdk = 26
        targetSdk = 35
        versionCode = 18
        versionName = "0.1.17-dev"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            // CI debug signing is not production/system signing authority. Give this physical-test
            // package a version-scoped application ID so it installs alongside a preinstalled
            // com.goreecloud.keyboard build instead of Android treating it as an incompatible
            // signature update.
            applicationIdSuffix = ".dev.v18"
        }

        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.customview:customview:1.2.0")
    testImplementation(kotlin("test"))
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test:runner:1.7.0")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test:core-ktx:1.7.0")
}
