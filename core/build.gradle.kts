// The canvas, the styles, the drawing functions and the library c0de_paint/cpaint: plain Java on the interpreter of c0de,
// without Minecraft or a mod loader. The access to the world (INVENTORY_BLOCKS, RENDER_CANVAS, CANVAS_FROM_WORLD) is an
// interface that the mod implements. The mod nodes compile these sources directly (see build.fabric.gradle.kts and
// build.neoforge.gradle.kts), where c0de is a mod; this project tests them with the interpreter of c0de, the runner
// jar of its GitHub release.
//
// Formatting of the other files (mod sources with Stonecutter comments, build scripts) is checked by
// scripts/check-whitespace.sh, because Spotless only accepts files within its project directory.
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
val c0deVersion = property("deps.c0de")

dependencies {
    // The interpreter of c0de (the API of libraries, values, the cursor); in the mod, c0de itself provides it
    compileOnly(c0deRunner(c0deVersion))
    testImplementation(c0deRunner(c0deVersion))
    testImplementation(platform("org.junit:junit-bom:6.1.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
    // A test that hangs fails instead of blocking the build
    systemProperty("junit.jupiter.execution.timeout.default", "60 s")
}

// Formatting: `./gradlew spotlessCheck` (part of `check`) and `./gradlew spotlessApply`
spotless {
    java {
        target("src/**/*.java")
        palantirJavaFormat()
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
}
