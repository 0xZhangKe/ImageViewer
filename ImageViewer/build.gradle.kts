import org.gradle.kotlin.dsl.sourceSets
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.compose.compiler)
    alias(libs.plugins.jetbrains.compose)
    id("maven-publish")
}

android {
    namespace = "com.zhangke.imageviewer"
    compileSdk = 35

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
}

kotlin {
    sourceSets.all {
        languageSettings {
            apiVersion = KotlinVersion.KOTLIN_2_0.version
            languageVersion = KotlinVersion.KOTLIN_2_0.version
        }
    }
    androidTarget()
    jvm()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    sourceSets {
        commonMain {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.ui)
                implementation(compose.foundation)
            }
        }
        androidMain {
            dependencies {
            }
        }
    }
}

//dependencies {
//    testImplementation(libs.junit)
//    androidTestImplementation(libs.androidx.junit)
//    androidTestImplementation(libs.androidx.espresso.core)
//
//    implementation(libs.androidx.ui)
//    implementation(libs.androidx.ui.graphics)
//    implementation(libs.androidx.compose.foundation)
//}
//afterEvaluate {
//    publishing {
//        publications {
//            create<MavenPublication>("maven") {
//                groupId = "com.zhangke.imageviewer"
//                artifactId = "image-viewer"
//                version = "1.0.3"
//
//                from(components["release"])
//            }
//        }
//    }
//}
