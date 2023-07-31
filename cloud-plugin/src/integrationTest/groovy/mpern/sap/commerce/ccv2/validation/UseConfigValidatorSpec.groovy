package mpern.sap.commerce.ccv2.validation

import java.nio.file.Files
import java.nio.file.Path

import groovy.json.JsonSlurper
import spock.lang.Specification
import spock.lang.TempDir

import mpern.sap.commerce.ccv2.model.Manifest
import mpern.sap.commerce.ccv2.validation.impl.UseConfigValidator

class UseConfigValidatorSpec extends Specification {

    @TempDir
    Path testProjectDir

    UseConfigValidator validator

    def setup() {
        validator = new UseConfigValidator(testProjectDir)
    }

    def "locations are validated"() {
        given:
        def rawManifest = new JsonSlurper().parseText('''\
        {
          "commerceSuiteVersion": "2005",
          "useConfig": {
            "properties": [
              {
                "location": "/hybris/config/environments/local-dev.properties",
                "persona": "development"
              },
              {
                "location": "../hybris/config/environments/local-stage.properties",
                "persona": "staging"
              },
              {
                "location": "./hybris/config/environments/local-prod.properties",
                "persona": "production"
              },
              {
                "location": "hybris/config/environments/common.properties"
              },
              {
                "location": "hybriß/cönfig/environments/common.properties"
              },
              {
                "key": "invalid",
                "value": "entry"
              }
            ]
          }
        }
        
        ''') as Map<String, Object>
        def manifest = Manifest.fromMap(rawManifest)

        when:
        def errors = validator.validate(manifest)

        then:
        errors.size() == 6
        errors.any{ it.location == "useConfig.properties[0]" && it.message.contains("absolute") }
        errors.any{ it.location == "useConfig.properties[1]" && it.message.contains("relative") }
        errors.any{ it.location == "useConfig.properties[2]" && it.message.contains("relative") }
        errors.any{ it.location == "useConfig.properties[3]" && it.message.contains("not found") }
        errors.any{ it.location == "useConfig.properties[4]" && it.message.contains("invalid") }
        errors.any{ it.location == "useConfig.properties[5]" && it.message.contains("invalid") }
    }

    def "localextensions.xml must be a valid localextensions.xml file"() {
        given:
        def rawManifest = new JsonSlurper().parseText('''\
        {
          "commerceSuiteVersion": "2005",
          "useConfig": {
            "extensions": {
              "location": "localextensions.xml"
            }
          }
        }
        ''') as Map<String, Object>
        def manifest = Manifest.fromMap(rawManifest)

        testProjectDir.resolve("localextensions.xml").text = """\
        <something><invalid></invalid></something>
        """.stripIndent()

        when:
        def errors = validator.validate(manifest)

        then:
        errors.size() == 1
        errors.any{ it.location == 'useConfig.extensions.location' && it.message.contains("valid")}
    }

    def "localextensions.xml must only contain extension names"() {
        given:
        def rawManifest = new JsonSlurper().parseText('''
        {
          "commerceSuiteVersion": "2005",
          "useConfig": {
            "extensions": {
              "location": "localextensions.xml"
            }
          }
        }
        ''') as Map<String, Object>
        Manifest manifest = Manifest.fromMap(rawManifest)

        testProjectDir.resolve("localextensions.xml").text = '''\
        <?xml version="1.0" encoding="UTF-8" standalone="no"?>
        <hybrisconfig xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
              xsi:noNamespaceSchemaLocation="resources/schemas/extensions.xsd">
            <extensions>
                <path dir="${HYBRIS_BIN_DIR}"/>
                <extension name="foo" dir="bar" />
            </extensions>
        </hybrisconfig>
        '''.stripIndent()

        when:
        def errors = validator.validate(manifest)

        then:
        errors.size() == 1
        errors.any{ it.location == 'useConfig.extensions.location' && it.message.contains("`extension.dir`")}
    }

    def "properties must use valid personas and aspects"() {
        given:
        def rawManifest = new JsonSlurper().parseText('''\
        {
          "commerceSuiteVersion": "2005",
          "useConfig": {
            "properties": [
              {
                "location": "dummy.properties",
                "persona": "development"
              },
              {
                "location": "dummy.properties",
                "persona": "invalid"
              },
              {
                "location": "dummy.properties",
                "aspect": "backoffice"
              },
              {
                "location": "dummy.properties",
                "aspect": "invalid"
              }
            ]
          }
        }
        
        ''') as Map<String, Object>
        Manifest manifest = Manifest.fromMap(rawManifest)
        Files.createFile(testProjectDir.resolve("dummy.properties"))

        when:
        def errors = validator.validate(manifest)

        then:
        errors.size() == 2
        errors.any{ it.location == "useConfig.properties[1]" && it.message.contains('`invalid`')}
        errors.any{ it.location == "useConfig.properties[3]" && it.message.contains('`invalid`')}
    }

    def "properties must by a valid Java properties file"() {
        given:
        def rawManifest = new JsonSlurper().parseText('''\
        {
          "commerceSuiteVersion": "2005",
          "useConfig": {
            "properties": [
              {
                "location": "non-latin1.properties",
              },
              {
                "location": "latin1.properties"
              }
            ]
          }
        }
        ''') as Map<String, Object>
        Manifest manifest = Manifest.fromMap(rawManifest)

        testProjectDir.resolve("non-latin1.properties").text = '''\
        non.latin1=fööbaß$\\{}
        foo=bar
        '''.stripIndent()
        testProjectDir.resolve("latin1.properties").text = '''\
        latin1=foobar$\\{}
        foo=bar
        '''.stripIndent()

        when:
        def errors = validator.validate(manifest)

        then:
        errors.size() == 1
        errors.any{ it.level == Level.WARNING && it.location == "useConfig.properties[0]" && it.message.contains('charset')}
    }

    def "solr customization must have required folder structure"() {
        given:
        def rawManifest = new JsonSlurper().parseText('''
        {
          "commerceSuiteVersion": "2011",
          "useConfig": {
            "solr": {
              "location": "solr"
            }
          }
        }
        ''') as Map<String, Object>
        Manifest manifestWithoutVersion = Manifest.fromMap(rawManifest)
        Files.createDirectory(testProjectDir.resolve("solr"))
        rawManifest = new JsonSlurper().parseText('''
        {
          "commerceSuiteVersion": "2011",
          "solrVersion": "8.6",
          "useConfig": {
            "solr": {
              "location": "solr"
            }
          }
        }
        ''') as Map<String, Object>
        Manifest manifestWithVersion = Manifest.fromMap(rawManifest)

        when:
        def missingFolder = validator.validate(manifestWithoutVersion)
        Files.createDirectories(testProjectDir.resolve("solr/server/solr/configsets/default/conf"))
        def folderExists = validator.validate(manifestWithVersion)

        then:
        missingFolder.size() == 2
        missingFolder.any{ it.location == "useConfig.solr.location" && it.message.contains("server/solr/configsets/default/conf")}
        missingFolder.any{ it.location == "solrVersion" && it.level == Level.WARNING}
        folderExists.isEmpty()
    }
}
