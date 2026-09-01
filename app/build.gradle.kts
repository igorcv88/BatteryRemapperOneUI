plugins {
    id("com.android.application")
}

android {
    namespace = "com.igcv.batteryremapper"
    compileSdk = 34

   defaultConfig {
        applicationId = "com.igcv.batteryremapper"
        minSdk = 29
        targetSdk = 34
        
        // Dynamic versioning using run_number from CI
        val runNumber = project.findProperty("versionCode")?.toString()?.toInt() ?: 1
        
        versionCode = runNumber
        versionName = "1.0.$runNumber"
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
    packaging {
        resources { merges += "META-INF/xposed/*" }
    }
}

dependencies {
    compileOnly("io.github.libxposed:api:102.0.0")
    implementation("io.github.libxposed:service:102.0.0")
    testImplementation("junit:junit:4.13.2")
}
