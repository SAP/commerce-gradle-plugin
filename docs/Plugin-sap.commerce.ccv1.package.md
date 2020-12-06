# Plugin `sap.commerce.ccv1.package`

The plugin builds hybris cloud services compliant packages and uses a convention-over-configuration directory layout to
keep the config files as required by the CCV1 packaging guidelines.

## Configuration

The plugin uses following DSL with these defaults:

```groovy
CCV1 {
    //customerID and projectID define the filename of the package
    customerID = ""
    projectID = project.name
    
    //do you have datahub in your infrastructure? (relevant for the package metadata.properties)
    datahub = false
    //environment names as defined by CCV1 guidelines.
    environments = ['dev', 'stag', 'prod']
    //Whats your pre-prodiction environment? (relevant for the package metadata.properties)
    preProductionEnvironment = 'stag'
    
    //datahub.war file to use in the package.
    // by default, datahub is *not* included
    datahubWar = null
    
    //the project binaries to use. these are the default folders for ant production
    platformZip = file("hybris/temp/hybris/hybrisServer/hybrisServer-Platform.zip")
    allExtensionsZip = file("hybris/temp/hybris/hybrisServer/hybrisServer-AllExtensions.zip")
    
    // where are the configuration files per environment stored?
    configurationFolder = file("ccv1-configuration")
    // the final package is created here
    distributionFolder = file("dist")
    // temp folder used to build the package
    tempFolder = file("temp")

    //Include solr customization folders into the final package?
    solr = false
}
```

The directory layout for the hybris cloud services environment configuration looks like this:

```
ccv1-configuration/
├── common
│   ├── datahub
│   │   └── customer.properties
│   ├── hybris
│   │   ├── customer.app.properties
│   │   ├── customer.properties
│   │   └── localextensions.xml
│   └── solr
│       └── ...
├── <environment>
│   ├── datahub
│   │   └── customer.properties
│   ├── hybris
│   │   ├── customer.app.properties
│   │   └── customer.properties
│   └── solr
│       └── ...
```

## Tasks

The plugin defines the following tasks

### `bootstrapCCV1Config`

Creates empty environment config folders, if they don't exist.

It also checks if you use datahub (`CCV1.datahub = true`) and provides config folders for the datahub too.

### `buildCCV1Package`

This is where the magic happens.

This task builds a valid CCV1 deployment package (following the [Deployment Packaging Guidelines][guide])

[guide]: https://help.sap.com/viewer/73ab63e258cc488ab38957de9eb63580/SHIP/en-US/2a8454bf66a3440daf66e980fd7d7a62.html

The `metadata.properties` are created based on the information provided in the DSL.

`CCV1.platformZip` and `CCV1.allExtensionsZip` are included into the package

If the datahub is required (`CCV1.datahub = true`) the plugin includes `CCV1.datahubWar` 
into the package, it also makes sure to use the correct file name according to the packaging guidelines.

The environment specific configuration is built with the files and folders in `ccv1-configuration`
(you may change the location by using `CCV1.configurationFolder` in the DSL)

To prevent duplication of files and configurations across the environments, the 
final configuration for every environment (as defined in `CCV1.environments`) is built as follows:

1. Copy files from `common/hybris` folder into target configuration
1. Create `customer.adm.properties` and `customer.app.properties` by merging the environment-specific configuration with
   the configuration provided in the `common/hybris` folder. \
   The final properties file is merged in this order:
    1. `common/hybris/customer.properties`
    1. `common/hybris/customer.(adm|app).properties`
    1. `<environment>/hybris/customer.properties`
    1. `<environment>/hybris/customer.(adm|app).properties`

    If any of these files is not present, it is ignored
1. If you need datahub in the package (`CCV1.datahub = true`), do more or less the same, but use the folders
   `common/datahub` and `<environment>/datahub` and only generate `customer.properties`
1. If you need experimental solr customization (The folders marked as "future implementation" on 
   [this diagram][structure], `CCV1.solr = true`), the `common/solr` and `<environmet>/solr` folders are merged into the
   final package


**Hint:** Since the environments are defined in the DSL, you do not need to keep empty files / folders around, if there is
nothing specific for a given environment. The plugin will always generate a configuration folder for every *defined* environment.
This means that for some projects the `common` folder is enough, if there is nothing environment-specific to configure.

The package name is built using following pattern:

`${CCV1.customerID}_${CCV1.projectID}_v{project.version}.zip`

If the `CCV1.customerID` is empty (default), the plugin omits the `${CCV1.customerID}_` prefix.

The plugin builds the final package and the required `.md5` checksum file in `dist/`
(you may change that using the DSL property `CCV1.distributionFolder`)

#### `cleanTemp`

Cleans temp folder used to assemble the final package (`CCV1.temp` in the DSL, default `temp/`)

[structure]: https://help.sap.com/viewer/73ab63e258cc488ab38957de9eb63580/SHIP/en-US/691db27e7fee481aa62c563f1b7f721e.html#loio4265d89cf68c4491af68c5d21fc5066f__fig_qbc_qwx_p1b
