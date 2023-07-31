package mpern.sap.commerce.build.extensioninfo;

import static org.w3c.dom.Node.ELEMENT_NODE;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import mpern.sap.commerce.build.util.Extension;
import mpern.sap.commerce.build.util.ExtensionType;

/**
 * Utility functions for loading extension information from Hybris specific XML
 * files.
 */
public final class ExtensionXmlUtil {

    /**
     * Loads the names of all extensions declared in localextensions.xml. Only
     * extensions declared with "extension" elements are supported. Autoloaded paths
     * are not supported.
     *
     * @param file the localextensions.xml to be loaded
     * @return the declared extensions names
     */
    public static Set<String> loadExtensionNamesFromLocalExtensionsXML(File file) {
        try {
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            Document doc = builder.parse(file);
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
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new ExtensionInfoException("Error parsing localextensions.xml file " + file, e);
        }
    }

    /**
     * Extracts an Extension information object from a given extensioninfo.xml file.
     *
     * @param extensioninfoXmlFile the location of extensioninfo.xml file
     * @param rootLocation         the root location of the project
     * @param extensionType        type of extension being extracted
     * @return the information object
     */
    @Nonnull
    public static Extension loadExtensionFromExtensioninfoXml(File extensioninfoXmlFile, String rootLocation,
            ExtensionType extensionType) {
        try {
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            Document doc = builder.parse(extensioninfoXmlFile);
            Node extensionNode = doc.getDocumentElement().getElementsByTagName("extension").item(0);

            Path extensionPath = extensioninfoXmlFile.toPath().getParent();
            return new Extension(extractExtensionNameFromNode(extensionNode, extensioninfoXmlFile),
                    getRelativeLocation(extensionPath, rootLocation), extensionType,
                    extractRequiredExtensionsNamesFromNode(extensionNode));

        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new ExtensionInfoException("Error parsing extensioninfo file " + extensioninfoXmlFile, e);
        }
    }

    private static String extractExtensionNameFromNode(Node extensionNode, File extensioninfoXmlFile)
            throws IOException {
        Node nameNode = extensionNode.getAttributes().getNamedItem("name");
        if (nameNode != null && nameNode.getNodeValue() != null && !nameNode.getNodeValue().isEmpty()) {
            return nameNode.getNodeValue();
        } else {
            throw new ExtensionInfoException("Found extension without name in file " + extensioninfoXmlFile);
        }
    }

    private static List<String> extractRequiredExtensionsNamesFromNode(Node extensionNode) throws IOException {
        NodeList extensionNodeChildNodes = extensionNode.getChildNodes();
        List<Element> requiresExtensionElements = new ArrayList<>();
        for (int i = 0; i < extensionNodeChildNodes.getLength(); i++) {
            Node node = extensionNodeChildNodes.item(i);
            if (node.getNodeType() == ELEMENT_NODE && node.getNodeName() == "requires-extension") {
                requiresExtensionElements.add((Element) node);
            }
        }

        List<String> requiredExtensionsNames = new ArrayList<>();
        for (Element requiresExtensionElement : requiresExtensionElements) {
            Attr nameAttr = requiresExtensionElement.getAttributeNode("name");
            requiredExtensionsNames.add(nameAttr.getValue());
        }

        return requiredExtensionsNames;
    }

    private static Path getRelativeLocation(Path fullLocation, String rootLocation) {
        // normalize to a Unix path
        String fullUnixLocation = fullLocation.toString().replace("\\", "/");
        // get everything after last rootLocation
        int rootLocationPos = fullUnixLocation.lastIndexOf(rootLocation);
        if (rootLocationPos == -1) {
            throw new ExtensionInfoException(
                    "Full location [" + fullUnixLocation + "] does not contain [" + rootLocation + "]");
        }
        return Path.of(fullUnixLocation.substring(rootLocationPos + rootLocation.length()));
    }

    private ExtensionXmlUtil() {
        // no instances for util class
    }

}
