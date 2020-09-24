# Gradle Plugins for SAP (Hybris) Commerce 2.1.0

[![Actions Status](https://github.com/sap-commerce-tools/commerce-gradle-plugin/workflows/Gradle%20CI/badge.svg)](https://github.com/sap-commerce-tools/commerce-gradle-plugin/actions)
[![REUSE status](https://api.reuse.software/badge/github.com/SAP/commerce-gradle-plugin)](https://api.reuse.software/info/github.com/SAP/commerce-gradle-plugin)

Bootstrap, configure and build your SAP Commerce (Hybris) project using Gradle 5+.

For the user documentation, please check the [Wiki](https://github.com/sap-commerce-tools/commerce-gradle-plugin/wiki)\
There are also various examples available in [sap-commerce-tools][tools], check the repositories ending with `-example`

(**The plugins are already published to https://plugins.gradle.org/**, so you can just use them for your commerce project)

|Published Plugin|Documentation|Description|
|---|---|---|
|[`mpern.sap.commerce.build`][build]|[Documentation][build-doc]|Automates the developer setup and allows you to work with the platform build inside gradle|
|[`mpern.sap.commerce.build.ccv2`][ccv2]|[Documentation][ccv2-doc]|Use `manifest.json` to configure and build your "Commerce Cloud in the Public Cloud" (aka CCv2) project locally|
|[`mpern.sap.commerce.ccv1.package`][package]|[Documentation][package-doc]|Creates CCv1 compliant deployment packages (Deployment Packagaging Guidelines v.2.3.3)|

This project uses [Semantic Versioning 2.0.0](https://semver.org/spec/v2.0.0.html)

## Support

Please raise an [issue] in this project, following the guidelines outlined in [CONTRIBUTING.md]

## Contributing

Please refer to [CONTRIBUTING.md]()

[CONTRIBUTING.md]: CONTRIBUTING.md
[issue]: https://github.com/SAP/commerce-gradle-plugin/issues

[build]: https://plugins.gradle.org/plugin/mpern.sap.commerce.build
[build-doc]: docs/Plugin-mpern.sap.commerce.build.md
[package]: https://plugins.gradle.org/plugin/mpern.sap.commerce.ccv1.package
[package-doc]: docs/Plugin-mpern.sap.commerce.ccv1.package.md
[ccv2]: https://plugins.gradle.org/plugin/mpern.sap.commerce.build.ccv2
[ccv2-doc]: docs/Plugin-mpern.sap.commerce.build.ccv2.md
[tools]:https://github.com/sap-commerce-tools
