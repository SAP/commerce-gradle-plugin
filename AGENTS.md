# AGENTS.md

This file provides guidance to code agents when working with code in this repository.

## Overview

This is a multi-module Gradle plugin project that provides two published plugins for SAP Commerce (Hybris) development:

- `sap.commerce.build` - automates developer setup, bootstraps the platform, and wraps Ant targets as Gradle tasks
- `sap.commerce.build.ccv2` - reads `manifest.json` to configure and build CCv2 (SAP Commerce Cloud in the Public Cloud) projects locally

## Build Commands

```sh
# Build and run all checks (unit + integration + functional tests, spotless)
./gradlew check

# Run only tests, skipping code style checks
./gradlew check -x spotlessCheck

# Fix code style issues (must be run before committing)
./gradlew spotlessApply

# Check code style without fixing
./gradlew spotlessCheck

# Run tests for a specific subproject
./gradlew :build-plugin:check
./gradlew :cloud-plugin:check

# Run a specific test suite
./gradlew :build-plugin:test
./gradlew :build-plugin:integrationTest
./gradlew :build-plugin:functionalTest

# Run a single test class (Spock uses --tests with the class name)
./gradlew :build-plugin:test --tests "mpern.sap.commerce.build.util.VersionTest"
./gradlew :cloud-plugin:integrationTest --tests "mpern.sap.commerce.ccv2.validation.AspectValidatorSpec"

# Check for dependency updates
./gradlew dependencyUpdates --refresh-dependencies
```

## Project Structure

```
commerce-gradle-plugin/
├── buildSrc/                  # Convention plugins used by all subprojects
│   └── src/main/kotlin/
│       ├── mpern.commons.gradle.kts       # Java 17 toolchain + spotless config
│       └── mpern.plugin.basics.gradle.kts # Groovy, JVM test suites, shadow JAR, plugin publishing
├── plugin-commons/            # Shared constants (e.g. extension names)
├── build-plugin/              # sap.commerce.build plugin (Java + Groovy tests)
├── cloud-plugin/              # sap.commerce.build.ccv2 plugin (Java + Groovy tests)
├── test-utils/                # Shared test helpers and fixture resources
└── manualTest/                # Composite build for manual integration testing
```

## Architecture

**`build-plugin`** (`HybrisPlugin.java`): Registers the `hybris` extension and tasks for bootstrapping the SAP Commerce platform - `bootstrapPlatform`, `cleanPlatform`, `unpackPlatform`, `setupDbDriver`. Uses `HybrisAntRule` to dynamically create `y<target>` tasks that delegate to the SAP Commerce Ant build.

**`cloud-plugin`** (`CloudV2Plugin.java`): Reads `manifest.json` at configuration time using `JsonSlurper` (Groovy) and maps it to the `Manifest` model. Applies on top of `build-plugin` when both are active. Registers CCv2-specific tasks: `validateManifest`, `generateCloudLocalextensions`, `generateCloudProperties`, `installManifestAddons`, `cloudTests`, `cloudWebTests`.

**Dependency flow**: `cloud-plugin` → `build-plugin` → `plugin-commons`.

## Test Structure

Each plugin subproject has three test suites (configured in `mpern.plugin.basics.gradle.kts`):

- `test` - unit tests (Spock `Specification`)
- `integrationTest` - tests using real fixture files from `test-utils/src/main/resources/`
- `functionalTest` - end-to-end tests using Gradle TestKit

All tests use [Spock Framework](https://spockframework.org/) with JUnit Jupiter. `check` runs all three suites.

## Code Style

Spotless enforces formatting automatically. The build **will fail** if code is not formatted. Run `./gradlew spotlessApply` before committing.

- Java: Eclipse JDT formatter (`gradle/spotless.xml`) + import order (`gradle/spotless.importorder`)
- Groovy: GrEclipse (`gradle/greclipse.properties`)
- Kotlin Gradle scripts: ktlint

## Manual Integration Testing

The `manualTest/` folder is a Gradle composite build that uses the local plugin version via `includeBuild("../")`. It requires a real SAP Commerce `manifest.json` and platform zips. Use it to test features that depend on the SAP Commerce Ant build.

## Release Process

Uses `pl.allegro.tech.build.axion-release`. Versions are derived from git tags.

```sh
git switch main
./gradlew release -Prelease.versionIncrementer=incrementPatch   # or incrementMinor / incrementMajor
git push --follow-tags
```

Before releasing: update `CHANGELOG.md`, verify with `-Prelease.dryRun` (then `git restore README.md`), and create a GitHub Release with the tag and changelog content.
