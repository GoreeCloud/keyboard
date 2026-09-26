plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val goreeCloudCandidateVersionName = "0.1.19-dev"
val goreeCloudCandidateDisplayVersion = goreeCloudCandidateVersionName.removeSuffix("-dev")

android {
    namespace = "com.goreecloud.keyboard"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.goreecloud.keyboard"
        minSdk = 26
        targetSdk = 35
        versionCode = 20
        versionName = goreeCloudCandidateVersionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            // CI debug signing is not production/system signing authority. Give this physical-test
            // package a version-scoped application ID so it installs alongside a preinstalled
            // com.goreecloud.keyboard build instead of Android treating it as an incompatible
            // signature update.
            applicationIdSuffix = ".dev.v20"
            // Derive the visible side-by-side Development identity from the same source as
            // versionName so Android's installer/IME picker cannot show a stale candidate number.
            resValue(
                "string",
                "app_name",
                "GoreeCloud Keyboard Dev $goreeCloudCandidateDisplayVersion",
            )
            resValue(
                "string",
                "ime_name",
                "GoreeCloud Keyboard Dev $goreeCloudCandidateDisplayVersion",
            )
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
