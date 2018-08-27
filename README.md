# Gradle Plugins for SAP (Hybris) Commerce

This projects provides two gradle plugins to ease the life of a hybris developer:

- [`mpern.sap.commerce.build`][build]: Automates the developer setup and allows you to work with the platform build inside gradle
- [`mpern.sap.commerce.ccv1.package`][package]: Creates CCv1 compliant deployment packages (Deployment Packagaging Guidelines v.2.3.3)
- [`mpern.sap.commerce.build.ccv2`][ccv2]: Use `manifest.json` to build and configure your commerce project locally

The plugins require Gradle 4+

To build, run `./gradlew build`

For the user documentation, please check the [Wiki](https://github.com/sap-commerce-tools/commerce-gradle-plugin/wiki)

There are various usage examples available in [sap-commerce-tools][tools], check the `*-example` repositories!

This project uses [Semantic Versioning 2.0.0](https://semver.org/spec/v2.0.0.html)

**Disclaimer**: *This is in no way, shape or form an official plugin. It is just something I came up with in my spare time,
and reflects what I (up until now) always did by hand and/or hacked together in ad-hoc build scripts.*

[build]: https://plugins.gradle.org/plugin/mpern.sap.commerce.build
[package]: https://plugins.gradle.org/plugin/mpern.sap.commerce.ccv1.package
[ccv2]: https://plugins.gradle.org/plugin/mpern.sap.commerce.build.ccv2
[tools]:https://github.com/sap-commerce-tools
