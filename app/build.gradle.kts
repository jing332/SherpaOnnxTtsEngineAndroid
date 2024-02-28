import java.util.Date
import java.util.TimeZone
import java.text.SimpleDateFormat

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlinx-serialization")

}

val buildTime: Long
    get() {
        val t = Date().time / 1000
        return t
    }

val releaseTime: String
    get() {
        val sdf = SimpleDateFormat("yy.MMddHH")
        sdf.timeZone = TimeZone.getTimeZone("GMT+8")
        return sdf.format(Date())
    }

val version = "1.$releaseTime"

val gitCommits: Int
    get() {
        val process = ProcessBuilder("git", "rev-list", "HEAD", "--count").start()
        return process.inputStream.reader().use { it.readText() }.trim().toInt()
    }
android {
    namespace = "com.k2fsa.sherpa.onnx.tts.engine"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.k2fsa.sherpa.onnx.tts.engine"
        minSdk = 21
        targetSdk = 34
        versionCode = gitCommits
        versionName = version

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "_debug"
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true

        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")


    implementation("com.charleskorn.kaml:kaml:0.57.0")
    implementation("com.github.FunnySaltyFish.ComposeDataSaver:data-saver:v1.1.5")
    implementation("org.meeuw.i18n:i18n-iso-639-3:3.0")

    val composeBom = platform("androidx.compose:compose-bom:2024.02.01")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.appcompat:appcompat:1.6.1")

//    implementation("androidx.compose.material3:material-icons-core")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(composeBom)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}