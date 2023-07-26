plugins {
    java
    id("com.diffplug.spotless")
    id("com.github.ben-manes.versions")
}

repositories {
    mavenCentral()
}

group = "sap.commerce"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
        vendor = JvmVendorSpec.SAP
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
        indentWithSpaces(4)
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
