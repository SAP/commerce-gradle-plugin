package mpern.sap.commerce.ccv2.validation;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import groovy.lang.Tuple2;

import mpern.sap.commerce.build.util.Extension;
import mpern.sap.commerce.ccv2.model.Manifest;

public interface ExtensionsResolver {

    Result determineEffectiveExtensions(Manifest manifest);

    Tuple2<Set<String>, List<String>> listAllConfiguredExtensions(Manifest manifest);

    class Result {
        public static final Result NO_RESULT = new Result(Collections.emptyList(), Collections.emptyList());
        public final List<Extension> extensions;
        public final List<String> locations;

        public Result(List<Extension> extensions, List<String> locations) {
            this.extensions = extensions;
            this.locations = locations;
        }

        public List<Extension> getExtensions() {
            return extensions;
        }

        public List<String> getLocations() {
            return locations;
        }
    }
}
