package mpern.sap.commerce.ccv2.validation.impl;

import static mpern.sap.commerce.ccv2.model.Aspect.ALLOWED_ASPECTS;
import static mpern.sap.commerce.ccv2.model.Property.ALLOWED_PERSONAS;
import static mpern.sap.commerce.ccv2.validation.ValidationUtils.validateAndNormalizePath;

import java.io.IOException;
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
                        .setMessage("Aspect `%s` not supported", properties.aspect)
                        .setLink(
                                "https://help.sap.com/viewer/1be46286b36a4aa48205be5a96240672/LATEST/en-US/2311d89eef9344fc81ef168ac9668307.html")
                        .createError());
            }
            if (!properties.persona.isEmpty() && !ALLOWED_PERSONAS.contains(properties.persona)) {
                errors.add(new Error.Builder().setLocation("useConfig.properties[%d]", i)
                        .setMessage("Persona `%s` not supported", properties.persona)
                        .setLink(
                                "https://help.sap.com/viewer/1be46286b36a4aa48205be5a96240672/LATEST/en-US/2311d89eef9344fc81ef168ac9668307.html")
                        .createError());
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
                                .setLink(
                                        "https://help.sap.com/viewer/1be46286b36a4aa48205be5a96240672/v2011/en-US/2311d89eef9344fc81ef168ac9668307.html")
                                .createError());
                    } else {
                        NodeList extensions = doc.getDocumentElement().getElementsByTagName("extension");
                        for (int i = 0; i < extensions.getLength(); i++) {
                            Node extension = extensions.item(i);
                            if (extension.getAttributes().getNamedItem("dir") != null) {
                                errors.add(new Error.Builder().setLocation("useConfig.extensions.location").setMessage(
                                        "`%s`: Attribute `extension.dir` is not supported. Only use `extension.name` to declare extensions.",
                                        localExtensionsLocation)
                                        .setLink(
                                                "https://help.sap.com/viewer/1be46286b36a4aa48205be5a96240672/LATEST/en-US/2311d89eef9344fc81ef168ac9668307.html")
                                        .createError());
                                break;
                            }
                        }
                    }
                } catch (SAXException | IOException e) {
                    errors.add(new Error.Builder().setLocation("useConfig.extensions.location")
                            .setMessage("File `%s` is not a valid localextensions.xml file", localExtensionsLocation)
                            .setLink(
                                    "https://help.sap.com/viewer/1be46286b36a4aa48205be5a96240672/v2011/en-US/2311d89eef9344fc81ef168ac9668307.html")
                            .createError());
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
                            solrCustom)
                            .setLink(
                                    "https://help.sap.com/viewer/b2f400d4c0414461a4bb7e115dccd779/LATEST/en-US/f7251d5a1d6848489b1ce7ba46300fe6.html")
                            .createError());
                }
            }
        }
        return errors;
    }
}
