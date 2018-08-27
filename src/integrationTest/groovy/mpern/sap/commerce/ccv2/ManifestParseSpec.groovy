package mpern.sap.commerce.ccv2

import groovy.json.JsonSlurper
import mpern.sap.commerce.ccv2.model.Manifest
import spock.lang.Specification

class ManifestParseSpec extends Specification {

    def "parse the sample manifest"() {
        given:
        Map<String, Object> rawManifest = new JsonSlurper().parse(this.getClass().getResource('/test-manifest.json'))

        when:
        Manifest m = Manifest.fromMap(rawManifest);

        then:
        m.commerceSuiteVersion == "6.7.0.1"
        m.extensions == [
                "modeltacceleratorservices",
                "electronicsstore",
                "privacyoverlayeraddon",
                "yacceleratorstorefront",
                "backoffice"
        ] as Set<String>

        m.properties.get(0).key == "test.property.1"
        m.properties.get(0).value == "test.property.1.value"
        m.properties.get(0).persona == "production"
        m.properties.get(1).key == "test.property.2"
        m.properties.get(1).value == "test.property.2.value"
        m.properties.get(1).persona == "development"
        m.properties.get(2).key == "test.property.2"
        m.properties.get(2).value == "test.property.2.value.in.prod.only"
        m.properties.get(2).persona == "production"

        m.storefrontAddons.get(0).addon == "privacyoverlayeraddon"
        m.storefrontAddons.get(0).storefront == "albinostorefront"
        m.storefrontAddons.get(0).template == "yacceleratorstorefront"
        m.storefrontAddons.get(1).addon == "albinoaddon"
        m.storefrontAddons.get(1).storefront == "albinostorefront"
        m.storefrontAddons.get(1).template == "yacceleratorstorefront"

        m.aspects.get(0).name == "backoffice"
        m.aspects.get(0).properties.get(0).key == "test.property.1"
        m.aspects.get(0).properties.get(0).value == "test.property-1-value-prod-backoffice"
        m.aspects.get(0).properties.get(0).persona == "production"
        m.aspects.get(0).properties.get(1).key == "test.property.2"
        m.aspects.get(0).properties.get(1).value == "test.property-2-value-backoffice"
        m.aspects.get(0).properties.get(1).persona == ""
        m.aspects.get(0).webapps.get(0).name == "hac"
        m.aspects.get(0).webapps.get(0).contextPath == "/hac"
        m.aspects.get(0).webapps.get(1).name == "mediaweb"
        m.aspects.get(0).webapps.get(1).contextPath == "/medias"
        m.aspects.get(0).webapps.get(2).name == "backoffice"
        m.aspects.get(0).webapps.get(2).contextPath == ""

        m.aspects.get(1).name == "accstorefront"
        m.aspects.get(1).properties.get(0).key == "spring.session.enabled"
        m.aspects.get(1).properties.get(0).value == "true"
        m.aspects.get(1).properties.get(0).persona == ""
        m.aspects.get(1).properties.get(1).key == "spring.session.yacceleratorstorefront.save"
        m.aspects.get(1).properties.get(1).value == "async"
        m.aspects.get(1).properties.get(1).persona == ""
        m.aspects.get(1).properties.get(2).key == "spring.session.yacceleratorstorefront.cookie.name"
        m.aspects.get(1).properties.get(2).value == "JSESSIONID"
        m.aspects.get(1).properties.get(2).persona == ""
        m.aspects.get(1).properties.get(3).key == "spring.session.yacceleratorstorefront.cookie.path"
        m.aspects.get(1).properties.get(3).value == "/"
        m.aspects.get(1).properties.get(3).persona == ""
        m.aspects.get(1).properties.get(4).key == "storefrontContextRoot"
        m.aspects.get(1).properties.get(4).value == ""
        m.aspects.get(1).properties.get(4).persona == ""
        m.aspects.get(1).webapps.get(0).name == "mediaweb"
        m.aspects.get(1).webapps.get(0).contextPath == "/medias"
        m.aspects.get(1).webapps.get(1).name == "albinostorefront"
        m.aspects.get(1).webapps.get(1).contextPath == ""
        m.aspects.get(1).webapps.get(2).name == "acceleratorservices"
        m.aspects.get(1).webapps.get(2).contextPath == "/acceleratorservices"

        m.aspects.get(2).name == "backgroundProcessing"
        m.aspects.get(2).properties.isEmpty()
        m.aspects.get(2).webapps.get(0).name == "hac"
        m.aspects.get(2).webapps.get(0).contextPath == ""
        m.aspects.get(2).webapps.get(1).name == "mediaweb"
        m.aspects.get(2).webapps.get(1).contextPath == "/medias"

        m.tests.extensions == [
                "privacyoverlayeraddon",
                "yacceleratorstorefront"
        ] as Set<String>
        m.tests.annotations == [
                "UnitTests",
                "IntegrationTests"
        ] as Set<String>
        m.tests.packages == [
                "de.hybris.infra.*"
        ] as Set<String>

        m.webTests.extensions == [
                "yacceleratorstorefront"
        ] as Set<String>
        m.webTests.excludedPackages == [
                "de.hybris.platform.*"
        ] as Set<String>
    }
}
