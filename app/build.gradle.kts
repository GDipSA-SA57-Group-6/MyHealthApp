import java.util.Properties

plugins {
    id("com.android.application")
}

android {
    namespace = "iss.AD.myhealthapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "iss.AD.myhealthapp"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // 从local.properties中读取API密钥
        val localProperties = Properties()
        file("../local.properties").inputStream().use {
            localProperties.load(it)
        }
        val mapsApiKey = localProperties.getProperty("MAPS_API_KEY", "")
        // 使用占位符设置API密钥
        resValue("string", "maps_api_key", mapsApiKey)
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
}

dependencies {
    implementation ("com.squareup.okhttp3:okhttp:4.10.0")

    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")


    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0");

    implementation("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.12.0")

    implementation ("androidx.preference:preference:1.1.1")

    implementation ("androidx.cardview:cardview:1.0.0")

    // Google Map
    implementation("com.google.android.material:material:1.11.0")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.code.gson:gson:2.8.8")

    // Ok Http
    implementation("com.squareup.okhttp3:okhttp:4.4.1")

    // Google Gson
    implementation("com.google.code.gson:gson:2.8.6")

    // Glide
    implementation ("com.github.bumptech.glide:glide:4.12.0")

    // circule menu
    implementation("com.github.Hitomis:CircleMenu:v1.1.0")

    // Tencent Location
    implementation ("com.tencent.map.geolocation:TencentLocationSdk-openplatform:7.3.0")

    // 动态权限请求
    implementation ("com.tbruyelle.rxpermissions2:rxpermissions:0.10.2")
    implementation ("io.reactivex.rxjava2:rxandroid:2.0.2")
    implementation ("io.reactivex.rxjava2:rxjava:2.0.0")

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.navigation:navigation-fragment:2.5.3")
    implementation("androidx.navigation:navigation-ui:2.5.3")

}