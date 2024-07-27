plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("maven-publish")
}

android {
    namespace = "com.zhangke.imageviewer"
    compileSdk = 34

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
        multiDexEnabled = true
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
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.6"
    }
}

dependencies {
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.compose.foundation)
}
afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("maven") {
                groupId = "com.zhangke.imageviewer"
                artifactId = "image-viewer"
                version = "0.1.3"

                from(components["release"])
            }
        }
    }
}

//configure<PublishingExtension> {
//    publications {
//        release(MavenPublication){
//            groupId = "com.zhangke.imageviewer"
//            artifactId = "imageviewer"
//            version = "0.0.8"
//        }
//        release(MavenPublication) {
//            groupId = "com.zhangke.imageviewer"
//            artifactId = "imageviewer"
//            version = "0.0.1"
//
//            afterEvaluate {
//                from(components.release)
//            }
//        }
//    }
//}

//afterEvaluate {
//    publishing {
//        publications {
//            create("release") {
//                groupId = "com.zhangke.imageviewer"
//                artifactId = "imageviewer"
//                version = "0.0.8"
//                from(components.release)
//            }
//        }
//    }
//}

//afterEvaluate {
//    publishing {
//        publications {
//            // Creates a Maven publication called "release".
//            release(MavenPublication) {
//                groupId = "com.zhangke.imageviewer"
//                artifactId = "imageviewer"
//                version = "0.0.8"
//            }
//        }
//    }
//}

