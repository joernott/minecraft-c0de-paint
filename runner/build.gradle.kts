// The runner library (task 8.2): c0de_paint/cpaint for the program runner of c0de, which runs c0de programs without
// Minecraft (docs/runner.md in minecraft-c0de). It contains `core` (the canvas and the library) and a world without
// Minecraft, in which RENDER_CANVAS writes the placed blocks to a YAML file. Released as
// c0de-paint-runner-<version>.jar; it needs the runner of c0de on the class path:
//
//   java -cp c0de-runner-<c0de version>.jar:c0de-paint-runner-<version>.jar de.joernott.c0de.core.runner.Main program.c0de
plugins {
    `java-library`
    id("com.diffplug.spotless") version "8.11.0"
}

repositories {
    mavenCentral()
    c0deReleases()
}

java {
    toolchain {
        // The lowest Java version of all nodes (Minecraft 1.21.x)
        languageVersion = JavaLanguageVersion.of(21)
    }
}

val properties = rootProject.file("stonecutter.properties.toml").readText()
fun property(name: String): String =
    Regex("""(?m)^${Regex.escape(name)}\s*=\s*"([^"]+)"""").find(properties)!!.groupValues[1]
val modVersion = property("mod.version")
val c0deVersion = property("deps.c0de")

sourceSets {
    main {
        java {
            srcDir(rootProject.file("core/src/main/java"))
            include("de/joernott/c0depaint/runner/**", "de/joernott/c0depaint/core/**")
        }
    }
}

dependencies {
    // The interpreter of c0de; the runner of c0de brings it at run time
    compileOnly(c0deRunner(c0deVersion))
    testImplementation(c0deRunner(c0deVersion))
    testImplementation(platform("org.junit:junit-bom:6.1.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.jar {
    archiveFileName = "c0de-paint-runner-$modVersion.jar"
    manifest {
        attributes("Implementation-Title" to "c0de-paint-runner", "Implementation-Version" to modVersion)
    }
}

tasks.test {
    useJUnitPlatform()
    // The handbook and its translations, whose programs must be valid with the library
    systemProperty("c0depaint.docs", rootProject.file("docs").absolutePath)
    systemProperty("junit.jupiter.execution.timeout.default", "60 s")
}

// Formatting: `./gradlew :runner:spotlessCheck` (part of `check`) and `./gradlew :runner:spotlessApply`
spotless {
    java {
        target("src/**/*.java")
        palantirJavaFormat()
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
}
