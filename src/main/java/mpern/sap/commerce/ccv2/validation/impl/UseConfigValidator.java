package mpern.sap.commerce.ccv2.validation.impl;

import static mpern.sap.commerce.ccv2.model.Aspect.ALLOWED_ASPECTS;
import static mpern.sap.commerce.ccv2.model.Property.ALLOWED_PERSONAS;
import static mpern.sap.commerce.ccv2.validation.ValidationUtils.validateAndNormalizePath;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import groovy.lang.Tuple2;

import mpern.sap.commerce.ccv2.model.Manifest;
import mpern.sap.commerce.ccv2.model.useconfig.Properties;
import mpern.sap.commerce.ccv2.validation.Error;
import mpern.sap.commerce.ccv2.validation.Level;
import mpern.sap.commerce.ccv2.validation.Validator;

public class UseConfigValidator implements Validator {

    private final Path projectRoot;

    public UseConfigValidator(Path projectRoot) {
        this.projectRoot = projectRoot;
    }

    @Override
    public List<Error> validate(Manifest manifest) throws Exception {
        List<Error> errors = new ArrayList<>();
        errors.addAll(validateProperties(manifest));
        errors.addAll(validateExtensions(manifest));
        errors.addAll(validateSolr(manifest));
        return errors;
    }

    private List<Error> validateProperties(Manifest manifest) {
        List<Error> errors = new ArrayList<>();
        for (int i = 0; i < manifest.useConfig.properties.size(); i++) {
            Properties properties = manifest.useConfig.properties.get(i);
            Tuple2<Path, List<Error>> result = validateAndNormalizePath(this.projectRoot,
                    String.format("useConfig.properties[%d]", i), properties.location);
            errors.addAll(result.getSecond());
            if (!properties.aspect.isEmpty() && !ALLOWED_ASPECTS.contains(properties.aspect)) {
                errors.add(new Error.Builder().setLocation("useConfig.properties[%d]", i)
                        .setMessage("Aspect `%s` not supported", properties.aspect).setCode("E-002").createError());
            }
            if (!properties.persona.isEmpty() && !ALLOWED_PERSONAS.contains(properties.persona)) {
                errors.add(new Error.Builder().setLocation("useConfig.properties[%d]", i)
                        .setMessage("Persona `%s` not supported", properties.persona).setCode("E-008").createError());
            }
            if (result.getFirst() != null) {
                try (InputStream stream = Files.newInputStream(result.getFirst())) {
                    new java.util.Properties().load(stream);
                } catch (Exception e) {
                    errors.add(new Error.Builder().setLocation("useConfig.properties[%d]", i)
                            .setMessage("`%s` is not a valid Java properties file", properties.location)
                            .setCode("E-010").createError());
                }
                try {
                    // ref. java doc of java.util.Properties.load(InputStream)
                    String defaultCharset = String.join("\n",
                            Files.readAllLines(result.getFirst(), StandardCharsets.ISO_8859_1));
                    String utf8 = String.join("\n", Files.readAllLines(result.getFirst(), StandardCharsets.UTF_8));
                    if (!defaultCharset.equals(utf8)) {
                        errors.add(new Error.Builder().setLocation("useConfig.properties[%d]", i)
                                .setLevel(Level.WARNING)
                                .setMessage(
                                        "`%s` seems to use a different charset than ISO 8859-1. This might lead to corrupted properties after build and deployment.",
                                        properties.location)
                                .setCode("W-002").createError());
                    }
                } catch (MalformedInputException e) {
                    errors.add(new Error.Builder().setLocation("useConfig.properties[%d]", i).setLevel(Level.WARNING)
                            .setMessage(
                                    "`%s` seems to use a different charset than ISO 8859-1. This might lead to corrupted properties after build and deployment.",
                                    properties.location)
                            .setCode("W-002").createError());
                } catch (IOException e) {
                    // shouldn't happen
                }
            }
        }
        return errors;
    }

    private List<Error> validateExtensions(Manifest manifest) {
        List<Error> errors = new ArrayList<>();
        String localExtensionsLocation = manifest.useConfig.extensions.location;
        if (!localExtensionsLocation.isEmpty()) {
            Tuple2<Path, List<Error>> localExtensions = validateAndNormalizePath(this.projectRoot,
                    "useConfig.extensions.location", localExtensionsLocation);
            errors.addAll(localExtensions.getSecond());
            if (localExtensions.getFirst() != null) {
                try {
                    DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = domFactory.newDocumentBuilder();
                    Document doc = builder.parse(localExtensions.getFirst().toFile());
                    if (!"hybrisconfig".equals(doc.getDocumentElement().getTagName())) {
                        errors.add(new Error.Builder().setLocation("useConfig.extensions.location")
                                .setMessage("File `%s` is not a valid localextensions.xml file",
                                        localExtensionsLocation)
                                .setCode("E-011").createError());
                    } else {
                        NodeList extensions = doc.getDocumentElement().getElementsByTagName("extension");
                        for (int i = 0; i < extensions.getLength(); i++) {
                            Node extension = extensions.item(i);
                            if (extension.getAttributes().getNamedItem("dir") != null) {
                                errors.add(new Error.Builder().setLocation("useConfig.extensions.location").setMessage(
                                        "`%s`: Attribute `extension.dir` is not supported. Only use `extension.name` to declare extensions.",
                                        localExtensionsLocation).setCode("E-012").createError());
                                break;
                            }
                        }
                    }
                } catch (SAXException | IOException e) {
                    errors.add(new Error.Builder().setLocation("useConfig.extensions.location")
                            .setMessage("File `%s` is not a valid extensions.xml file", localExtensionsLocation)
                            .setCode("E-011").createError());
                } catch (ParserConfigurationException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return errors;
    }

    private List<Error> validateSolr(Manifest manifest) {
        List<Error> errors = new ArrayList<>();
        String solrCustom = manifest.useConfig.solr.location;
        if (!solrCustom.isEmpty()) {
            Tuple2<Path, List<Error>> solrCustomPath = validateAndNormalizePath(this.projectRoot,
                    "useConfig.solr.location", solrCustom);
            errors.addAll(solrCustomPath.getSecond());
            if (solrCustomPath.getFirst() != null) {
                Path expected = solrCustomPath.getFirst().resolve(Paths.get("server/solr/configsets/default/conf"));
                if (!Files.exists(expected)) {
                    errors.add(new Error.Builder().setLocation("useConfig.solr.location").setMessage(
                            "Location `%s` does not contain the required folder structure `server/solr/configsets/default/conf`",
                            solrCustom).setCode("E-013").createError());
                }
                if (manifest.solrVersion.isEmpty()) {
                    errors.add(new Error.Builder().setLocation("solrVersion").setLevel(Level.WARNING).setMessage(
                            "Solr customization without pinned Solr version.\nThis may lead to unexpected build errors if a patch releases changes the Solr version.")
                            .setCode("W-004").createError());
                }
            }
        }
        return errors;
    }
}
