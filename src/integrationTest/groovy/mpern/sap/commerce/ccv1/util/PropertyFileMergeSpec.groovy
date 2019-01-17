package mpern.sap.commerce.ccv1.util

import org.junit.ClassRule
import org.junit.rules.TemporaryFolder
import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Path

class PropertyFileMergeSpec extends Specification {

    @Shared
    @ClassRule
    TemporaryFolder propertyFolder = new TemporaryFolder()

    @Shared
    Path first
    @Shared
    Path second
    @Shared
    Path third

    def setupSpec() {
        def root = propertyFolder.root.toPath()

        first = root.resolve("first.properties")
        first << """
        property=first
        fromFirst=value
        """

        second = root.resolve("second.properties")
        second << """
        property=second
        fromSecond = value
        """

        third = root.resolve("third.properties")
        third << """
        property=third
        fromThird = value
        """
    }

    def "files are merged in order"(List files, String result) {
        expect:
        new PropertyFileMerger(files).mergeProperties()['property'] == result

        where:
        files                  || result
        [first, second, third] || "third"
        [first, third, second] || "second"
        [third, second, first] || "first"
    }

    def "result of merge is a set union of all properties"() {
        when:
        def result = new PropertyFileMerger([first, second, third]).mergeProperties()

        then:
        result["property"] == "third"
        result["fromFirst"] == "value"
        result["fromSecond"] == "value"
        result["fromThird"] == "value"
    }
}
