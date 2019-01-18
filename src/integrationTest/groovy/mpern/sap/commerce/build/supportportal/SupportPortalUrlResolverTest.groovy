package mpern.sap.commerce.build.supportportal

import spock.lang.Specification

import static org.junit.Assume.assumeTrue

class SupportPortalUrlResolverTest extends Specification {

    def username
    def password

    def setup() {
        def properties = new Properties()

        this.getClass().getResource('/test-user.properties')?.withInputStream {
            properties.load(it)
        }
        username = properties.username
        password = properties.password
        if (username == null || password == null) {
            username = System.getenv("SUPPORTPORTAL_USER")
            password = System.getenv("SUPPORTPORTAL_PASSWORD")
        }
    }


    def "resolve should find URI for softwaredownloads.sap.com"() {
        given:
        assumeTrue(username != null && password != null)

        //HYBRIS COMMERCE 1808 Maintenance Software Component via searching for "hybris 1808" in https://launchpad.support.sap.com/
//        def source = new URI("https://launchpad.support.sap.com/#/softwarecenter/template/products/%20_APP=00200682500000001943&_EVENT=DISPHIER&HEADER=Y&FUNCTIONBAR=N&EVENT=TREE&NE=NAVIGATE&ENR=73555000100200008593&V=MAINT&TA=ACTUAL&PAGE=SEARCH/HYBRIS%20COMMERCE%201808")
        def source = new URI("https://launchpad.support.sap.com/#/softwarecenter/template/products/%20_APP=00200682500000001943&_EVENT=DISPHIER&HEADER=Y&FUNCTIONBAR=N&EVENT=TREE&NE=NAVIGATE&ENR=73555000100200008592&V=MAINT&TA=ACTUAL&PAGE=SEARCH/HYBRIS%20DATAHUB%201808")

        when:
        CookieManager ssoCookies = SSOLogin.withCredentails(username, password).forResource(SupportPortalUrlResolver.SUPPORT_PORTAL_API)
        def target = SupportPortalUrlResolver.usingCookies(ssoCookies)
                .resolve(source)

        then:
        noExceptionThrown()
        target.host == "softwaredownloads.sap.com"
    }

    def "OData Entries are parsed out of XML response correctly"() {

        def input = "<feed xmlns=\"http://www.w3.org/2005/Atom\" xmlns:m=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\" xmlns:d=\"http://schemas.microsoft.com/ado/2007/08/dataservices\" xml:base=\"https://launchpad.support.sap.com/services/odata/svt/swdcuisrv/\"><id>https://launchpad.support.sap.com/services/odata/svt/swdcuisrv/HierarchyItemSet</id><title type=\"text\">HierarchyItemSet</title><updated>2018-02-10T07:40:53Z</updated><author><name/></author><link href=\"HierarchyItemSet\" rel=\"self\" title=\"HierarchyItemSet\"/><entry><id>https://launchpad.support.sap.com/services/odata/svt/swdcuisrv/HierarchyItemSet('000001')</id><title type=\"text\">HierarchyItemSet('000001')</title><updated>2018-02-10T07:40:53Z</updated><category term=\"/SVT/SWDC_UI_SRV.HierarchyItem\" scheme=\"http://schemas.microsoft.com/ado/2007/08/dataservices/scheme\"/><link href=\"HierarchyItemSet('000001')\" rel=\"self\" title=\"HierarchyItem\"/><content type=\"application/xml\"><m:properties xmlns:m=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\" xmlns:d=\"http://schemas.microsoft.com/ado/2007/08/dataservices\"><d:Id>000001</d:Id><d:NodeType>FILTER</d:NodeType><d:NodeLevel>01</d:NodeLevel><d:NodeParent>000000</d:NodeParent><d:NodeChild>000002</d:NodeChild><d:NodeIcon>sap-icon://download</d:NodeIcon><d:Title>HYBRIS COMMERCE 6.6 (SUPPORT PACKAGES AND PATCHES)</d:Title><d:Description></d:Description><d:ObjectType>CVNR</d:ObjectType><d:SubtreeEvent></d:SubtreeEvent><d:InfoEvent></d:InfoEvent><d:EccnInfoEvent></d:EccnInfoEvent><d:ListEvent></d:ListEvent><d:NewWindowEvent></d:NewWindowEvent><d:CrossLink></d:CrossLink><d:CrossLinkDescription></d:CrossLinkDescription><d:PAMLink></d:PAMLink><d:PAMLinkDescription></d:PAMLinkDescription><d:OSSLegalNotices></d:OSSLegalNotices><d:OSSLegalNoticesDescription></d:OSSLegalNoticesDescription></m:properties></content></entry><entry><id>https://launchpad.support.sap.com/services/odata/svt/swdcuisrv/HierarchyItemSet('000002')</id><title type=\"text\">HierarchyItemSet('000002')</title><updated>2018-02-10T07:40:53Z</updated><category term=\"/SVT/SWDC_UI_SRV.HierarchyItem\" scheme=\"http://schemas.microsoft.com/ado/2007/08/dataservices/scheme\"/><link href=\"HierarchyItemSet('000002')\" rel=\"self\" title=\"HierarchyItem\"/><content type=\"application/xml\"><m:properties xmlns:m=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\" xmlns:d=\"http://schemas.microsoft.com/ado/2007/08/dataservices\"><d:Id>000002</d:Id><d:NodeType>OS</d:NodeType><d:NodeLevel>02</d:NodeLevel><d:NodeParent>000001</d:NodeParent><d:NodeChild>000000</d:NodeChild><d:NodeIcon>sap-icon://download</d:NodeIcon><d:Title># OS INDEPENDENT</d:Title><d:Description></d:Description><d:ObjectType>OS</d:ObjectType><d:SubtreeEvent></d:SubtreeEvent><d:InfoEvent></d:InfoEvent><d:EccnInfoEvent></d:EccnInfoEvent><d:ListEvent>_EVENT=LIST&amp;EVENT=LIST&amp;ENR=73555000100200007322&amp;SWTYPSC=SPP&amp;PECCLSC=OS&amp;INCL_PECCLSC1=OS&amp;PECGRSC1=OSINDEP&amp;V=MAINT&amp;TA=ACTUAL</d:ListEvent><d:NewWindowEvent></d:NewWindowEvent><d:CrossLink></d:CrossLink><d:CrossLinkDescription></d:CrossLinkDescription><d:PAMLink></d:PAMLink><d:PAMLinkDescription></d:PAMLinkDescription><d:OSSLegalNotices></d:OSSLegalNotices><d:OSSLegalNoticesDescription></d:OSSLegalNoticesDescription></m:properties></content></entry></feed>"

        when:
        def entries = SupportPortalUrlResolver.usingCookies(new CookieManager()).parseODataEntries(input)

        then:
        entries.size() == 2
        entries[0]."Id" == "000001"
        entries[1]."Id" == "000002"
        entries[1]."ListEvent" == "_EVENT=LIST&amp;EVENT=LIST&amp;ENR=73555000100200007322&amp;SWTYPSC=SPP&amp;PECCLSC=OS&amp;INCL_PECCLSC1=OS&amp;PECGRSC1=OSINDEP&amp;V=MAINT&amp;TA=ACTUAL"
        entries[1]."entry-id" == "https://launchpad.support.sap.com/services/odata/svt/swdcuisrv/HierarchyItemSet('000002')"
    }
}
