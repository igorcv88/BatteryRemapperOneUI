plugins {
    id("com.android.application")
}

android {
    namespace = "com.igcv.batteryremapper"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.igcv.batteryremapper"
        minSdk = 29
        targetSdk = 34

        val releaseVersionCode = project.findProperty("releaseVersionCode")
            ?.toString()?.toIntOrNull() ?: 1
        val releaseVersionName = project.findProperty("releaseVersionName")
            ?.toString()?.takeIf { it.isNotBlank() } ?: "1.0.0"

        versionCode = releaseVersionCode
        versionName = releaseVersionName
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources { merges += "META-INF/xposed/*" }
    }
}

dependencies {
    compileOnly("io.github.libxposed:api:102.0.0")
    implementation("io.github.libxposed:service:102.0.0")
    compileOnly("androidx.annotation:annotation:1.9.1")
    testImplementation("junit:junit:4.13.2")
}
