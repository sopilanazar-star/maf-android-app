plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "ua.lviv.maf"
    compileSdk = 34

    defaultConfig {
        applicationId = "ua.lviv.maf"
        minSdk = 24
        targetSdk = 34

        // ВАЖЛИВО: Для наступного оновлення змініть на 22 та "2.2"
        versionCode = 21        
        versionName = "2.1"     

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // ДОДАЄМО НАЛАШТУВАННЯ ПІДПИСУ (Signing Config)
    // Це вирішить проблему "Додаток не встановлено" при оновленні
    signingConfigs {
        create("release") {
            // Ці дані ми заповнимо після створення вами .jks ключа
            storeFile = file("maf-release.jks") 
            storePassword = System.getenv("SIGNING_STORE_PASSWORD")
            keyAlias = System.getenv("SIGNING_KEY_ALIAS")
            keyPassword = System.getenv("SIGNING_KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            // Підключаємо наш підпис до release збірки
            signingConfig = signingConfigs.getByName("release") 
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isDebuggable = true
            // Щоб debug версія могла оновлювати release, вони мають мати однаковий підпис
            signingConfig = signingConfigs.getByName("release")
        }
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    
    // ДОДАЄМО: Firebase Remote Config (для автооновлень)
    implementation("com.google.firebase:firebase-config-ktx")
    
    // ДОДАЄМО: Google AdMob (для реклами та пасивного доходу)
    implementation("com.google.android.gms:play-services-ads:23.0.0")

    // Існуючі залежності
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.webkit:webkit:1.11.0")
    implementation("androidx.activity:activity-ktx:1.9.3")
    implementation("androidx.browser:browser:1.8.0")
    implementation("androidx.core:core-splashscreen:1.0.1")
}
