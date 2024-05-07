plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
}

android {
    namespace = "m20.bgm.downloader.v4"
    compileSdk = 34

    defaultConfig {
        applicationId = "m20.bgm.downloader.v4"
        minSdk = 24
        targetSdk = 34
        versionCode = 4031
        versionName = "4.0.3.1"

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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.navigation:navigation-fragment:2.7.6")
    implementation("androidx.navigation:navigation-ui:2.7.6")
    implementation("androidx.preference:preference:1.2.0")
    implementation("androidx.core:core-ktx:+")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    implementation("com.squareup.okhttp3:okhttp:3.10.0")
    implementation("com.google.code.gson:gson:2.10")
    implementation("io.github.justson:agentweb-core:v5.1.1-androidx")
    implementation("io.github.justson:agentweb-filechooser:v5.1.1-androidx") // (可选)
    implementation("com.github.Justson:Downloader:v5.0.4-androidx") // (可选)
    implementation("org.jsoup:jsoup:1.14.3")
    implementation ("com.google.android.flexbox:flexbox:3.0.0")
    implementation ("androidx.palette:palette:1.0.0")

    implementation("com.umeng.umsdk:common:9.4.7") // 必选
    implementation("com.umeng.umsdk:asms:1.4.0") // 必选
    implementation("com.umeng.umsdk:abtest:1.0.0") //使用U-App中ABTest能力，可选

    implementation ("com.makeramen:roundedimageview:2.3.0")
    implementation("org.apache.commons:commons-text:1.9")
    implementation("com.github.DylanCaiCoding.Longan:longan:1.1.1")

}