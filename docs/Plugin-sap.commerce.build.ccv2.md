# Plugin `sap.commerce.build.ccv2`

This plugins parses Commerce Cloud v2 [`manifest.json`][manifest] file and provides it to the Gradle build script.

If you also use the `sap.commerce.build` plugin, it preconfigures various tasks based on `manifest.json`

[manifest]: https://help.sap.com/viewer/1be46286b36a4aa48205be5a96240672/latest/en-US/2be55790d99e4a1dad4caa7a1fc1738f.html

## Configuration

The following example shows the full DSL (Domain Specific Language) with all default options and the dependencies the
plugin pre-configures.

```groovy
CCV2 {
    //target folder for the `generate*` tasks (details see below)
    generatedConfiguration = file('generated-configuration')

    //Use this property to access the manifest.json in your Gradle build script
    manifest = < parsed manifest.json >
}
```

If you also use `sap.commerce.build` in your build, the CCv2 plugin preconfigures `hybris.version` with `commerceSuiteVersion` or `commerceSuitePreviewVersion` (details see next heading) as defined in your `manifest.json`.

### Preview Release Support

General information: [Preview Releases]

Starting with 5.0.0, the CCV2 plugin fully supports preview releases both for SAP Commerce Cloud (key `commercePreviewVersion`) and extension packs (key `previewVersion` in the `extensionPacks` array; more details [here][preview-manifest] and [here][preview-pack])

[Preview Releases]: https://help.sap.com/docs/SAP_COMMERCE_CLOUD_PUBLIC_CLOUD/75d4c3895cb346008545900bffe851ce/6221aeb97f784d55856bfdff0bd05e0e.html?locale=en-US
[preview-manifest]: https://help.sap.com/docs/SAP_COMMERCE_CLOUD_PUBLIC_CLOUD/1be46286b36a4aa48205be5a96240672/811b9e1cb1094da5bbe8e384345e73cc.html?locale=en-US&version=LATEST
[preview-pack]: https://help.sap.com/docs/SAP_COMMERCE_INTEGRATIONS/58d2065698d847efaa44e08c3556ae96/fdc6a80d9de54f00bdb6edc83a7c01a1.html?locale=en-US&version=LATEST

To get the currently effective version (regular or preview release) in your Gradle build script use `CCV2.manifest.effectiveVersion` (`String getEffectiveVersion()`).

If your build need to handle preview versions differently, you can check for a preview with `CCV2.manifest.preview` (`boolean isPreview()`)

>[!IMPORTANT]
> **Preview Releases - Default Maven Coordinates**
>
> If you use `commerceSuitePreviewVersion`  or `previewVersion` of an extension pack, it changes the
> [`groupId`][coordinates] of the dependency to `de.hybris.platform.preview` instead of the default value of
> `de.hybris.platform` of regular releases.
>
> Maven coordinates of `commerceSuitePreviewVersion`:
>
> ```
> de.hybris.platform.preview:hybris-commerce-suite:${commerceSuitePreviewVersion}@zip
> ```
>
> Maven coordinates of an extension pack defining `previewVersion`:
>
> ```
> de.hybris.platform.preview:${name}:${previewVersion}@zip
> ```

[coordinates]: https://maven.apache.org/pom.html#maven-coordinates

### `extenionPacks` Support

All artifacts configured as additional `extensionPacks` in your `manifest.json` will also be unpacked into the root of
your repository during [`bootstrapPlatform`][bootstrap]. This allows you to easily bootstrap e.g. the "Integration
Extension Pack" locally.

See also:

- [Deploying the Integrations Pack on SAP Commerce Cloud][pack]
- [Extension Packs][packs]

**How are `extensionPacks` resolved as Maven artifacts?**

- If `name` / `version` is supplied:\
  `de.hybris.platform:${name}:${version}@zip`
- If `artifact` is supplied:\
  `${artifact}` (as is, without any changes)\
  (If `artifact` is configured, `name` and `version` are ignored, as specified in the docs)

[bootstrap]: /docs/Plugin-sap.commerce.build.md#bootstrapplatform

[pack]: https://help.sap.com/viewer/2f43049ad8e443249e1981575adddb5d/LATEST/en-US/19bacaecbdd34cc8bd58bdd8daf428c5.html

[packs]: https://help.sap.com/viewer/1be46286b36a4aa48205be5a96240672/LATEST/en-US/ad98c976ab3d433e935b4b5c89303dd5.html

## Tasks

The plugin defines the following tasks

### `validateManifest`

Validate `manifest.json` for common issues. If errors are detected, the task fails. Warnings are logged, but do not
cause the task to fail.

You can find all possible errors and warnings in [ccv2-validation.md](ccv2-validation.md)

### `installManifestAddons`

**Only available if the build also uses `sap.commerce.build`**

Runs `ant addonistall` for all addons defined in `storefrontAddons` of the manifest.

### `cloudTests`

**Only available if the build also uses `sap.commerce.build`**

Runs `ant alltests` preconfigured with the values of the [`tests`][tests] object of the manifest

[tests]: https://help.sap.com/viewer/1be46286b36a4aa48205be5a96240672/latest/en-US/5ae6471137c44947a4f3051c753229d7.html

### `cloudWebTests`

**Only available if the build also uses `sap.commerce.build`**

Runs `ant webtests` preconfigured with the values of the [`webTests`][webtests] object of the manifest

[webtests]: https://help.sap.com/viewer/1be46286b36a4aa48205be5a96240672/latest/en-US/e978c15cad464c9eabb67bd868154377.html

### `generateCloudProperties`

Generates `*.properties` files (into the folder configured by `CCV2.generatedConfiguration`) per aspect and persona as
defined in `manifest.json`.

Filename schema: `<aspect>_<persona>.properties`.

The aspect `common` is used for the properties that are shared between all aspects.

### `generateCloudLocalextensions`

Generates a `localextensions.xml` file (into the folder configured by `CCV2.generatedConfiguration`) based on
the `extensions` list in the manifest.
