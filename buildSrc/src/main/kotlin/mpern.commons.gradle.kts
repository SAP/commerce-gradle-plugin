plugins {
    java
    id("com.diffplug.spotless")
}

repositories {
    mavenCentral()
}

group = "sap.commerce"
project.version = rootProject.version

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs = listOf("-Xlint:deprecation", "-Xlint:unchecked")
}

spotless {
    format("misc") {
        // define the files to apply `misc` to
        target("*.md", ".gitignore")

        // define the steps to apply to those files
        trimTrailingWhitespace()
        leadingTabsToSpaces(4)
        endWithNewline()
    }
    java {
        target("src/*/java/**/*.java")
        removeUnusedImports()
        importOrderFile("../gradle/spotless.importorder")
        eclipse().configFile("../gradle/spotless.xml")
    }
    groovy {
        target("src/*/groovy/**/*.groovy")
        importOrderFile("../gradle/spotless.importorder")
        greclipse().configFile("../gradle/greclipse.properties")
    }
    kotlinGradle {
        ktlint()
    }
}
