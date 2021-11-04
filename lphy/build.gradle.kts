import java.nio.file.*

plugins {
    `java-library`
    `maven-publish`
}

group = "lphy"
version = "1.1-SNAPSHOT"


dependencies {
    // required in test
    api("org.antlr:antlr4-runtime:4.8")
    api("org.apache.commons:commons-math3:3.6.1")
    api("org.apache.commons:commons-lang3:3.10")
    // in maven
    api("org.scilab.forge:jlatexmath:1.0.7")
    api("org.scilab.forge:jlatexmath-font-greek:1.0.7")
    api("org.scilab.forge:jlatexmath-font-cyrillic:1.0.7")
    api("net.steppschuh.markdowngenerator:markdowngenerator:1.3.1.1")

    // not in maven
    api(files("lib/jebl-3.0.1.jar"))
    //implementation(fileTree("lib") { exclude("junit-*.jar") })

    testImplementation("junit:junit:4.13.2")
//    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:4.13")
}

java {
    sourceCompatibility = JavaVersion.VERSION_16
    targetCompatibility = JavaVersion.VERSION_16
    withSourcesJar()
}

// overwrite compileJava to use module-path
// overwrite compileJava to pass dependencies to tests
tasks.compileJava {
    // use the project's version or define one directly
    options.javaModuleVersion.set(provider { project.version as String })

    doFirst {
        println("Java version used is ${JavaVersion.current()}.")
        options.compilerArgs = listOf("--module-path", classpath.asPath)
        classpath = files()
    }

    doLast {
        println("${project.name} compiler args = ${options.compilerArgs}")
    }
}

// lphy-$version.jar
tasks.jar {
    manifest {
        // shared attr in the root build
        attributes(
            "Implementation-Title" to "LPhy",
        )
    }
}

// configure core dependencies, which can be reused in lphy-studio
val coreJars by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
    extendsFrom(configurations["api"])
}
artifacts {
    add("coreJars", tasks.jar)
}

// publishing
val releaseDir = "releases"
tasks.withType<AbstractPublishToMaven>().configureEach {
    doFirst {
        val path: java.nio.file.Path = Paths.get("${rootDir}", releaseDir)
        if (Files.exists(path)) {
            println("Delete the existing previous release : ${path.toAbsolutePath()}")
            project.delete(path)
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("LPhy") {
            artifactId = "core"
            from(components["java"])
        }
    }

    repositories {
        maven {
            name = releaseDir
            url = uri(layout.buildDirectory.dir("${rootDir}/${releaseDir}"))
            println("Set the base URL of $releaseDir repository to : ${url.path}")
        }
    }
}


// junit tests, https://docs.gradle.org/current/dsl/org.gradle.api.tasks.testing.Test.html
tasks.test {
    useJUnit()
    // useJUnitPlatform()
    // set heap size for the test JVM(s)
    minHeapSize = "128m"
    maxHeapSize = "1G"
    // show standard out and standard error of the test JVM(s) on the console
    testLogging.showStandardStreams = true
    //testLogging.exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
}

// list locations of jars in dependencies
tasks.register("showCache") {
    doLast {
        configurations.compileClasspath.get().forEach { println(it) }
    }
}