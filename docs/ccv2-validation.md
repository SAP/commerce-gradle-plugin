# CCv2 Validation Errors

Here you can find additional information for the errors and warnings reported by `validateManifest`

<!-- toc levels=2 bold=false -->
- [:red_circle: E-001 >extension< not available](#red_circle-e-001-extension-not-available)
- [:red_circle: E-002 Aspect >aspect< not supported](#red_circle-e-002-aspect-aspect-not-supported)
- [:red_circle: E-003 Duplicate aspect configuration](#red_circle-e-003-duplicate-aspect-configuration)
- [:red_circle: E-004 Duplicate extension configuration](#red_circle-e-004-duplicate-extension-configuration)
- [:red_circle: E-005 Duplicate context path/webroot configuration](#red_circle-e-005-duplicate-context-pathwebroot-configuration)
- [:red_circle: E-006 contextPath <path> must start with /](#red_circle-e-006-contextpath-path-must-start-with-)
- [:red_circle: E-007 Webapps not allowed for aspect admin](#red_circle-e-007-webapps-not-allowed-for-aspect-admin)
- [:red_circle: E-008 Persona >persona< not supported](#red_circle-e-008-persona-persona-not-supported)
- [:red_circle: E-009 Location >path< is invalid/absolute/...](#red_circle-e-009-location-path-is-invalidabsolute)
- [:red_circle: E-010 >file< is not a valid Java properties file](#red_circle-e-010-file-is-not-a-valid-java-properties-file)
- [:red_circle: E-011 >file< is not a valid extensions.xml file](#red_circle-e-011-file-is-not-a-valid-extensionsxml-file)
- [:red_circle: E-012 extension.dir is not supported](#red_circle-e-012-extensiondir-is-not-supported)
- [:red_circle: E-013 Solr customization folder structure](#red_circle-e-013-solr-customization-folder-structure)
- [:red_circle: E-014 Version >version< does not support Cloud Extension Pack](#red_circle-e-014-version-version-does-not-support-cloud-extension-pack)
- [:red_circle: E-015 Patch release not allowed with Cloud Extension Pack](#red_circle-e-015-patch-release-not-allowed-with-cloud-extension-pack)
- [:red_circle: E-016 Invalid media conversion configuration](#red_circle-e-016-invalid-media-conversion-configuration)
- [:red_circle: E-017 Webroot configured in properties](#red_circle-e-017-webroot-configured-in-properties)
- [:red_circle: E-018 Invalid Solr Version](#red_circle-e-018-invalid-solr-version)
- [:red_circle: E-019 Invalid Integration Extension Pack Version](#red_circle-e-019-invalid-integration-extension-pack-version)
- [:warning: W-001 Property >property< is a managed property](#warning-w-001-property-property-is-a-managed-property)
- [:warning: W-002 Property file encoding](#warning-w-002-property-file-encoding)
- [:warning: W-003 Cloud Hot Folder without configuration for processing nodes](#warning-w-003-cloud-hot-folder-without-configuration-for-processing-nodes)
- [:warning: W-004 Solr customization without pinned Solr version](#warning-w-004-solr-customization-without-pinned-solr-version)
<!-- /toc -->


## <a id="e001"></a>:red_circle: E-001 `>extension<` not available

All extensions (remember, addons are also extensions) referenced in the `manifest.json` 
must be part of the extensions loaded by the platform during build or server startup.

The error indicates that the extension is not loaded by the platform.

- Double-check the name of the extension/addon
- Make sure that the extension is enabled in `manifest.json`
  - Is it configured in extensions.xml file referenced via `useConfig.extensions.location`?
  - Is it listed in the `extensions` array?
- Or add the extensions as a dependency to another extensions that is already configured

### Related Documentation

- [Extensions](https://help.sap.com/viewer/1be46286b36a4aa48205be5a96240672/latest/en-US/aa4777ef30f845008c64dae7218ac82d.html)
- [SAP Commerce Cloud Configuration Reuse][reuse]
- [Extension Dependencies](https://help.sap.com/viewer/20125f0eca6340dba918bda360e3cdfa/latest/en-US/8bbf3a9d86691014aa4189bf3ac0eb88.html)

## <a id="e002"></a>:red_circle: E-002 Aspect `>aspect<` not supported

Only a predefined set of aspects is supported in CCv2.

### Related Documentation

- [Aspects][aspects]

## <a id="e003"></a>:red_circle: E-003 Duplicate aspect configuration

Configuring the same aspect multiple times leads to undefined behaviour during build and deploy.

### Related Documentation

- [Aspects][aspects]

## <a id="e004"></a>:red_circle: E-004 Duplicate extension configuration

Configuring the same extension more than once for the same aspect leads to undefined behaviour during
build and deploy.

### Related Documentation

- [Aspects][aspects]

## <a id="e005"></a>:red_circle: E-005 Duplicate context path/webroot configuration

Using the same `contextPath`/webroot more than once in the same aspect breaks the server startup.

## <a id="e006"></a>:red_circle: E-006 contextPath `<path>` must start with `/`

If the `contextPath` doesn't start with `/`, the build will fail (since the server wouldn't start with this configuration).

## <a id="e007"></a>:red_circle: E-007 Webapps not allowed for aspect `admin`

CCv2 uses the `admin` aspect to perform system updates and starts it in "headless" mode.
Configuring webapps for it is not supported and may break the deployment process.

### Related Documentation

- [Aspects][aspects]

## <a id="e008"></a>:red_circle: E-008 Persona `>persona<` not supported

Only a predefined set of personas (= environment types) is supported in CCv2

### Relevant Documentation

- [Properties](https://help.sap.com/viewer/1be46286b36a4aa48205be5a96240672/latest/en-US/d6794b766aea4783b829988dc587f978.html)
- [Detailed Information](https://help.sap.com/viewer/1be46286b36a4aa48205be5a96240672/latest/en-US/e6e07ca6171a4081b733c46fce22e8ee.html)

## <a id="e009"></a>:red_circle: E-009 Location `>path<` is invalid/absolute/...

Files and folders referenced in `manifest.json` must be:

- Not empty (you have to define the `location` attribute!)
- Unix-style paths (i.e. use `/` as separator)
- relative to the folder that contains `manifest.json`
- in US-ASCII (using special characters may lead to undefined behaviour)
- plain values (no relative paths, no environment variable substitution etc.)

Bad:

:x: `/some/path` - absolute path\
:x: `path/../other/path` - relative path\
:x: `$HYBRIS_BIN_DIR/value` - variable substitution\
:x: `folder/file.pröperties` - special character\
:x: `path\to\file.properties` - wrong path separator\

Good:

:white_check_mark: `relative/path/to/something`

### Relevant Documentation

- [General Rules for Paths Declared in a Manifest](https://help.sap.com/viewer/1be46286b36a4aa48205be5a96240672/latest/en-US/6b40de5762694346bf3b1e9494b2256b.html)
- [Location of Resources in the Code Repository](https://help.sap.com/viewer/1be46286b36a4aa48205be5a96240672/latest/en-US/611a2ffa372348c1a39447ad927b2035.html)

## <a id="e010"></a>:red_circle: E-010 `>file<` is not a valid Java properties file

Files referenced in the `useConfig.properties` array of `manifest.json` must be readable as Java
properties files.

### Relevant Documentation

- [SAP Commerce Cloud Configuration Reuse][reuse]
- [java.util.Properties](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/Properties.html)

## <a id="011"></a>:red_circle: E-011 `>file<` is not a valid extensions.xml file

The file referenced in `useConfig.extensions.locations` must be a valid extensions.xml file.

### Relevant Documentation

- [SAP Commerce Cloud Configuration Reuse][reuse]
- [Configuration Options in localextensions.xml](https://help.sap.com/viewer/b490bb4e85bc42a7aa09d513d0bcb18e/latest/en-US/cce26d8ef435425fb9e054d91794148c.html)

## <a id="012"></a>:red_circle: E-012 `extension.dir` is not supported

The cloud build only supports the `name` attribute of the `<extension>` tags in the
`useConfig.extensions.locations` file.

### Relevant Documentation

- [SAP Commerce Cloud Configuration Reuse][reuse]
- [Configuration Options in localextensions.xml](https://help.sap.com/viewer/b490bb4e85bc42a7aa09d513d0bcb18e/latest/en-US/cce26d8ef435425fb9e054d91794148c.html)

## <a id="e013"></a>:red_circle: E-013 Solr customization folder structure

The Solr customization support for CCv2 expects a specific folder structure to be present.

If the folder structure is incorrect, the customization simply doesn't work.

### Relevant Documentation

- [Customizing Solr](https://help.sap.com/viewer/b2f400d4c0414461a4bb7e115dccd779/latest/en-US/f7251d5a1d6848489b1ce7ba46300fe6.html)

## <a id="e014"></a>:red_circle: E-014 Version `>version<` does not support Cloud Extension Pack

The Cloud Extension Pack is only available for versions 1811 to 1905.

### Relevant Documentation 

- [Manifest Components Reference (v1905)](https://help.sap.com/viewer/1be46286b36a4aa48205be5a96240672/v1905/en-US/3d562b85b37a460a92d32ec991459133.html)

## <a id="e015"></a>:red_circle: E-015 Patch release not allowed with Cloud Extension Pack

When using the Cloud Extension Pack, you cannot configure a specific patch release

### Relevant Documentation

- [Manifest Components Reference (v1905)](https://help.sap.com/viewer/1be46286b36a4aa48205be5a96240672/v1905/en-US/3d562b85b37a460a92d32ec991459133.html)

## <a id="e016"></a>:red_circle: E-016 Invalid media conversion configuration

To use the cloud media conversion feature, you have to configure the `cloudmediaconversion` extension *and* enable the image processing
service (`enableImageProcessingService`)

### Relevant Documentation

- [Enabling Media Conversion](https://help.sap.com/viewer/403d43bf9c564f5a985913d1fbfbf8d7/latest/en-US/fba094343e624aae8f041d0170046355.html)

## <a id="e017"></a>:red_circle: E-017 Webroot configured in properties

Do not configure `<extension>.webroot` in any properties. Only use `asepect[].webapps[].contextPath` 
to configure and enable web extensions.

### Relevant Documentation

- [Manifest Components Reference](https://help.sap.com/viewer/1be46286b36a4aa48205be5a96240672/latest/en-US/3d562b85b37a460a92d32ec991459133.html)

## <a id="e018"></a>:red_circle: E-018 Invalid Solr Version

The Solr version in the manifest must be two numbers separated by a dot (`<major>.<minor>`).

### Relevant Documentation

- [Manifest Components Reference](https://help.sap.com/viewer/1be46286b36a4aa48205be5a96240672/latest/en-US/3d562b85b37a460a92d32ec991459133.html)

## <a id="e019"></a>:red_circle: E-019 Invalid Integration Extension Pack Version

The Integration Extension Pack must be compatible with SAP Commerce and its version must be fully 
qualified, i.e. include the patch number.

:x: `2102` - missing patch\
:white_check_mark: `2102.0`

### Relevant Documentation

- [Integrations Compatibility Matrix](https://help.sap.com/viewer/bad9b0b66bac476f8a4a5c4a08e4ab6b/LATEST/en-US/f7e859a4b880476aa37e376b5423188c.html)


## <a id="w001"></a>:warning: W-001 Property `>property<` is a managed property

The build process preconfigures certain properties with optimal values for a high-performance cloud 
environments.

Changing their values may have unintended side effects (like breaking the server) and should
only be done if recommended by SAP experts or SAP support.

### Relevant Documentation

- [Managed Properties](https://help.sap.com/viewer/1be46286b36a4aa48205be5a96240672/latest/en-US/a30160b786b545959184898b51c737fa.html)

## <a id="w002"></a>:warning: W-002 Property file encoding

Property files are assumed to be encoded in ISO 8859-1. Using a different encoding might lead
to changed/corrupted values after build and deploy.

### Relevant Documentation

- [java.util.Properties](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/Properties.html)

## <a id="w003"></a>:warning: W-003 Cloud Hot Folder without configuration for processing nodes

If you use the Cloud Hot Fodlers feature of the Commerce cloud, you also have to configure the 
`backgroundProcessing` nodes to process the incoming files.

### Relevant Documentation

- [Enabling and Configuring Hot Folders](https://help.sap.com/viewer/403d43bf9c564f5a985913d1fbfbf8d7/latest/en-US/6e23a26fe9c8472380f9101e8a9fe1c3.html)

## <a id="w004"></a>:warning: W-004 Solr customization without pinned Solr version

If you customize your Solr configuration, it is recommended that you pin the Solr version in your `manifest.json`.

A SAP Commerce patch release may change the default Solr version, leading to deployment errors if your customization is not
compatible with the new Solr version.

### Relevant Documentation

- [Solr Server Version Selection](https://help.sap.com/viewer/1be46286b36a4aa48205be5a96240672/latest/en-US/b35bc14a62aa4950bdba451a5f40fc61.html#loiod7294323e5e542b7b37f48dd83565321)

[aspects]: https://help.sap.com/viewer/1be46286b36a4aa48205be5a96240672/latest/en-US/8f494fb9617346188ddf21a971db84fc.htm
[reuse]: https://help.sap.com/viewer/1be46286b36a4aa48205be5a96240672/latest/en-US/2311d89eef9344fc81ef168ac9668307.html
