package mpern.sap.commerce.ccv2.validation;

import mpern.sap.commerce.build.util.Extension;
import mpern.sap.commerce.ccv2.model.Manifest;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestExtensionResolver implements ExtensionsResolver {
    private List<Extension> extensions = new ArrayList<>();

    @Override
    public Result determineEffectiveExtensions(Manifest manifest) {
        return new Result(extensions, Collections.singletonList("test.extensions"));
    }

    public void addExtension(String name) {
        extensions.add(new Extension(name, Paths.get("test", name)));
    }

}
