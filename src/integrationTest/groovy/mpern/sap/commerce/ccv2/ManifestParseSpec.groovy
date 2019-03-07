package mpern.sap.commerce.ccv2

import groovy.json.JsonSlurper
import mpern.sap.commerce.ccv2.model.Manifest
import mpern.sap.commerce.ccv2.model.TestConfiguration
import spock.lang.Specification

class ManifestParseSpec extends Specification {

    def "parse the sample manifest"() {
        given:
        Map<String, Object> rawManifest = new JsonSlurper().parse(this.getClass().getResource('/test-manifest.json'))

        when:
        Manifest m = Manifest.fromMap(rawManifest)

        then:
        with(m) {
            commerceSuiteVersion == "6.7.0.1"
            extensions == [
                    "modeltacceleratorservices",
                    "electronicsstore",
                    "privacyoverlayeraddon",
                    "yacceleratorstorefront",
                    "backoffice"
            ] as Set<String>

            with(properties.get(0)) {
                key == "test.property.1"
                value == "test.property.1.value"
                persona == "production"
            }
            with(properties.get(1)) {
                key == "test.property.2"
                value == "test.property.2.value"
                persona == "development"
            }
            with(properties.get(2)) {
                key == "test.property.2"
                value == "test.property.2.value.in.prod.only"
                persona == "production"
            }

            with(storefrontAddons.get(0)) {
                addon == "privacyoverlayeraddon"
                storefront == "albinostorefront"
                template == "yacceleratorstorefront"
            }
            with(storefrontAddons.get(1)) {
                addon == "albinoaddon"
                storefront == "albinostorefront"
                template == "yacceleratorstorefront"
            }

            with(aspects.get(0)) {
                name == "backoffice"
                with(properties.get(0)) {
                    key == "test.property.1"
                    value == "test.property-1-value-prod-backoffice"
                    persona == "production"
                }
                with(properties.get(1)) {
                    key == "test.property.2"
                    value == "test.property-2-value-backoffice"
                    persona == ""
                }
                with(webapps.get(0)) {
                    name == "hac"
                    contextPath == "/hac"
                }
                with(webapps.get(1)) {
                    name == "mediaweb"
                    contextPath == "/medias"
                }
                with(webapps.get(2)) {
                    name == "backoffice"
                    contextPath == ""
                }
            }

            with(aspects.get(1)) {
                name == "accstorefront"
                with(properties.get(0)) {
                    key == "spring.session.enabled"
                    value == "true"
                    persona == ""
                }
                with(properties.get(1)) {
                    key == "spring.session.yacceleratorstorefront.save"
                    value == "async"
                    persona == ""
                }
                with(properties.get(2)) {
                    key == "spring.session.yacceleratorstorefront.cookie.name"
                    value == "JSESSIONID"
                    persona == ""
                }
                with(properties.get(3)) {
                    key == "spring.session.yacceleratorstorefront.cookie.path"
                    value == "/"
                    persona == ""
                }
                with(properties.get(4)) {
                    key == "storefrontContextRoot"
                    value == ""
                    persona == ""
                }
                with(webapps.get(0)) {
                    name == "mediaweb"
                    contextPath == "/medias"
                }
                with(webapps.get(1)) {
                    name == "albinostorefront"
                    contextPath == ""
                }
                with(webapps.get(2)) {
                    name == "acceleratorservices"
                    contextPath == "/acceleratorservices"
                }
            }

            with(aspects.get(2)) {
                name == "backgroundProcessing"
                properties.isEmpty()
                with(webapps.get(0)) {
                    name == "hac"
                    contextPath == ""
                }
                with(webapps.get(1)) {
                    name == "mediaweb"
                    contextPath == "/medias"
                }
            }

            with(tests) {
                extensions == [
                        "privacyoverlayeraddon",
                        "yacceleratorstorefront"
                ] as Set<String>
                annotations == [
                        "UnitTests",
                        "IntegrationTests"
                ] as Set<String>
                packages == [
                        "de.hybris.infra.*"
                ] as Set<String>
            }

            with(webTests) {
                extensions == [
                        "yacceleratorstorefront"
                ] as Set<String>
                excludedPackages == [
                        "de.hybris.platform.*"
                ] as Set<String>
            }
        }
    }

    def "parsing a minimal manifest works"() {
        given:
        Map<String, Object> minimalManifest = new JsonSlurper().parse(this.getClass().getResource('/minimal-manifest.json'))

        when:
        Manifest m = Manifest.fromMap(minimalManifest)
        then:
        with(m) {
            commerceSuiteVersion == "1811.3"
            extensions == [
                    "modeltacceleratorservices",
                    "electronicsstore",
                    "privacyoverlayeraddon",
                    "yacceleratorstorefront",
                    "backoffice"
            ] as Set<String>

            aspects == []
            storefrontAddons == []
            tests == TestConfiguration.NO_VALUE
            webTests == TestConfiguration.NO_VALUE
        }

    }
}
