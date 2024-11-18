import com.vanniktech.maven.publish.SonatypeHost

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.android.library).apply(false)
    alias(libs.plugins.kotlin.android).apply(false)
    alias(libs.plugins.kotlin.jvm).apply(false)
    alias(libs.plugins.kotlin.multiplatform).apply(false)
    alias(libs.plugins.jetbrains.compose) apply false
    alias(libs.plugins.jetbrains.compose.compiler).apply(false)
    alias(libs.plugins.publish)
    alias(libs.plugins.dokka)
}

allprojects {
    group = "io.github.0xzhangke"
    version = ProjectVersion.VERSION

    plugins.withId("com.vanniktech.maven.publish.base") {
        mavenPublishing {
            publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)
            signAllPublications()
            pom {
                name.set("Imager-Viewer")
                description.set("Compose Imager-Viewer.")
                url.set("https://github.com/0xZhangKe/ImageViewer")
                licenses {
                    license {
                        name.set("Apache 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        id.set("zhangke")
                        name.set("Zhangke")
                        url.set("https://github.com/0xZhangKe")
                    }
                }
                scm {
                    url.set("https://github.com/0xZhangKe/ImageViewer")
                    connection.set("scm:git:git://github.com:0xZhangKe/ImageViewer.git")
                    developerConnection.set("scm:git:git://github.com:0xZhangKe/ImageViewer.git")
                }
            }
        }
    }

    // fix order of android release lint tasks
    listOf(
        "generateReleaseLintVitalModel",
        "lintVitalAnalyzeRelease",
    ).forEach { name ->
        tasks.matching { it.name == name }.configureEach {
            dependsOn(tasks.matching { it.name == "copyFontsToAndroidAssets" })
        }
    }
}

tasks.dokkaHtmlMultiModule {
    moduleVersion.set(ProjectVersion.VERSION)
    outputDirectory.set(rootDir.resolve("docs/static/api"))
}

object ProjectVersion {

    // incompatible API changes
    private const val MAJOR = "1"

    // functionality in a backwards compatible manner
    private const val MINOR = "1"

    // backwards compatible bug fixes
    private const val PATH = "0"

    const val VERSION = "$MAJOR.$MINOR.$PATH"
}
