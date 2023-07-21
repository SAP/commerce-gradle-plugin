import pl.allegro.tech.build.axion.release.domain.hooks.HookContext

plugins {
    id("groovy")
    id("com.gradle.plugin-publish") version "1.2.0"

    id("pl.allegro.tech.build.axion-release") version "1.15.3"
    id("com.diffplug.spotless") version "6.20.0"
    id("com.github.ben-manes.versions") version "0.47.0"
}

scmVersion {
    localOnly = true
    ignoreUncommittedChanges = true

    checks {
        aheadOfRemote = false
    }
    hooks {
        pre(
            "fileUpdate",
            mapOf(
                "encoding" to "utf-8",
                "file" to file("README.md"),
                "pattern" to KotlinClosure2({ pv: String, _: HookContext -> "$pv" }),
                "replacement" to KotlinClosure2({ cv: String, _: HookContext -> "$cv" }),
            ),
        )
        pre("commit")
    }
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
        importOrderFile("gradle/spotless.importorder")
        eclipse().configFile("gradle/spotless.xml")
    }
    groovy {
        target("src/*/groovy/**/*.groovy")
        importOrderFile("gradle/spotless.importorder")
        greclipse().configFile("gradle/greclipse.properties")
    }
    kotlinGradle {
        ktlint()
    }
}

group = "sap.commerce"
project.version = scmVersion.version

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.spockframework:spock-bom:2.1-groovy-3.0"))
    testImplementation("org.spockframework:spock-core")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

gradlePlugin {
    website = "https://github.com/SAP/commerce-gradle-plugin"
    vcsUrl = "https://github.com/SAP/commerce-gradle-plugin"

    plugins {
        create("hybrisPlugin") {
            id = "sap.commerce.build"
            implementationClass = "mpern.sap.commerce.build.HybrisPlugin"

            displayName = "SAP Commerce Bootstrap & Build Plugin"
            description = """Manage the whole development lifecycle of your SAP Commerce Project with Gradle"""
            tags = setOf(
                "sap commerce",
                "sap hybris commerce",
                "hybris",
                "sap",
                "commerce",
                "bootstrap",
                "build",
            )
        }
        create("ccv2BuildSupport") {
            id = "sap.commerce.build.ccv2"
            implementationClass = "mpern.sap.commerce.ccv2.CloudV2Plugin"

            displayName = "SAP Commerce Cloud in the Public Cloud Build Support Plugin"
            description = """Use the CCv2 manifest.json to configure and build your local development environment"""
            tags = setOf(
                "sap commerce",
                "sap hybris commerce",
                "hybris",
                "sap",
                "commerce",
                "ccv2",
                "public cloud",
                "manifest",
            )
        }
    }
}

sourceSets {
    create("commonTest") {
        compileClasspath += sourceSets.main.get().output + configurations.testRuntimeClasspath.get()
        runtimeClasspath += output + compileClasspath
    }
}

val commonTestImplementation by configurations.getting {
    extendsFrom(configurations.implementation.get())
}

sourceSets {
    create("integrationTest") {
        compileClasspath += sourceSets.main.get().output + configurations.testRuntimeClasspath.get() + sourceSets["commonTest"].output
        runtimeClasspath += output + compileClasspath
    }
}

tasks.register<Test>("integrationTest") {
    description = "Runs the integration tests."
    group = "verification"
    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath
    mustRunAfter("test")
}

tasks.named("check") {
    dependsOn("integrationTest")
}

sourceSets {
    create("functionalTest") {
        compileClasspath += sourceSets.main.get().output + configurations.testRuntimeClasspath.get() + sourceSets["commonTest"].output
        runtimeClasspath += output + compileClasspath
    }
}

tasks.register<Test>("functionalTest") {
    description = "Runs the functional tests."
    group = "verification"
    testClassesDirs = sourceSets["functionalTest"].output.classesDirs
    classpath = sourceSets["functionalTest"].runtimeClasspath
    mustRunAfter("test", "integrationTest")
}

tasks.named("check") {
    dependsOn("functionalTest")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    testLogging {
        events("passed", "failed", "skipped", "standardError")
        showStandardStreams = true
        showStackTraces = true
        showExceptions = true
    }
}
