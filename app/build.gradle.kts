plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val appVersion = "1.0.1"
val appVersionCode = 2

// 发布签名：仅从环境变量读取，密钥永不入库。
// 本地开发未设置这些变量时回退到调试签名，保证 assembleRelease 仍可验证构建。
val releaseKeystore = System.getenv("SMARTSORT_KEYSTORE_FILE").orEmpty()
val releaseStorePassword = System.getenv("SMARTSORT_KEYSTORE_PASSWORD").orEmpty()
val releaseKeyAlias = System.getenv("SMARTSORT_KEY_ALIAS").orEmpty()
val releaseKeyPassword = System.getenv("SMARTSORT_KEY_PASSWORD").orEmpty()
val requireSigned = System.getenv("SMARTSORT_REQUIRE_SIGNED") == "true"

if (requireSigned && releaseKeystore.isBlank()) {
    error("SMARTSORT_REQUIRE_SIGNED=true 但未配置 SMARTSORT_KEYSTORE_FILE，禁止生成未签名发布包")
}
if (releaseKeystore.isNotBlank() &&
    listOf(releaseStorePassword, releaseKeyAlias, releaseKeyPassword).any { it.isBlank() }
) {
    error("发布签名环境变量不完整：需要 KEYSTORE_FILE / KEYSTORE_PASSWORD / KEY_ALIAS / KEY_PASSWORD")
}

android {
    namespace = "com.smartsort.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.smartsort.app"
        minSdk = 26
        targetSdk = 34
        versionCode = appVersionCode
        versionName = appVersion
    }

    signingConfigs {
        create("release") {
            if (releaseKeystore.isNotBlank()) {
                storeFile = file(releaseKeystore)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = if (releaseKeystore.isBlank()) {
                signingConfigs.getByName("debug")
            } else {
                signingConfigs.getByName("release")
            }
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

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1,*.version}"
            excludes += "/DebugProbesKt.bin"
            excludes += "/kotlin-tooling-metadata.json"
        }
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.3")
    implementation("androidx.activity:activity-compose:1.9.0")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")

    debugImplementation("androidx.compose.ui:ui-tooling")

    testImplementation("junit:junit:4.13.2")
}

// AGP 8.5 未提供公开的产物重命名 API，构建结束后将默认文件名统一为 SmartSort-v<versionName>.apk。
val renameReleaseApk = tasks.register("renameReleaseApk") {
    doLast {
        val outDir = layout.buildDirectory.dir("outputs/apk/release").get().asFile
        val defaultApk = outDir.resolve("${project.name}-release.apk")
        val targetApk = outDir.resolve("SmartSort-v${appVersion}.apk")
        if (!defaultApk.exists()) error("未找到 release 产物：${defaultApk}")
        defaultApk.copyTo(targetApk, overwrite = true)
        defaultApk.delete()
    }
}

tasks.configureEach {
    if (name == "assembleRelease") finalizedBy(renameReleaseApk)
}
