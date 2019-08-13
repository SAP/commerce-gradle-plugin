**Disclaimer**: *This is in no way, shape or form an official plugin provided by SAP. It is just something I came up with in my spare time,
and reflects what I (up until now) always did by hand and/or hacked together in ad-hoc build scripts.*

# Gradle Plugins for SAP (Hybris) Commerce 1.5.0

[![Actions Status](https://wdp9fww0r9.execute-api.us-west-2.amazonaws.com/production/badge/sap-commerce-tools/commerce-gradle-plugin)](https://wdp9fww0r9.execute-api.us-west-2.amazonaws.com/production/results/sap-commerce-tools/commerce-gradle-plugin)

> Shout out to [@CultureHQ](https://github.com/CultureHQ) for providing [github-actions-badge](https://github.com/CultureHQ/github-actions-badge)

This projects provides two gradle plugins to ease the life of a hybris developer \
(**The plugins are already published to https://plugins.gradle.org/**, so you can just use them for your commerce project)

|Published Plugin|Documentation|Description|
|---|---|---|
|[`mpern.sap.commerce.build`][build]|[Documentation][build-doc]|Automates the developer setup and allows you to work with the platform build inside gradle|
|[`mpern.sap.commerce.build.ccv2`][ccv2]|[Documentation][ccv2-doc]|**Experimental** Use `manifest.json` to build and configure your commerce project locally|
|[`mpern.sap.commerce.ccv1.package`][package]|[Documentation][package-doc]|Creates CCv1 compliant deployment packages (Deployment Packagaging Guidelines v.2.3.3)|

The plugins require Gradle 4+

For the user documentation, please check the [Wiki](https://github.com/sap-commerce-tools/commerce-gradle-plugin/wiki)

There are various usage examples available in [sap-commerce-tools][tools], check the repositories ending with `-example`


This project uses [Semantic Versioning 2.0.0](https://semver.org/spec/v2.0.0.html)

[build]: https://plugins.gradle.org/plugin/mpern.sap.commerce.build
[build-doc]: https://github.com/sap-commerce-tools/commerce-gradle-plugin/wiki/Plugin-mpern.sap.commerce.build
[package]: https://plugins.gradle.org/plugin/mpern.sap.commerce.ccv1.package
[package-doc]: https://github.com/sap-commerce-tools/commerce-gradle-plugin/wiki/Plugin-mpern.sap.commerce.ccv1.package
[ccv2]: https://plugins.gradle.org/plugin/mpern.sap.commerce.build.ccv2
[ccv2-doc]: https://github.com/sap-commerce-tools/commerce-gradle-plugin/wiki/Plugin-mpern.sap.commerce.build.ccv2
[tools]:https://github.com/sap-commerce-tools
