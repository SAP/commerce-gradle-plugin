plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("com.diffplug.gradle.spotless:com.diffplug.gradle.spotless.gradle.plugin:7.2.1")
    implementation("com.gradle.plugin-publish:com.gradle.plugin-publish.gradle.plugin:1.3.1")
    implementation("pl.allegro.tech.build.axion-release:pl.allegro.tech.build.axion-release.gradle.plugin:1.20.1")
    implementation("com.gradleup.shadow:com.gradleup.shadow.gradle.plugin:8.3.9")
}