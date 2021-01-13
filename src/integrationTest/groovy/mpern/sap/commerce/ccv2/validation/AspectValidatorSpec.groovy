package mpern.sap.commerce.ccv2.validation

import groovy.json.JsonSlurper
import spock.lang.Specification

import mpern.sap.commerce.ccv2.model.Manifest
import mpern.sap.commerce.ccv2.validation.impl.AspectValidator

class AspectValidatorSpec extends Specification {

    Manifest manifest
    AspectValidator validator

    def setup() {
        def rawManifest = new JsonSlurper().parse(this.getClass().getResource('/validator/aspect-manifest.json')) as Map<String, Object>
        manifest = Manifest.fromMap(rawManifest)
        validator = new AspectValidator();
    }

    def "validating aspects detects errors"() {
        when:
        List<Error> errors = validator.validate(manifest)
        errors.forEach{
            System.out.println(it)
        }
        then:
        errors.size() == 6
        errors.any {it.level == Level.ERROR && it.location == "aspects[?name == 'invalid']" && it.message.contains("not supported")}
        errors.any {it.level == Level.ERROR && it.location == "aspects[?name == 'admin']" && it.message.contains("Webapps")}
        errors.any {it.level == Level.ERROR && it.location == "aspects[?name == 'accstorefront'].webapps[3]" && it.message.contains("Context path ``")}
        errors.any {it.level == Level.ERROR && it.location == "aspects[?name == 'backoffice'].webapps[3]" && it.message.contains("Extension `hac`")}
        errors.any {it.level == Level.WARNING && it.location == "aspects[?name == 'backoffice'].properties[2]" && it.message.contains('`spring.session.enabled`')}
        errors.any {it.level == Level.ERROR && it.location == "aspects[?name == 'accstorefront'].webapps[0]" && it.message.contains("`/`")}
    }
}
