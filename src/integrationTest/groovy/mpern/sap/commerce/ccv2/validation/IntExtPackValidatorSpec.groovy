package mpern.sap.commerce.ccv2.validation

import groovy.json.JsonSlurper
import spock.lang.Specification

import mpern.sap.commerce.ccv2.model.Manifest
import mpern.sap.commerce.ccv2.validation.impl.IntExtPackValidator

class IntExtPackValidatorSpec extends Specification {

    def "Int Ext Pack validation recognizes correct pack"(String commerce, String pack) {
        given:
        def rawManifest = new JsonSlurper().parseText("""
        {
          "commerceSuiteVersion": "${commerce}",
          "extensionPacks" : [
            {
              "name" : "hybris-commerce-integrations",
              "version" : "${pack}"
            }
          ]
        }
        """) as Map<String, Object>
        Manifest manifest = Manifest.fromMap(rawManifest)
        def validator = new IntExtPackValidator()

        when:
        def result = validator.validate(manifest)

        then:
        result.isEmpty()

        where:
        commerce | pack
        "2005"   | "2005.2"
        "2011"   | "2102.0"
        "2105"   | "2108.0"
        "2205"   | "2205.0"
        "2211"   | "2211.0"
    }

    def "Int Ext Pack validation recognizes invalid combinations"(String commerce, String pack, String message) {
        given:
        def rawManifest = new JsonSlurper().parseText("""
        {
          "commerceSuiteVersion": "${commerce}",
          "extensionPacks" : [
            {
              "name" : "hybris-commerce-integrations",
              "version" : "${pack}"
            }
          ]
        }
        """) as Map<String, Object>
        Manifest manifest = Manifest.fromMap(rawManifest)
        def validator = new IntExtPackValidator()

        when:
        def result = validator.validate(manifest)

        then:
        result.size() == 1
        result.any{ it.level == Level.ERROR && it.message.toLowerCase().contains(message)}

        where:
        commerce | pack     | message
        "2005"   | "2015.2" | "not compatible"
        "1811"   | "2005.2" | "available"
        "2105"   | "2108"   | "qualified"
        "2211"   | "2211"   | "qualified"
        "2211"   | "2108.1" | "not compatible"
    }
}
