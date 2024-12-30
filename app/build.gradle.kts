plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.dagger.hilt)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.realm.kotlin)
    id("kotlin-parcelize")
}

android {
    namespace = "com.colors.testble"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.colors.testble"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    /** Kotlin Coroutine **/
    implementation(libs.kotlinx.coroutines.android)
    /** Viewmodel **/
    implementation(libs.lifecycle.viewmodel.ktx)
    /** Dagger Hilt **/
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    /**Navigation Compose**/
    implementation(libs.navigation.compose)
    /**Hilt Navigation Compose**/
    implementation(libs.hilt.navigation.compose)
    /**
     * Work manager
     * */
    implementation(libs.work.runtime.ktx)
    /**
     * Hilt work
     * */
    implementation(libs.hilt.work)
    ksp(libs.hilt.compiler)
    /**
     * Realm database
     * */
    implementation(libs.realm.library.base)
    implementation(libs.realm.library.sync)

    /**Kotlinx serialization json**/
    implementation(libs.kotlinx.serialization.json)
}