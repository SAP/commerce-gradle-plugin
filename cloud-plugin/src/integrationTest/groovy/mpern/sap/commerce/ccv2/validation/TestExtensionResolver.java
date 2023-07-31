package mpern.sap.commerce.ccv2.validation;

import groovy.lang.Tuple2;
import mpern.sap.commerce.build.util.Extension;
import mpern.sap.commerce.ccv2.model.Manifest;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TestExtensionResolver implements ExtensionsResolver {
    private List<Extension> extensions = new ArrayList<>();

    @Override
    public Result determineEffectiveExtensions(Manifest manifest) {
        return new Result(extensions, Collections.singletonList("test.extensions"));
    }

    @Override
    public Tuple2<Set<String>, List<String>> listAllConfiguredExtensions(Manifest manifest) {
        return new Tuple2<>(extensions.stream().map(e -> e.name).collect(Collectors.toUnmodifiableSet()), Collections.singletonList("test.extensions"));
    }

    public void addExtension(String name) {
        extensions.add(new Extension(name, Path.of("test", name)));
    }

}
