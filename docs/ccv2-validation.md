# CCv2 Validation Errors

Here you can detail information for the errors and warnings reported by `validateManifest`

## <a id="e001"></a>E-001 `<extension>` not available

All extensions (remember, addons are also extensions) referenced in the `manifest.json` 
must be part of the extensions loaded by the platform.

The error indicates that the extension does not exist.

- Double-check its name
- make sure that  you enable it in the `manifest.json` using the `extensions` array 
  or in the file referenced via `useConfig.extensions.location`.
- or configure it as a dependency of another extensions

### Related Documentation

- [Extensions](https://help.sap.com/viewer/1be46286b36a4aa48205be5a96240672/latest/en-US/aa4777ef30f845008c64dae7218ac82d.html)
- [SAP Commerce Cloud Configuration Reuse](https://help.sap.com/viewer/1be46286b36a4aa48205be5a96240672/latest/en-US/2311d89eef9344fc81ef168ac9668307.html)
- [Extension Dependencies](https://help.sap.com/viewer/20125f0eca6340dba918bda360e3cdfa/latest/en-US/8bbf3a9d86691014aa4189bf3ac0eb88.html)

## <a id="e002"></a>E-002 Aspect `<aspect>` not supported

Only a predefined set of aspects is supported in CCv2.

### Related Documentation

- [Aspects][aspects]

## <a id="e003"></a>E-003 Duplicate aspect configuration

Configuring the same aspect multiple times leads to undefined behaviour during build and deploy.

### Related Documentation

- [Aspects][aspects]

## <a id="e004"></a>E-004 Duplicate extension configuration

Configuring the same extension more than once for the same aspect leads to undefined behaviour during
build and deploy.

### Related Documentation

- [Aspects][aspects]

## <a id="e005"></a>E-005 Duplicate context path/webroot configuration

Using the same `contextPath`/webroot more than once in the same aspect breaks the server startup.

## <a id="e006"></a>E-006 contextPath `<path>` must start with `/`

If the contextPath doesn't start with `/`, the server doesn't start

## <a id="e007"></a>E-007 Webapps not allowed for aspect `admin`

CCv2 uses the `admin` aspect to perform system updates and starts it in "headless" mode.
Configuring webapps for it is not supported and may break the deployment process.

### Related Documentation

- [Aspects][aspects]

## <a id="e008"></a>E-008 Persona `<persona>` not supported

Only a predefined set of personas (= environment types) is supported in CCv2

### Relevant Documentation

- [Properties](https://help.sap.com/viewer/1be46286b36a4aa48205be5a96240672/latest/en-US/d6794b766aea4783b829988dc587f978.html)
- [Detailed Information](https://help.sap.com/viewer/1be46286b36a4aa48205be5a96240672/latest/en-US/e6e07ca6171a4081b733c46fce22e8ee.html)

## <a id="e009"></a>E-009 Location `<path>` is invalid/absolute/...

Files and folders referenced in `manifest.json` must be:

- relative to the folder that contains `manifest.json`
- plain values (no relative paths, no environment variable substitution etc.)
- Unix-style paths (using `/` as separator)
- in US-ASCII (using special characters may lead to undefined behaviour)

Bad:

- :x: `/some/path` - absolut path
- :x: `path/../other/path` - relative path
- :x: `$HYBRIS_BIN_DIR/value` - variable substitution
- :x: `folder/file.pröperties` - special character
- :x: `path\to\file.properties` - wrong path separator

Good:

- :white_check_mark: `relative/path/to/something`

### Relevant Documentation

- [General Rules for Paths Declared in a Manifest](https://help.sap.com/viewer/1be46286b36a4aa48205be5a96240672/latest/en-US/6b40de5762694346bf3b1e9494b2256b.html)
- [Location of Resources in the Code Repository](https://help.sap.com/viewer/1be46286b36a4aa48205be5a96240672/latest/en-US/611a2ffa372348c1a39447ad927b2035.html)

## <a id="e010"></a>E-010 `<file>` is not a valid Java properties file

Files referenced in the `useConfig.properties` array of `manifest.json` must be readable as Java
properties files.

### Relevant Documentation

- [SAP Commerce Cloud Configuration Reuse][reuse]
- [java.util.Properties](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/Properties.html)

## <a id="011"></a>E-011 `<file>` is not a valid extensions.xml file

The file referenced in `useConfig.extensions.locations` must be a valid extensions.xml file.

### Relevant Documentation

- [SAP Commerce Cloud Configuration Reuse][reuse]

## <a id="012"></a>E-012 `extension.dir` is not supported

The cloud build only uses the `name` attribute of the `<extension>` tags in the
`useConfig.extensions.locations` file.

### Relevant Documentation

- [SAP Commerce Cloud Configuration Reuse][reuse]

## <a id="e013"></a>E-013 Solr customization folder structure

The Solr customization support for CCv2 expects a specific folder structure to be present.

If the folder structure is incorrect, the customization simply doesn't work.

### Relevant Documentation

- [Customizing Solr](https://help.sap.com/viewer/b2f400d4c0414461a4bb7e115dccd779/latest/en-US/f7251d5a1d6848489b1ce7ba46300fe6.html)

## <a id="e014"></a>E-014 Version `<version>` does not support Cloud Extension Pack

The Cloud Extension Pack is only available for versions 1811 to 1905.

### Relevant Documentation 

- [Manifest Components Reference (v1905)](https://help.sap.com/viewer/1be46286b36a4aa48205be5a96240672/v1905/en-US/3d562b85b37a460a92d32ec991459133.html)

## <a id="e015"></a>E-015 Patch release not allowed with Cloud Extension Pack

When using the Cloud Extension Pack, you cannot configure a specific patch release

### Relevant Documentation

- [Manifest Components Reference (v1905)](https://help.sap.com/viewer/1be46286b36a4aa48205be5a96240672/v1905/en-US/3d562b85b37a460a92d32ec991459133.html)

## <a id="w001"></a>W-001 Property `<property>` is a managed property

The build process preconfigures certain properties with optimal values for a high-performance cloud 
environments.

Changing their values may have unintended side effects and should only be done if recommended by SAP
experts or SAP support.

### Relevant Documentation

- [Managed Properties](https://help.sap.com/viewer/1be46286b36a4aa48205be5a96240672/latest/en-US/a30160b786b545959184898b51c737fa.html)

## <a id="w002"></a>W-002 Property file encoding

Property files are assumed to be encoded in ISO 8859-1. Using a different encoding might lead
to changed/corrupted values after build and deploy.

### Relevant Documentation

- [java.util.Properties](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/Properties.html)

## <a id="w003"></a>W-003 Cloud Hot Folder without configuration for processing nodes

If you use the Cloud Hot Fodlers feature of the Commerce cloud, you also have to configure the 
`backgroundProcessing` nodes to process the incoming files.

### Relevant Documentation

- [Enabling and Configuring Hot Folders](https://help.sap.com/viewer/403d43bf9c564f5a985913d1fbfbf8d7/latest/en-US/6e23a26fe9c8472380f9101e8a9fe1c3.html)

[aspects]: https://help.sap.com/viewer/1be46286b36a4aa48205be5a96240672/latest/en-US/8f494fb9617346188ddf21a971db84fc.htm
[reuse]: https://help.sap.com/viewer/1be46286b36a4aa48205be5a96240672/latest/en-US/2311d89eef9344fc81ef168ac9668307.html
