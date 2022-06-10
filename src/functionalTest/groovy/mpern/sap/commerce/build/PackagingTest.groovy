package mpern.sap.commerce.build

import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes

import org.gradle.testkit.runner.GradleRunner

import spock.lang.Specification
import spock.lang.TempDir

class PackagingTest extends Specification {
    @TempDir
    Path testProjectDir

    Path buildFile

    Path common
    Path dev
    Path stag

    Path hybrisPlatformZip
    Path hybrisExtensionsZip

    FileSystem packageFile

    String customerID = "customer"
    String projectID = "project"
    String version = "version"

    String packageName = "${customerID}-${projectID}_v${version}"

    GradleRunner runner

    def setup() {
        buildFile = testProjectDir.resolve('build.gradle')
        buildFile << """
            plugins {
                id 'sap.commerce.ccv1.package'
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

        common = testProjectDir.resolve(Paths.get("ccv1-configuration", "common"))
        createConfigStructure(common)
        dev = testProjectDir.resolve(Paths.get("ccv1-configuration", "dev"))
        createConfigStructure(dev)
        stag = testProjectDir.resolve(Paths.get("ccv1-configuration", "stag"))
        createConfigStructure(stag)

        def hybrisProductionFolder = testProjectDir.resolve("hybris/temp/hybris/hybrisServer")
        Files.createDirectories(hybrisProductionFolder)
        hybrisPlatformZip = hybrisProductionFolder.resolve("hybrisServer-Platform.zip")
        Files.createFile(hybrisPlatformZip)

        hybrisExtensionsZip = hybrisProductionFolder.resolve("hybrisServer-AllExtensions.zip")
        Files.createFile(hybrisExtensionsZip)

        runner = GradleRunner.create()
                .withProjectDir(testProjectDir.toFile())
        def gradleVersion = System.getenv("GRADLE_VERSION")
        if (gradleVersion) {
            println "Using Gradle ${gradleVersion}"
            runner.withGradleVersion(gradleVersion)
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
        Files.delete(hybrisPlatformZip)

        when:
        runner.withArguments("--stacktrace", 'buildCCV1Package')
                .buildAndFail()

        then:
        noExceptionThrown()
    }

    def "package name is root folder of zip file"() {

        given: "hybris configuration files in common folder"
        def props = common.resolve("hybris/customer.properties")
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
        def props = common.resolve("hybris/customer.properties")
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
        def props = common.resolve("hybris/customer.properties")
        Files.createFile(props)
        props.toFile() << """
            some content
        """

        when: "building package"
        runner.withArguments("--stacktrace", 'buildCCV1Package')
                .build()

        then: "hash file is present"
        Files.exists(testProjectDir.resolve("dist/${packageName}.md5"))
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
        def datahub = testProjectDir.resolve("${datahubfile}")
        Files.createFile(datahub)

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
        Files.createFile(testProjectDir.resolve("datahub.war"))
        def commonDatahubConfig = common.resolve("datahub")
        Files.createDirectories(commonDatahubConfig)
        Files.createFile(commonDatahubConfig.resolve("dhub-encrypt-key"))
        def devDatahubConfig = dev.resolve("datahub")
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
        packageFile = FileSystems.newFileSystem(testProjectDir.resolve("dist/${packageName}.zip"), Collections.emptyMap())
        new Tuple(packageFile, packageFile.getPath("${packageName}"))
    }

    def "property files are merged from common into environment specific configs"() {
        given: "property files in common and environment"
        def commonProperties = common.resolve("hybris/customer.properties")
        Files.createFile(commonProperties)
        commonProperties << """
        property.from.common=common
        property.to.override=common
        """.stripIndent()
        def devProperties = dev.resolve("hybris/customer.properties")
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
        def common = common.resolve("hybris/localextensions.xml")
        Files.createFile(common)
        common << "common"

        def dev = dev.resolve("hybris/localextensions.xml")
        Files.createFile(dev)
        dev << "dev"

        def stag_adm = stag.resolve("hybris/localextensions.adm.xml")
        Files.createFile(stag_adm)
        stag_adm << "stag adm"

        def stag_app = stag.resolve("hybris/localextensions.app.xml")
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
        def common = common.resolve("misc/somefile.txt")
        Files.createFile(common)
        common << "common"

        def dev = dev.resolve("misc/somefile.txt")
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
