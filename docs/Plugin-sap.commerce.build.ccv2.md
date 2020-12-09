# Plugin `sap.commerce.build.ccv2`

This plugins parses Commerce Cloud v2 [`manifest.json`][manifest] file and provides it to the gradle build script.

If you also use the `sap.commerce.build` plugin, it adds various tasks to
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

If you also use `sap.commerce.build` in your build, the `hybris.version` is
preconfigured with `commerceSuiteVersion` of the manifest.

### `extenionPacks` Support

All artifacts configured as additional `extensionPacks` in your `manifest.json` will also be unpacked into the root of your repository during [`bootstrapPlatform`][bootstrap].
This allows you e.g. to use the "Integration Extension Pack" locally.

See also:

- ["Deploying the Integrations Pack on SAP Commerce Cloud"][pack]
- ["Extension Packs"][packs]

**How are `extensionPacks` resolved as Maven artifacts?**

- `name` / `version` :\
    `de.hybris.platform:${name}:${version}@zip`
- `artifact`:\
    `${artifact}` (as is, without any changes)\
    If `artifact` is configured, `name` and `version` are ignored (as specified in the docs)

[bootstrap]: /docs/Plugin-sap.commerce.build.md#bootstrapplatform
[pack]: https://help.sap.com/viewer/2f43049ad8e443249e1981575adddb5d/LATEST/en-US/19bacaecbdd34cc8bd58bdd8daf428c5.html
[packs]: https://help.sap.com/viewer/1be46286b36a4aa48205be5a96240672/LATEST/en-US/ad98c976ab3d433e935b4b5c89303dd5.html

### `useCloudExtensionPack` Support

> The Cloud Extension Pack is only available for SAP Commerce 1811 and 1905.
> Starting with 2005 it is replaced by the "Integration Extension Pack" (see above)

If the cloud extension pack is enabled in your `manifest.json` (`useCloudExtensionPack: true`), the `bootstrapPlatform` task will automatically:

- Download and unpack the extension pack (artifact coordinates: `de.hybris.platform:hybris-cloud-extension-pack:<commerce-version-without-patch>.+`) into `cloud-extension-pack`
- Patch `localextensions.xml` to load the extensions from the cloud extension pack, if necessary


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
