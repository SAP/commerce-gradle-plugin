# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

<!-- uncomment headings as required -->

<!-- ### Added -->
<!-- for new features. -->

<!-- #### Changed -->
<!-- for changes in existing functionality. -->

<!-- ### Deprecated -->
<!-- for soon-to-be removed features. -->

<!-- ### Removed -->
<!-- for now removed features. -->

<!-- ### Fixed -->
<!-- for any bug fixes. -->

<!-- ### Security -->
<!-- in case of vulnerabilities. -->

## [5.0.0] 2025-11-XX

### Added

- Gradle 9.x support - this is now the preferred Gradle version for the plugins
- Task ordering improvements.
- Support for `commerceSuitePreviewVersion` / `previewVersion` in `manifest.json`
  (see [SAP Help](https://help.sap.com/docs/SAP_COMMERCE_CLOUD_PUBLIC_CLOUD/1be46286b36a4aa48205be5a96240672/811b9e1cb1094da5bbe8e384345e73cc.html?locale=en-US); [#105])

### Changed

- Misc. internal refactor

### Fixed

- `sparseBootstrap` for `2211-jdk21` ([#103])

### Removed

- `removeUnusedExtensions` now completely removed. Please use `spareseBootstrap` instead
- Support for Gradle 7.6.x as it is [EOL]

[EOL]: https://docs.gradle.org/current/userguide/feature_lifecycle.html#eol_support
[#105]: https://github.com/SAP/commerce-gradle-plugin/issues/105
[#103]: https://github.com/SAP/commerce-gradle-plugin/issues/103
[config-cache]: https://docs.gradle.org/9.2.1/userguide/configuration_cache_enabling.html

## [4.1.0]

- Add support for JDK21 version of SAP Commerce
- Dependency Updates

## [4.0.0] 2023-07-31

### Added

- Gradle 8 support ([#45])
- Commerce build, `HybrisAntTask`: configure additional ant properties via command
  line flags ([#29]), e.g.
  ```bash
  > ./gradlew ybuild --antProperty=build.parallel=true --antProperty=foo.bar=false
  ```

### Changed

- Misc. internal refactoring ([#44], [#46])

### Deprecated

- Commerce build, `removeUnusedExtensions`: `sparseBootstrap` introduced in 3.9.0
  is the better way to save disk space

### Removed

- Dropped support for all Gradle versions < 7.6.2

### Fixed

- Commerce build, `removeUnusedExtensions`: do not modify default excludes ([#52])



[#29]: https://github.com/SAP/commerce-gradle-plugin/issues/29
[#45]: https://github.com/SAP/commerce-gradle-plugin/issues/45
[#52]: https://github.com/SAP/commerce-gradle-plugin/issues/52
[#46]: https://github.com/SAP/commerce-gradle-plugin/issues/46
[#44]: https://github.com/SAP/commerce-gradle-plugin/issues/44

## [3.10.0] 2023-07-17

### Added

- Commerce build: improve support of preview versions, e.g. `2211.FP1` with mapping to patch levels ([#53])

[#53]: https://github.com/SAP/commerce-gradle-plugin/issues/53

**Thank You** to [@iccaprar] for his continued support.

## [3.9.1] 2023-05-30

### Added

- Commerce build: preliminary support of preview versions, e.g. `2211.FP1` ([#48])
- Commerce build, sparse bootstrap: fix performance extension scan performance degradation when including smartedittools
  ([#49])

**Thank You** to [@iccaprar] for his continued support.

[#48]: https://github.com/SAP/commerce-gradle-plugin/issues/48
[#49]: https://github.com/SAP/commerce-gradle-plugin/issues/49

## [3.9.0]

### Added

- Commerce plugin: Add `sparseBootstrap` mode.

Only unpack the extensions your project needs instead of the whole zip.
Details can be found in the [Documentation](./docs/Plugin-sap.commerce.build.md)

A huge **Thank You** to [@iccaprar] for designing and implementing `sparseBootstrap`!

## [3.8.0]

### Added

- CCv2 Plugin, manifest validation: Add support for SAP Commerce 2211 and
  Integration Extension Pack 2211 ([#41])

Thank you [@iccaprar] for adding the 2211 compatibility check!

[#41]: https://github.com/SAP/commerce-gradle-plugin/issues/41
[@iccaprar]: https://github.com/iccaprar

## [3.7.1] 2022-06-15

### Fixed

- Commerce plugin, task `cleanPlatform`: Fix OutOfMemory error and performance issues

Details: [Conversation in Gradle Community Slack](https://gradle-community.slack.com/archives/CAHSN3LDN/p1655306254427809)

## [3.7.0] 2022-06-15

### Deprecated

- `sap.commerce.ccv1.package` - CCv1 is sunset.\
  The packaging plugin will be removed in the next major release

### Added

- Commerce plugin: [Lazy configuration] with fallback for ant properties ([$32])
- Commerce plugin: `HybrisAntTaks` support for 2205
- CCv2 Plugin, manifest validation: Add support for SAP Commerce 2105 and
  Integration Extension Pack 2108 ([#38])
- CCv2 Plugin, manifest validation: Add support for SAP Commerce 2205 and
    Integration Extension Pack 2205

### Fixed

- Windows compatibility ([#34])

Thank you [@tklostermannNSD] for adding the 2105 / 2108 compatibility check!

Special shout out to [@aepfli] for not only adding lazy configuration to `HybrisAntTas` and improving
the Windows compatibility, but for also adding the much-needed OS compatibility check to our
GH Actions flow.

[#32]: https://github.com/SAP/commerce-gradle-plugin/pull/32
[Lazy configuration]: https://docs.gradle.org/current/userguide/lazy_configuration.html
[#38]: https://github.com/SAP/commerce-gradle-plugin/pull/38
[@aepfli]: https://github.com/aepfli
[@tklostermannNSD]: https://github.com/tklostermannNSD

## [3.6.0] 2021-06-25

### Changed

- CCv2 Plugin, Tasks `cloudTests` and `cloudWebTests`: build fails when tests fail (`failbuildonerror=yes`, [#26])

### Fixed

- CCv2 Plugin, Tasks `cloudTests` and `cloudWebTests` Tasks are now always instances of `mpern.sap.commerce.build.tasks.HybrisAntTask` ([#27])

Thank you [@guiliguili] for reporting both issues!

[#26]: https://github.com/SAP/commerce-gradle-plugin/issues/26
[#27]: https://github.com/SAP/commerce-gradle-plugin/issues/27
[@guiliguili]: https://github.com/guiliguili


## [3.5.0] 2021-04-21

### Added

- Task `validateManifest` - Validate Integration Extension Pack\
  Ensure that the correct Integration Extension Pack is used.

### Changed

- `manifest.json` - Stricter validation for `extensionPacks`. Invalid entries now cause the parser to fail.

### Fixed

- Task `unpackPlatform` could fail in case of duplicate files. Now Gradle will emit a warning and overwrite the file.

## [3.4.0] 2021-03-15

### Added

- Task `validateManifest` \
  Validate `solrVersion` and warn if you customize solr without pinning the version.

## [3.3.0] 2021-01-20

### Added

- Task `validateManifest` - New Validations

  - Check if of cloud media conversion is correctly configured
  - Fail if `<extension>.webroot` properties are used

## [3.2.0] 2021-01-15

### Added

- CCv2: New Task `validateManifest`\
  Validate your `manifest.json` for common errors locally, instead of detecting error only after you have triggered
  the build in the cloud portal or, even worse, during deployment.\
  For details please check the [user documentation](docs/Plugin-sap.commerce.build.ccv2.md#validatemanifest)

- CCv2: Task `installManifestAddons`\
  Support for `storefrontAddons.addons`/ `storefrontAddons.storefronts` arrays ([Documentation][addons])

- Build: Configure default task dependencies for generated ant tasks ([#24])\
  You can now configure the default dependencies for ant tasks generated by the [ant task rule](docs/Plugin-sap.commerce.build.md#hybris-ant-rule)

  ```gradle
  hybris {
    antTaskDependencies.set(listOf("bootstrapPlatform"))
  }
   ```

[addons]: https://help.sap.com/viewer/1be46286b36a4aa48205be5a96240672/v2011/en-US/9a3ab7d08c704fccb7fd899e876d41d6.html
[#24]: https://github.com/SAP/commerce-gradle-plugin/issues/24

### Changed

- CCv2: Task `installManifestAddons`\
  Addons are now installed exactly in the same way as they are in the cloud build.

## [3.1.0] - 2020-12-23

### Added

- `removeUnusedExtensions` - remove all extensions that the project doesn't use (to save disk space)

## [3.0.0] - 2020-09-25

### Changed

- **Plugin IDs**\
  The new plugin IDs are:
  - `sap.commerce.build`
  - `sap.commerce.build.ccv2`
  - `sap.commerce.ccv1.package`

### Removed

- Removed `SupportPortalDownload`

## [2.1.1] - 2020-09-25

### Deprecated

- **Plugin IDs**\
  Since the plugin recently moved to the SAP open source organization, the plugin will soon be updated to reflect this change

  |Old|New|
  |---|---|
  |`mpern.sap.commerce.build`|`sap.commerce.build`|
  |`mpern.sap.commerce.build.ccv2`|`sap.commerce.build.ccv2`|
  |`mpern.sap.commerce.ccv1.package`|`sap.commerce.ccv1.package`|

- Task Type `SupportPortalDownload`\
  The logic to determine the download link for an artifact is quite complex and brittle,
  and there are easier ways to do achieve the same outcome.\
  Check the [FAQ](docs/FAQ.md) for more information


## [2.1.0] - 2020-06-16

### Added

- CCv2 Plugin: Support `extensionPacks` array ([docs][ref]). This is especially relevant for the new ["Integration Extension Pack"][pack]

[ref]: https://help.sap.com/viewer/1be46286b36a4aa48205be5a96240672/latest/en-US/ad98c976ab3d433e935b4b5c89303dd5.html
[pack]: https://help.sap.com/viewer/product/SAP_COMMERCE_INTEGRATIONS/latest/en-US

During the execution of the `bootstrapPlatform` task, the plugin resolves extension packs as dependencies as follows:

- `name` / `version` :\
    `de.hybris.platform:${name}:${version}@zip`
- `artifact`:\
    `${artifact}` (as is, without any changes)\
    if `artifact` is configured, `name` and `version` are ignored (as specified in the docs)

The plugin unpacks all resolved dependencies into the project root folder. (the same way as the platform.zip is unpacked).

### Changed

- Changed the default value for `hybris.cleanGlob` to `glob:**hybris/bin/{ext-**,platform**,modules**}` to match the new folder structure.

## [2.0.0] - 2020-02-16

### Added

- Support Gradle 6.x

### Removed

- Dropped support for Gradle 4.x due to changes in the Gradle Plugin API in 6.x

## [1.5.1] - 2019-10-22

### Fixed

- Change the file format of the deployment package checksum file to follow the latest packaging guidelines. ([#15])

[#15]: https://github.com/SAP/commerce-gradle-plugin/pull/15

A big "thank you" to [@I048752] for providing the fix!

[@I048752]: https://github.com/I048752

## [1.5.0] - 2019-08-09

### Added

- CCv2 Plugin: Cloud Extension Pack support

If the cloud extension pack is enabled in your `manifest.json` (`"useCloudExtensionPack": true`), the `bootstrapPlatform` task will automatically:

- download and unpack the extension pack (artifact coordinates: `de.hybris.platform:hybris-cloud-extension-pack:<commerce-version-without-patch>.+`) into the folder `cloud-extension-pack`
- patch `localextensions.xml` to load the extensions from the cloud extension pack, if necessary


### Changed

- Install addons defined in `manifest.json` faster


## [1.4.0] - 2019-04-09

### Added

- Task `buildCCV1Package` now declares task outputs - you can now easily (post)process the generated package ([#12])

[#12]: https://github.com/SAP/commerce-gradle-plugin/issues/12

## [1.3.3] - 2019-03-29

### Fixed

- Ensure correct ccv1 deployment package name ([#11])

[#11]: https://github.com/SAP/commerce-gradle-plugin/issues/11

## [1.3.2] - 2019-03-18

### Fixed

- Fix calling ant targets on the SAP JVM ([#10])

[#10]: https://github.com/SAP/commerce-gradle-plugin/issues/10

## [1.3.1] - 2019-03-07

### Fixed

-  CCv2 plugin correctly parses a minimal `manifest.json` ([#9])

[#9]: https://github.com/SAP/commerce-gradle-plugin/issues/9

## [1.3.0] - 2019-01-18

### Added

- Gradle 5+ support\
  Check the [kotlin-dsl-example][example] repository on how to write your build with the Kotlin DSL

- New property `sha256Sum` for `SupportPortalDownloadTask` to support checksums available on launchpad.support.sap.com (More details in the [documentation][doc] and the [FAQ][faq])

[doc]: https://github.com/SAP/commerce-gradle-plugin/wiki/Plugin-mpern.sap.commerce.build#mpernsapcommercebuildtaskssupportportaldownload
[faq]: https://github.com/SAP/commerce-gradle-plugin/wiki/FAQ#where-do-i-find-the-sha256sum-value-for-a-sap-commerce-distribution-in-the-sap-support-portal
[example]: https://github.com/sap-commerce-tools/kotlin-dsl-example

## [1.2.2] - 2019-01-17

### Changed

- CCv2 Plugin: you can override the platform version configured in `manifest.json` in the `build.gradle`

### Fixed

- Improve platform version detection

This should take care of [#8]

[#8]: https://github.com/SAP/commerce-gradle-plugin/issues/8

## [1.2.1] - 2018-12-14

### Fixed

- Keep case of ant target ([#5])

[#5]: https://github.com/SAP/commerce-gradle-plugin/issues/5

## [1.2.0] - 2018-10-26

### Added

- Include Solr customization in a CCv1 package (PR [#3])

A big "thank you" to [@karol-szczecinski-sap] for implementing the feature!

[#3]: https://github.com/SAP/commerce-gradle-plugin/pull/3
[@karol-szczecinski-sap]: https://github.com/karol-szczecinski-sap

## [1.1.1] - 2018-09-24

### Fixed

- Change glob pattern to match ant directory in 1808

## [1.1.0] - 2018-08-27

### Added

* CCV2 `manifest.json` support \
  Configure your local build using a `manifest.json` file using the new CCV2 build plugin.\
  Details can be found in the [wiki][ccv2.build]!

[ccv2.build]:https://github.com/SAP/commerce-gradle-plugin/wiki/Plugin-mpern.sap.commerce.build.ccv2

### Changed

* `HybrisAntTask` \
  configure Ant properties consistently via the new methods `antProperty(String key, String value)` and `setAntProperties(Map<String,String>)`

  Using the built-in method `systemProperty` (provided by `JavaExec`) doesn't work for all cases, unfortunately

## [1.0.2] - 2018-08-23

### Fixed

- Fixed ccv1 package generation on Linux (see PR [#1])

Shout out to [@corneleberle] for providing the fix.

[#1]: https://github.com/SAP/commerce-gradle-plugin/pull/1
[@corneleberle]: https://github.com/corneleberle

## [1.0.1] - 2018-06-29

### Fixed

- Fix unpack of platform zip on Windows

## [1.0.0] - 2018-06-25

:tada: Initial release :tada:

[Unreleased]: https://github.com/SAP/commerce-gradle-plugin/compare/v5.0.0...HEAD
[5.0.0]: https://github.com/SAP/commerce-gradle-plugin/compare/v4.1.0...v5.0.0
[4.1.0]: https://github.com/SAP/commerce-gradle-plugin/compare/v4.0.0...v4.1.0
[4.0.0]: https://github.com/SAP/commerce-gradle-plugin/compare/v3.10.0...v4.0.0
[3.10.0]: https://github.com/SAP/commerce-gradle-plugin/compare/v3.9.1...v3.10.0
[3.9.1]: https://github.com/SAP/commerce-gradle-plugin/compare/v3.9.0...v3.9.1
[3.9.0]: https://github.com/SAP/commerce-gradle-plugin/compare/v3.8.0...v3.9.0
[3.8.0]: https://github.com/SAP/commerce-gradle-plugin/compare/v3.7.1...v3.8.0
[3.7.1]: https://github.com/SAP/commerce-gradle-plugin/compare/v3.7.0...v3.7.1
[3.7.0]: https://github.com/SAP/commerce-gradle-plugin/compare/v3.6.0...v3.7.0
[3.6.0]: https://github.com/SAP/commerce-gradle-plugin/compare/v3.5.0...v3.6.0
[3.5.0]: https://github.com/SAP/commerce-gradle-plugin/compare/v3.4.0...v3.5.0
[3.4.0]: https://github.com/SAP/commerce-gradle-plugin/compare/v3.3.0...v3.4.0
[3.3.0]: https://github.com/SAP/commerce-gradle-plugin/compare/v3.2.0...v3.3.0
[3.2.0]: https://github.com/SAP/commerce-gradle-plugin/compare/v3.1.0...v3.2.0
[3.1.0]: https://github.com/SAP/commerce-gradle-plugin/compare/v3.0.0...v3.1.0
[3.0.0]: https://github.com/SAP/commerce-gradle-plugin/compare/v2.1.1...v3.0.0
[2.1.1]: https://github.com/SAP/commerce-gradle-plugin/compare/v2.1.0...v2.1.1
[2.1.0]: https://github.com/SAP/commerce-gradle-plugin/compare/v2.0.0...v2.1.0
[2.0.0]: https://github.com/SAP/commerce-gradle-plugin/compare/v1.5.1...v2.0.0
[1.5.1]: https://github.com/SAP/commerce-gradle-plugin/compare/v1.5.0...v1.5.1
[1.5.0]: https://github.com/SAP/commerce-gradle-plugin/compare/v1.4.0...v1.5.0
[1.4.0]: https://github.com/SAP/commerce-gradle-plugin/compare/v1.3.3...v1.4.0
[1.3.3]: https://github.com/SAP/commerce-gradle-plugin/compare/v1.3.2...v1.3.3
[1.3.2]: https://github.com/SAP/commerce-gradle-plugin/compare/v1.3.1...v1.3.2
[1.3.1]: https://github.com/SAP/commerce-gradle-plugin/compare/v1.3.0...v1.3.1
[1.3.0]: https://github.com/SAP/commerce-gradle-plugin/compare/v1.2.2...v1.3.0
[1.2.2]: https://github.com/SAP/commerce-gradle-plugin/compare/v1.2.1...v1.2.2
[1.2.1]: https://github.com/SAP/commerce-gradle-plugin/compare/v1.2.0...v1.2.1
[1.2.0]: https://github.com/SAP/commerce-gradle-plugin/compare/v1.1.1...v1.2.0
[1.1.1]: https://github.com/SAP/commerce-gradle-plugin/compare/v1.1.0...v1.1.1
[1.1.0]: https://github.com/SAP/commerce-gradle-plugin/compare/v1.0.2...v1.1.0
[1.0.2]: https://github.com/SAP/commerce-gradle-plugin/compare/v1.0.1...v1.0.2
[1.0.1]: https://github.com/SAP/commerce-gradle-plugin/compare/v1.0.0...v1.0.1
[1.0.0]: https://github.com/SAP/commerce-gradle-plugin/releases/tag/v1.0.0
