# Plugin `mpern.sap.commerce.build.ccv2`

This plugins parses Commerce Cloud v2 [`manifest.json`][manifest] file and provides it to the gradle build script.

If you also use the `mpern.sap.commerce.build` plugin, it adds various tasks to
your build which are configured based on `manifest.json`

[manifest]: https://help.sap.com/viewer/1be46286b36a4aa48205be5a96240672/latest/en-US/2be55790d99e4a1dad4caa7a1fc1738f.html

## Configuration 
 
The following example shows the full DSL (Domain Specific Language) with all default options and the dependencies the 
plugin pre-configures.

```groovy
CCV2 {
    //target folder for the generate* tasks (details see below)
    generatedConfiguration = file('generated-configuration')
    
    //allows you a read-only access to the parsed manifest in your build script
    manifest = <parsed manifest.json>
}

```

If you also use `mpern.sap.commerce.build` in your build, the `hybris.version` is
preconfigured with `commerceSuiteVersion` of the manifest.


## Tasks

The plugin defines the following tasks

### `generateCloudProperties`

Generates `*.properties` files in `CCV2.generatedConfiguration` per aspect and persona as defined in `manifest.json`.

Filename schema: `<aspect>_<persona>.properties`.

The aspect `common` is used for the properties that are defined independent of any
aspect.

### `generateCloudLocalextensions`

Generates a `localextensions.xml` file in `CCV2.generatedConfiguration` based on
the `extensions` list in the manifest.

### `installManifestAddons`

**Only available if the build also uses `mpern.sap.commerce.build`**

Runs `ant addonistall` for all addons defined in `storefrontAddons` of the manifest.

### `cloudTests`

**Only available if the build also uses `mpern.sap.commerce.build`**

Runs `ant alltests` preconfigured with the values of the [`tests`][tests] object of the manifest

[tests]: https://help.sap.com/viewer/1be46286b36a4aa48205be5a96240672/latest/en-US/5ae6471137c44947a4f3051c753229d7.html

### `cloudWebTests`

**Only available if the build also uses `mpern.sap.commerce.build`**

Runs `ant webtests` preconfigured with the values of the [`webTests`][webtests] object of the manifest

[webtests]: https://help.sap.com/viewer/1be46286b36a4aa48205be5a96240672/latest/en-US/e978c15cad464c9eabb67bd868154377.html
