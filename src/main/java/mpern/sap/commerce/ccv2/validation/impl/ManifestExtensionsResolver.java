package mpern.sap.commerce.ccv2.validation.impl;

import static mpern.sap.commerce.ccv2.validation.ValidationUtils.validateAndNormalizePath;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import groovy.lang.Tuple2;

import mpern.sap.commerce.build.util.Extension;
import mpern.sap.commerce.build.util.PlatformResolver;
import mpern.sap.commerce.ccv2.model.Manifest;
import mpern.sap.commerce.ccv2.validation.Error;
import mpern.sap.commerce.ccv2.validation.ExtensionsResolver;

public class ManifestExtensionsResolver implements ExtensionsResolver {

    public static Set<String> CLOUD_ONLY_EXTENSIONS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            // https://help.sap.com/viewer/0fa6bcf4736c46f78c248512391eb467/LATEST/en-US/b13c673497674994a7f243e3225af9b3.html
            "modeltacceleratorservices",
            // https://help.sap.com/viewer/b2f400d4c0414461a4bb7e115dccd779/LATEST/en-US/784f9480cf064d3b81af9cad5739fecc.html
            "modelt",
            // https://help.sap.com/viewer/403d43bf9c564f5a985913d1fbfbf8d7/LATEST/en-US/fba094343e624aae8f041d0170046355.html
            "cloudmediaconversion")));
    private final Path projectRoot;

    public ManifestExtensionsResolver(Path projectRoot) {
        this.projectRoot = projectRoot;
    }

    @Override
    public Result determineEffectiveExtensions(Manifest manifest) {
        Tuple2<Set<String>, List<String>> listing = listAllConfiguredExtensions(manifest);
        Set<String> extensionNames = listing.getFirst();
        Set<String> configuredCloudOnly = new LinkedHashSet<>(extensionNames);
        configuredCloudOnly.retainAll(CLOUD_ONLY_EXTENSIONS);
        extensionNames.removeAll(CLOUD_ONLY_EXTENSIONS);
        // try to load
        try {
            PlatformResolver resolver = new PlatformResolver(
                    this.projectRoot.resolve(Paths.get("hybris/bin/platform")));
            List<Extension> extensions = resolver.loadListOfExtensions(extensionNames);
            for (String cloudOnly : configuredCloudOnly) {
                extensions.add(new Extension(cloudOnly, Paths.get("dummy", cloudOnly)));
            }
            return new Result(extensions, listing.getSecond());
        } catch (Exception e) {
            // ignore
        }
        return Result.NO_RESULT;
    }

    @Override
    public Tuple2<Set<String>, List<String>> listAllConfiguredExtensions(Manifest manifest) {
        Set<String> extensionNames = new LinkedHashSet<>();
        List<String> locations = new ArrayList<>();
        String extensionsLocation = manifest.useConfig.extensions.location;
        if (!extensionsLocation.isEmpty()) {
            Tuple2<Path, List<Error>> xmlFile = validateAndNormalizePath(this.projectRoot, "", extensionsLocation);
            if (xmlFile.getFirst() != null) {
                locations.add(String.format("useConfig.extensions.location (%s)", extensionsLocation));
                extensionNames.addAll(loadExtensionNamesFromExtensionsXML(xmlFile.getFirst()));
                extensionNames.removeAll(manifest.useConfig.extensions.exclude);
            }
        }
        if (!manifest.extensions.isEmpty()) {
            locations.add("extensions");
            extensionNames.addAll(manifest.extensions);
        }
        return new Tuple2<>(extensionNames, locations);
    }

    private Set<String> loadExtensionNamesFromExtensionsXML(Path file) {
        try {
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            Document doc = builder.parse(file.toFile());
            Set<String> extensionNames = new LinkedHashSet<>();
            NodeList extensions = doc.getDocumentElement().getElementsByTagName("extension");
            for (int i = 0; i < extensions.getLength(); i++) {
                Node extension = extensions.item(i);
                Node name = extension.getAttributes().getNamedItem("name");
                if (name != null && name.getNodeValue() != null && !name.getNodeValue().isEmpty()) {
                    extensionNames.add(name.getNodeValue());
                }
            }
            return extensionNames;
        } catch (Exception e) {
            // ignore
        }
        return Collections.emptySet();
    }
}
