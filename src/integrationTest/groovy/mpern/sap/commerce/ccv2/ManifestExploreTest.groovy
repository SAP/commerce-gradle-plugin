package mpern.sap.commerce.ccv2

import groovy.json.JsonSlurper
import spock.lang.Specification

class ManifestExploreTest extends Specification {

    def manifestFile

    def setup() {
        manifestFile = new File(this.getClass().getResource( '/test-manifest.json' ).getFile())
    }

    def "read json and get a bunch of stuff"() {

        when:
        def json = new JsonSlurper().parse(manifestFile)
        json = (Map) json

        then:
        json.get("commerceSuiteVersion") == "6.7.0.1"
        json.get("commasderceSuiteVersion") == null
        json.get("storefrontAddons") instanceof List
        ((Map)json.get("tests")).get("extensions") instanceof List
        def first = ((List) json.get("storefrontAddons")).get(0)
        first instanceof Map
        def firstAddon = (Map) first
        firstAddon.get("addon") == "privacyoverlayeraddon"
        firstAddon.get("storefront") == "albinostorefront"
        firstAddon.get("template") == "yacceleratorstorefront"


    }
}
