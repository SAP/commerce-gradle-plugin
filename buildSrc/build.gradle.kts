plugins {
    `kotlin-dsl`
    id("com.github.ben-manes.versions") version "0.47.0"
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("com.diffplug.spotless:spotless-plugin-gradle:6.20.0")
    implementation("com.gradle.publish:plugin-publish-plugin:1.2.0")
    implementation("pl.allegro.tech.build:axion-release-plugin:1.15.4")
    implementation("com.github.ben-manes:gradle-versions-plugin:0.47.0")
}