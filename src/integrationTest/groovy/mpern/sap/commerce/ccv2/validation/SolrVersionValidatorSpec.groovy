package mpern.sap.commerce.ccv2.validation

import groovy.json.JsonSlurper
import spock.lang.Specification

import mpern.sap.commerce.ccv2.model.Manifest
import mpern.sap.commerce.ccv2.validation.impl.SolrVersionValidator

class SolrVersionValidatorSpec extends Specification {
    def "solrVersion version must a valid Solr major.minor version"() {
        given:
        def rawManifest = new JsonSlurper().parseText('''
        {
          "commerceSuiteVersion": "2011",
          "solrVersion": "8.6.3"
        }
        ''') as Map<String, Object>
        Manifest manifest = Manifest.fromMap(rawManifest)
        def validator = new SolrVersionValidator();

        when:
        def faultySolr = validator.validate(manifest)

        then:
        faultySolr.size() == 1
        faultySolr.any{ it.location == "solrVersion" && it.level == Level.ERROR }
    }
}
