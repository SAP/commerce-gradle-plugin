package mpern.sap.commerce.ccv2.tasks;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.TaskAction;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public abstract class PatchLocalExtensions extends DefaultTask {

    @TaskAction
    public void addCepLoadDir() throws Exception {

        Path hybrisBin = getProject().getRootDir().toPath().resolve(Paths.get("hybris", "bin"));
        Path cepPath = Path.of(getCepFolder().get());

        Path relativize = hybrisBin.relativize(cepPath);
        String cepPathString = "${HYBRIS_BIN_DIR}" + File.separator + relativize;

        patchLocalExtensions(cepPathString);
    }

    private void patchLocalExtensions(String cepPathString) throws Exception {
        Path localExtensions = getTarget().get().getAsFile().toPath();
        if (!(Files.exists(localExtensions))) {
            getLogger().debug("{} not found; nothing to do", localExtensions);
            return;
        }

        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();

        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document doc = builder.parse(localExtensions.toFile());

        NodeList paths = doc.getDocumentElement().getElementsByTagName("path");

        int platformIndex = -1;
        int cepIndex = -1;
        for (int i = 0; i < paths.getLength(); i++) {
            Node item = paths.item(i);
            String dir = item.getAttributes().getNamedItem("dir").getNodeValue();
            if ("${HYBRIS_BIN_DIR}".equalsIgnoreCase(dir)) {
                platformIndex = i;
            }
            if (cepPathString.equalsIgnoreCase(dir)) {
                cepIndex = i;
            }
        }
        if (cepIndex == -1) {
            getLogger().lifecycle("cloud extension pack not configured in localextensions.xml, patching...");
            Comment comment = doc.createComment("generated by commerce-gradle-plugin");
            paths.item(0).getParentNode().insertBefore(comment, paths.item(0));
            Text textNode = doc.createTextNode("\n");
            paths.item(0).getParentNode().insertBefore(textNode, paths.item(0));
            Element cepPath = doc.createElement("path");
            cepPath.setAttribute("dir", cepPathString);
            cepPath.setAttribute("autoload", "false");
            paths.item(0).getParentNode().insertBefore(cepPath, paths.item(0));
            textNode = doc.createTextNode("\n");
            paths.item(0).getParentNode().insertBefore(textNode, paths.item(1));
            textNode = doc.createTextNode("\n");
            paths.item(0).getParentNode().insertBefore(textNode, paths.item(1));

        } else if (cepIndex > platformIndex) {
            throw new GradleException(String.format("%s: <path dir='%s'> must be before <path='${HYBRIS_BIN_DIR}'>",
                    getProject().getRootDir().toPath().relativize(localExtensions), cepPathString));
        }

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        StreamResult result = new StreamResult(localExtensions.toFile());
        DOMSource source = new DOMSource(doc);
        transformer.transform(source, result);
    }

    @InputFiles
    public abstract RegularFileProperty getTarget();

    @Input
    public abstract Property<String> getCepFolder();
}
