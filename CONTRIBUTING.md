# Contributing to commerce-gradle-plugin

Thank you for taking the time to invest in these plugins! Let's make the life of Commerce developers easier together.

## Bug Reports

Please ensure that you use the [latest version][latest] of the plugin(s) and also check the [issues] if there is a similar bug already filed.

If you have found a new bug, please make sure to provide as much detail as you can, or, if possible, provide a link to a minimal repository where the bug can be reproduced.

As you probably know, reproducing bugs can be very hard. The faster we can reproduce the bug, the faster it will get fixed.

[latest]: https://github.com/SAP/commerce-gradle-plugin/releases
[issues]: https://github.com/SAP/commerce-gradle-plugin/issues

## Feature Requests

You have an idea for a new feature or a new automation that you could benefit the SAP Commerce Cloud developer community?

Just open a new issue and describe your idea!

## Contributing Code Changes

Here is the short version of how you can contribute changes:

1. Fork the repository (see the [github help][fork] for further guidance)
1. Implement your new feature on a new git branch
1. Provide unit, functional and/or integration tests
1. Make sure to apply the project code style using `gradlew spotlessApply`
1. Open a pull request ([github help][pr])

[fork]: https://help.github.com/articles/fork-a-repo
[pr]: https://docs.github.com/en/github/collaborating-with-issues-and-pull-requests/creating-a-pull-request

All major Java IDEs can easily import Gradle projects and therefore also this project.\
Please use the Gradle Wrapper provided in the project for your build.

### Code Style

We enforce a consistent code style for the whole project using [spotless][spotless] in combination with the Eclipse JDT formatter. You can find the formatter configuration files in `gradle/spotless.xml` and `gradle/spotless.importorder`.\
The PR build will *fail* if the you don't follow the project code style.

[spotless]: https://github.com/diffplug/spotless/tree/main/plugin-gradle

### Manual Integration Testing

Some features are very hard to test automatically, especially if they depend on the behavior of the SAP Commerce build.

You can use a Gradle [composite build][manual] to test your local plugin version with a Commerce project.
An example of such a setup is provided in the folder `manualTest`.

[manual]: https://guides.gradle.org/testing-gradle-plugins/#manual-tests

## Developer Certificate of Origin (DCO)

Due to legal reasons, contributors will be asked to accept a DCO before they submit the first pull request to this projects, this happens in an automated fashion during the submission process. SAP uses [the standard DCO text of the Linux Foundation](https://developercertificate.org/).
