package mpern.sap.commerce.ccv2.validation;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import groovy.lang.Tuple2;

import mpern.sap.commerce.build.util.Extension;
import mpern.sap.commerce.ccv2.model.Manifest;

/**
 * Resolves information about extensions declared in the build manifest.
 */
public interface ExtensionsResolver {

    /**
     * Resolves the detailed information about the extensions configured in the
     * manifest.
     *
     * @param manifest the manifest being used
     * @return the operation result
     */
    Result determineEffectiveExtensions(Manifest manifest);

    /**
     * Gets the names of the configured extensions from the manifest.
     *
     * @param manifest the manifest to get the information from
     * @return first element in tuple is the set of extension names that are
     *         configured, second element contains the list of locations from where
     *         the extension names are obtained
     */
    Tuple2<Set<String>, List<String>> listAllConfiguredExtensions(Manifest manifest);

    class Result {
        public static final Result NO_RESULT = new Result(Collections.emptyList(), Collections.emptyList());
        public final List<Extension> extensions;
        public final List<String> locations;

        public Result(List<Extension> extensions, List<String> locations) {
            this.extensions = Collections.unmodifiableList(extensions);
            this.locations = Collections.unmodifiableList(locations);
        }

        public List<Extension> getExtensions() {
            return extensions;
        }

        public List<String> getLocations() {
            return locations;
        }
    }
}
