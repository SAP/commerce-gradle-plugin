package mpern.sap.commerce.build

import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes

class PackagingTest extends Specification {
    @Rule
    TemporaryFolder testProjectDir = new TemporaryFolder()

    File buildFile

    File common
    File dev
    File stag

    File hybrisPlatformZip
    File hybrisExtensionsZip

    FileSystem packageFile

    String customerID = "customer"
    String projectID = "project"
    String version = "version"

    String packageName = "${customerID}-${projectID}_v${version}"

    GradleRunner runner

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')
        buildFile << """
            plugins {
                id 'mpern.sap.commerce.ccv1.package'
                //add java plugin to setup defaults for all packaging tasks
                //those defaults could potentially break the CCv1 packaging
                id 'base'
            }
            version = '${version}'
            CCV1 {
                customerID = "${customerID}"
                projectID = "${projectID}"
            }
        """

        common = testProjectDir.newFolder("ccv1-configuration", "common")
        createConfigStructure(common.toPath())
        dev = testProjectDir.newFolder("ccv1-configuration", "dev")
        createConfigStructure(dev.toPath())
        stag = testProjectDir.newFolder("ccv1-configuration", "stag")
        createConfigStructure(stag.toPath())

        def hybrisProductionFolder = testProjectDir.newFolder("hybris", "temp", "hybris", "hybrisServer")
        hybrisPlatformZip = new File(hybrisProductionFolder, "hybrisServer-Platform.zip")
        hybrisPlatformZip.createNewFile()

        hybrisExtensionsZip = new File(hybrisProductionFolder, "hybrisServer-AllExtensions.zip")
        hybrisExtensionsZip.createNewFile()

        runner = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
        def testVersion = System.getenv("GRADLE_VERSION")
        if (testVersion) {
            runner.withGradleVersion(testVersion)
        }
        runner.withPluginClasspath()
    }

    def cleanup() {
        try {
            packageFile.close()
        } catch (e) {
            //don't care
        }
    }

    def createConfigStructure(Path base) {
        Files.createDirectories(base.resolve("hybris"))
        Files.createDirectories(base.resolve("datahub"))
        Files.createDirectories(base.resolve("misc"))
    }

    def "packaging fails when no platform artifacts present"() {

        given:
        hybrisPlatformZip.delete()

        when:
        runner.withArguments("--stacktrace", 'buildCCV1Package')
                .buildAndFail()

        then:
        noExceptionThrown()
    }

    def "package name is root folder of zip file"() {

        given: "hybris configuration files in common folder"
        def props = common.toPath().resolve("hybris/customer.properties")
        Files.createFile(props)
        props.toFile() << """
        some content
        """

        when: "building package"
        runner.withArguments("--stacktrace", 'buildCCV1Package')
                .build()

        def (_, Path packageRoot) = openPackageFile()

        then: "package root is package name"
        Files.exists(packageRoot)
        packageRoot.fileName.toString() == packageName
    }


    def "content of common folder is used for all environments"() {

        given: "hybris configuration files in common folder"
        def props = common.toPath().resolve("hybris/customer.properties")
        Files.createFile(props)
        props.toFile() << """
        some content
        """

        when: "building package"
        runner.withArguments("--stacktrace", 'buildCCV1Package')
                .build()

        def (_, Path packageRoot) = openPackageFile()

        then: "files in common are used in package"
        Files.exists(packageRoot.resolve("hybris/config/dev/customer.app.properties"))
        Files.exists(packageRoot.resolve("hybris/config/dev/customer.adm.properties"))
        Files.exists(packageRoot.resolve("hybris/config/stag/customer.app.properties"))
        Files.exists(packageRoot.resolve("hybris/config/stag/customer.adm.properties"))

    }

    def "md5 hash is created for package"() {

        given: "hybris configuration files in common folder"
        def props = common.toPath().resolve("hybris/customer.properties")
        Files.createFile(props)
        props.toFile() << """
            some content
        """

        when: "building package"
        runner.withArguments("--stacktrace", 'buildCCV1Package')
                .build()

        then: "hash file is present"
        Files.exists(testProjectDir.root.toPath().resolve("dist/${packageName}.md5"))
    }

    private void dumpDir(Path p) {
        Files.walkFileTree(p, new SimpleFileVisitor<Path>() {
            @Override
            FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                println(file)
                return super.visitFile(file, attrs)
            }
        })
    }

    def "datahub included with required filename if datahub enabled for CCV1 package"() {

        def datahubfile = "some-other-filename.war"

        given: "datahub is enabled"
        buildFile << """
        CCV1 {
            datahub = true
            datahubWar = file("${datahubfile}")
        }
        """
        def datahub = testProjectDir.newFile("${datahubfile}")
        datahub.createNewFile()

        when: "building CCV1 package"
        runner.withArguments("--stacktrace", 'buildCCV1Package')
                .build()


        def (_, Path packageRoot) = openPackageFile()

        then:
        Files.exists(packageRoot.resolve("datahub/bin/datahub-webapp.war"))
    }

    def "enabling datahub without configuring a valid file fails"() {

        given: "datahub is enabled but no file configured"
        buildFile << """
        CCV1 {
            datahub = true
            datahubWar = file('doesnotexist.war')
        }
        """

        when: "building CCV1 package and fail"
        runner.withArguments("--stacktrace", 'buildCCV1Package')
                .buildAndFail()

        then: "it failed"
        noExceptionThrown()
    }

    def "datahub config files included when datahub enabled"() {
        given: "datahub is enabled, configuration files present"
        buildFile << """
        CCV1 {
            datahub = true
            datahubWar = file('datahub.war')
        }
        """
        testProjectDir.newFile("datahub.war")
        def commonDatahubConfig = common.toPath().resolve("datahub")
        Files.createDirectories(commonDatahubConfig)
        Files.createFile(commonDatahubConfig.resolve("dhub-encrypt-key"))
        def devDatahubConfig = dev.toPath().resolve("datahub")
        Files.createDirectories(devDatahubConfig)
        Files.createFile(devDatahubConfig.resolve("customer.properties"))

        when:
        runner.withArguments("--stacktrace", 'buildCCV1Package')
                .build()

        def (_, Path packageRoot) = openPackageFile()

        then:
        Files.exists(packageRoot.resolve("datahub/config/dev/customer.properties"))
        Files.exists(packageRoot.resolve("datahub/config/dev/dhub-encrypt-key"))
        Files.exists(packageRoot.resolve("datahub/config/stag/dhub-encrypt-key"))
    }

    private Tuple openPackageFile() {
        packageFile = FileSystems.newFileSystem(testProjectDir.root.toPath().resolve("dist/${packageName}.zip"), null)
        new Tuple(packageFile, packageFile.getPath("${packageName}"))
    }

    def "property files are merged from common into environment specific configs"() {
        given: "property files in common and environment"
        def commonProperties = common.toPath().resolve("hybris/customer.properties")
        Files.createFile(commonProperties)
        commonProperties << """
        property.from.common=common
        property.to.override=common
        """.stripIndent()
        def devProperties = dev.toPath().resolve("hybris/customer.properties")
        Files.createFile(devProperties)
        devProperties << """
        property.to.override=dev
        property.from.dev=dev
        """.stripIndent()

        when:
        runner.withArguments("--stacktrace", 'buildCCV1Package')
                .build()

        def (_, Path packageRoot) = openPackageFile()
        def adminProperties = packageRoot.resolve("hybris/config/dev/customer.adm.properties")
        def appProperties = packageRoot.resolve("hybris/config/dev/customer.app.properties")
        dumpDir(packageRoot)
        then:
        !Files.exists(packageRoot.resolve("hybris/config/dev/customer.properties"))
        !Files.exists(packageRoot.resolve("hybris/config/stag/customer.properties"))
        !Files.exists(packageRoot.resolve("hybris/config/prod/customer.properties"))
        verifyPropertiesAreMergedCorrectly(adminProperties)
        verifyPropertiesAreMergedCorrectly(appProperties)
    }

    void verifyPropertiesAreMergedCorrectly(Path p) {
        assert Files.exists(p)
        assert p.text.contains("property.to.override=dev")
        assert p.text.contains("property.from.dev=dev")
        assert p.text.contains("property.from.common=common")
    }

    def "localextensions.xml built from env or from common"() {
        def common = common.toPath().resolve("hybris/localextensions.xml")
        Files.createFile(common)
        common << "common"

        def dev = dev.toPath().resolve("hybris/localextensions.xml")
        Files.createFile(dev)
        dev << "dev"

        def stag_adm = stag.toPath().resolve("hybris/localextensions.adm.xml")
        Files.createFile(stag_adm)
        stag_adm << "stag adm"

        def stag_app = stag.toPath().resolve("hybris/localextensions.app.xml")
        Files.createFile(stag_app)
        stag_app << "stag app"

        when:
        runner.withArguments("--stacktrace", 'buildCCV1Package')
                .build()

        def (_, Path packageRoot) = openPackageFile()

        then:
        !Files.exists(packageRoot.resolve("hybris/config/dev/localextensions.xml"))
        !Files.exists(packageRoot.resolve("hybris/config/stag/localextensions.xml"))
        !Files.exists(packageRoot.resolve("hybris/config/prod/localextensions.xml"))
        packageRoot.resolve("hybris/config/dev/localextensions.app.xml").text == "dev"
        packageRoot.resolve("hybris/config/dev/localextensions.adm.xml").text == "dev"
        packageRoot.resolve("hybris/config/stag/localextensions.adm.xml").text == "stag adm"
        packageRoot.resolve("hybris/config/stag/localextensions.app.xml").text == "stag app"
        packageRoot.resolve("hybris/config/prod/localextensions.adm.xml").text == "common"
        packageRoot.resolve("hybris/config/prod/localextensions.app.xml").text == "common"

    }

    def "misc folder is included in package"() {
        def common = common.toPath().resolve("misc/somefile.txt")
        Files.createFile(common)
        common << "common"

        def dev = dev.toPath().resolve("misc/somefile.txt")
        Files.createFile(dev)
        dev << "dev"

        when:
        runner.withArguments("--stacktrace", 'buildCCV1Package')
                .build()
        def (_, Path packageRoot) = openPackageFile()

        then:
        packageRoot.resolve("hybris/misc/dev/somefile.txt").text == "dev"
        packageRoot.resolve("hybris/misc/stag/somefile.txt").text == "common"
        packageRoot.resolve("hybris/misc/prod/somefile.txt").text == "common"
    }
}
