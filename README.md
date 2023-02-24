# SAP Commerce Gradle Plugins 3.9.0

[![Actions Status](https://github.com/SAP/commerce-gradle-plugin/workflows/Gradle%20CI/badge.svg)](https://github.com/SAP/commerce-gradle-plugin/actions)
[![REUSE status](https://api.reuse.software/badge/github.com/SAP/commerce-gradle-plugin)](https://api.reuse.software/info/github.com/SAP/commerce-gradle-plugin)

Bootstrap, configure and build your SAP Commerce (Hybris) project using Gradle 5+.

**For the user documentation, please check the [`/docs` folder](/docs)**

The plugins are published to https://plugins.gradle.org/.

|Published Plugin|Documentation|Description|
|---|---|---|
|[`sap.commerce.build`][build]|[Documentation][build-doc]|Automates the developer setup and allows you to interact with the platform build using Gradle|
|[`sap.commerce.build.ccv2`][ccv2]|[Documentation][ccv2-doc]|Use `manifest.json` to configure and build your "SAP Commerce Cloud in the Public Cloud" (aka CCv2) project locally|
|[`sap.commerce.ccv1.package`][package]|[Documentation][package-doc]|Creates CCv1 compliant deployment packages (Deployment Packaging Guidelines v.2.3.3)|

This project uses [Semantic Versioning 2.0.0](https://semver.org/spec/v2.0.0.html)

## Getting Started

Here is how you get started with the plugins for your SAP Commerce project.

### Prerequisites

[Install Gradle](https://gradle.org/install/), in case you haven't yet.

### Examples

**Minimal Setup for CCv2 Manifest Validation**

1. ```cd <project>/core-customize```
2. (optional, but highly recommended) Initialize the [Gradle Wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html)

   ```shell
   gradle wrapper
   ```
3. Add a minimal Gradle build script:\
   `build.gradle.kts`

    ```kotlin
    plugins {
        id("sap.commerce.build.ccv2") version("3.9.0")
    }
    ```

4. `./gradlew validateManifest`

**Development Setup**

*For a fully automated, best-practice CCv2 project setup, refer to [sap-commerce-tools/ccv2-project-template](https://github.com/sap-commerce-tools/ccv2-project-template)*

1. ```cd <project>/core-customize```
2. (optional, but highly recommended) Initialize the [Gradle Wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html)

   ```shell
   gradle wrapper
   ```
3. Add Gradle build script \
   ```build.gradle.kts```

   ```kotlin
   plugins {
      id("sap.commerce.build") version("3.9.0")
      id("sap.commerce.build.ccv2") version("3.9.0")
   }

   repositories {
     //Please refer to the official Gradle documentation and the plugin documentation for additional
     // information about dependency resolution.

     // Option 1: Use a (custom) Maven repository to provide SAP Commerce artifacts for development
     maven {
        url = uri("https://custom.repo.com/maven")
     }
     // Option 2: Download all required files manually and put them in `dependencies` folder
     // There are ways to automate the downloads from launchpad.support.sap.com, please check the FAQ.
     // Make sure to rename the files accordingly (<artifactId>-<version>.zip)
     flatDir { dirs("dependencies") }

     mavenCentral()
   }
   ```
4. Enjoy things like:

   - `./gradlew bootstrapPlatform` - download (if you use Maven) and set up the correct SAP Commerce zip, extension packs, cloud extension packs, ..., as defined in `manifest.json`
   - `./gradlew installManifestAddons` - install all addons as defined in `manifest.json`
   - `./gradlew yclean yall` - run `ant clean all` using Gradle. You can run any Ant target provided by SAP Commerce as `y<target>`.
   - `./gradlew validateManifest`- validate your manifest for common errors. Now with additional checks because
     the full platform is available.
   - `./gradlew cloudTests cloudWebTests`- run the tests defined in `manifest.json`


Don't forget to commit the Gradle Wrapper and your build script.

## Support

Please raise an [issue] in this GitHub project, following the guidelines outlined in [CONTRIBUTING.md]

## Contributing

Please refer to [CONTRIBUTING.md]

[CONTRIBUTING.md]: CONTRIBUTING.md
[issue]: https://github.com/SAP/commerce-gradle-plugin/issues

[build]: https://plugins.gradle.org/plugin/sap.commerce.build
[build-doc]: docs/Plugin-sap.commerce.build.md
[package]: https://plugins.gradle.org/plugin/sap.commerce.ccv1.package
[package-doc]: docs/Plugin-sap.commerce.ccv1.package.md
[ccv2]: https://plugins.gradle.org/plugin/sap.commerce.build.ccv2
[ccv2-doc]: docs/Plugin-sap.commerce.build.ccv2.md
[tools]:https://github.com/sap-commerce-tools
