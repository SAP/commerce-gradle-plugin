package mpern.sap.commerce.build.supportportal

import spock.lang.Specification

import java.util.stream.Collectors

import static org.junit.Assume.assumeTrue

class SSOLoginTests extends Specification {

    def username
    def password

    def setup() {
        def properties = new Properties()
        this.getClass().getResource('/test-user.properties')?.withInputStream {
            properties.load(it)
        }
        username = properties.username
        password = properties.password
    }

    def "try to login for same resource"() {
        given:
        assumeTrue(username != null && password != null)

        def uri = new URI("https://launchpad.support.sap.com/services/odata/svt/swdcuisrv")

        when:
        CookieManager cm = SSOLogin.withCredentails(username, password).forResource(uri)

        HttpURLConnection c = uri.toURL().openConnection()
        List<HttpCookie> cookies = cm.getCookieStore().get(uri)
        def param = cookies.stream().map({ keks -> keks.toString() }).collect(Collectors.joining("; "))
        c.addRequestProperty("Cookie", param)
        def result = c.inputStream.text
        c.disconnect()

        then:
        result.startsWith("<?xml version=\"1.0\" encoding=\"utf-8\"?><app:service")
    }
}
