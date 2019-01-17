package mpern.sap.commerce.build.util

import groovy.json.JsonSlurper
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import java.nio.file.Path
import java.nio.file.Paths

class DownloadInfoFileTest extends Specification {

    @Rule
    TemporaryFolder tempFolder = new TemporaryFolder();

    def "getInfoFile uses original path with .download-info extension"(Path original, Path expected) {

        expect:
        DownloadInfoFile.infoFileName(original) == expected;

        where:
        original                                          || expected
        Paths.get("some/folder/file.extension")           || Paths.get("some/folder/file.extension.download-info")
        Paths.get("some/folder/file.extension.extension") || Paths.get("some/folder/file.extension.extension.download-info")
        Paths.get("some/folder/file")                     || Paths.get("some/folder/file.download-info")

    }

    def "write produces expected output"() {
        def infoFile = tempFolder.newFile()
        DownloadInfoFile f = new DownloadInfoFile(infoFile.toPath())

        f.eTag = "etag-value"
        f.cachedMd5Hash = "hash-value"
        f.originalFilename = "originalfilename-value"
        f.lastModified = "lastmodified-value"
        f.downloadUrl = "downloadurl-value"

        when:
        f.write()
        def json = new JsonSlurper().parse(infoFile)

        then:
        json.eTag == f.eTag
        json.cachedMd5Hash == f.cachedMd5Hash
        json.originalFilename == f.originalFilename
        json.lastModified == f.lastModified
        json.downloadUrl == f.downloadUrl
    }

    def "parse works"() {
        def infoFile = tempFolder.newFile()
        infoFile << """
        {
            "cachedMd5Hash": "hash-value",
            "lastModified": "lastmodified-value",
            "eTag": "etag-value",
            "originalFilename": "original-value",
            "downloadUrl": "download-value"
        }
        """.stripIndent()


        when:
        DownloadInfoFile f = new DownloadInfoFile(infoFile.toPath())

        then:
        f.eTag == "etag-value"
        f.cachedMd5Hash == "hash-value"
        f.originalFilename == "original-value"
        f.lastModified == "lastmodified-value"
        f.downloadUrl == "download-value"
    }
}
