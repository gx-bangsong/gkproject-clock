plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.ksp) // 确保你的 libs.versions.toml 中有 kotlin-ksp 的定义

}

android {
    namespace = "com.gkprojct.clock"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.gkprojct.clock"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }

    sourceSets {
        getByName("main") {
            jniLibs.srcDirs("libs")  // 设置 jniLibs 目录
            resources.srcDirs("libs")  // 设置资源目录
        }
    }

    testOptions {
        unitTests.returnDefaultValues = true
        unitTests.all {
            it.jvmArgs(
                "--add-opens=java.base/java.lang=ALL-UNNAMED",
                "--add-opens=java.base/java.util=ALL-UNNAMED",

            "--add-opens=java.base/java.time=ALL-UNNAMED" // <-- Add this line
            )
        }
    }
}

//repositories {
//    google()
//    mavenCentral()
//    flatDir {
//        dirs("libs")  // 配置本地 libs 目录
//    }
//}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation ("com.google.accompanist:accompanist-permissions:0.34.0") // 替换 <latest_version> 为最新版本号
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.runtime) // <-- 添加这一行

    // 引用本地的 AAR 文件
    implementation(files("libs/material-icons-extended-android-1.7.0.aar"))

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.compose.ui:ui-tooling:1.6.8")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")
    implementation("androidx.compose.runtime:runtime-livedata:1.6.8")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("androidx.test:core:1.6.1")
    implementation("androidx.test:runner:1.6.2")
    implementation("androidx.test:rules:1.6.1")// Use the latest stable version if needed
    // 使用已定义的 junit 版本
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin) // 追加
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    // 如果使用 libs.versions.toml
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler) // 或 kapt(libs.androidx.room.compiler)
}
