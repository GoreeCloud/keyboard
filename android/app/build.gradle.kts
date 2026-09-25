plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val developmentSigningSource =
    rootProject.file("android/dev-signing/goreecloud-keyboard-dev.jks.b64")
val developmentSigningFile =
    layout.buildDirectory.file("development-signing/goreecloud-keyboard-dev.jks").get().asFile

if (developmentSigningSource.isFile) {
    developmentSigningFile.parentFile.mkdirs()
    developmentSigningFile.writeBytes(
        java.util.Base64.getDecoder().decode(
            developmentSigningSource.readText(Charsets.UTF_8).trim(),
        ),
    )
}

android {
    namespace = "com.goreecloud.keyboard"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.goreecloud.keyboard"
        minSdk = 26
        targetSdk = 35
        versionCode = 3
        versionName = "0.1.2-dev"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("development") {
            check(developmentSigningFile.isFile) {
                "Missing repository Development signing material at $developmentSigningSource"
            }
            storeFile = developmentSigningFile
            storePassword = "goreecloud-dev-only"
            keyAlias = "goreecloud-keyboard-dev"
            keyPassword = "goreecloud-dev-only"
        }
    }

    buildTypes {
        debug {
            // Development artifacts must not collide with the system/preinstalled production-id
            // package. This also lets the stable public Development test key provide repeatable
            // updateability across CI builds without claiming production signing authority.
            applicationIdSuffix = ".dev"
            signingConfig = signingConfigs.getByName("development")
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
