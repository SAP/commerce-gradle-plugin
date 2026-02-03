plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("com.diffplug.gradle.spotless:com.diffplug.gradle.spotless.gradle.plugin:8.2.1")
    implementation("com.gradle.plugin-publish:com.gradle.plugin-publish.gradle.plugin:2.0.0")
    implementation("pl.allegro.tech.build.axion-release:pl.allegro.tech.build.axion-release.gradle.plugin:1.21.1")
    implementation("com.gradleup.shadow:com.gradleup.shadow.gradle.plugin:9.3.1")
}