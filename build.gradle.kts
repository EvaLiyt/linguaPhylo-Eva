// buildSrc is a trap, use composite builds
// https://docs.gradle.org/current/userguide/structuring_software_products.html

// this is the umbrella build to define cross-build lifecycle tasks.
// https://docs.gradle.org/current/userguide/structuring_software_products_details.html

import java.text.SimpleDateFormat
import java.util.Calendar

plugins {
    `java-library`
    `maven-publish`
}

// Configures this project and each of its sub-projects.
allprojects {
    repositories {
        mavenCentral()
        // add sonatype snapshots repository
        maven {
            url=uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        }
        // Managing plugin versions via pluginManagement in settings.gradle.kts
//        mavenLocal() // only for testing
    }
}

// Configures the sub-projects of this project.
subprojects {
    group = "io.github.linguaphylo"

    var calendar: Calendar? = Calendar.getInstance()
    var formatter = SimpleDateFormat("dd-MMM-yyyy HH:mm:ss")

    // shared attributes
    tasks.withType<Jar>() {
        manifest {
            attributes(
                "Implementation-Vendor" to "LPhy team",
                "Implementation-Version" to archiveVersion,
                "Implementation-URL" to "https://github.com/LinguaPhylo/linguaPhylo",
                "Built-By" to "Walter Xie", //System.getProperty("user.name"),
                "Build-Jdk" to JavaVersion.current().majorVersion.toInt(),
                "Built-Date" to formatter.format(calendar?.time)
            )
        }
        // copy LICENSE to META-INF
        metaInf {
            from (rootDir) {
                include("LICENSE")
            }
        }
    }

    // configure the shared contents in MavenPublication especially POM
    afterEvaluate{
        extensions.configure<PublishingExtension>{
            publications {
                withType<MavenPublication>().all() {
                    // only for name.contains("lphy")
                    if (name.contains("lphy")) {
                        from(components["java"])
                        // Configures the version mapping strategy
                        versionMapping {
                            usage("java-api") {
                                fromResolutionOf("runtimeClasspath")
                            }
                            usage("java-runtime") {
                                fromResolutionResult()
                            }
                        }
                        pom {
                            name.set(project.name)
//                        description.set("...")
                            // compulsory
                            url.set("https://linguaphylo.github.io/")
                            packaging = "jar"
                            properties.set(
                                mapOf(
                                    "maven.compiler.source" to java.sourceCompatibility.majorVersion,
                                    "maven.compiler.target" to java.targetCompatibility.majorVersion
                                )
                            )
                            licenses {
                                license {
                                    name.set("GNU Lesser General Public License, version 3")
                                    url.set("https://www.gnu.org/licenses/lgpl-3.0.txt")
                                }
                            }
//                        developers {
// ...
//                        }
                            // https://central.sonatype.org/publish/requirements/
                            scm {
                                connection.set("scm:git:git://github.com/LinguaPhylo/linguaPhylo.git")
                                developerConnection.set("scm:git:ssh://github.com/LinguaPhylo/linguaPhylo.git")
                                url.set("https://github.com/LinguaPhylo/linguaPhylo")
                            }
                        }
                        println("Define MavenPublication ${name} and set shared contents in POM")
                    }
                }
            }
        }
    }


}

