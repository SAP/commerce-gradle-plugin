package mpern.sap.commerce.build.extensioninfo;

import static mpern.sap.commerce.build.HybrisPlugin.HYBRIS_BIN_DIR;
import static mpern.sap.commerce.build.HybrisPlugin.HYBRIS_PLATFORM_CONFIGURATION;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.gradle.api.Project;
import org.gradle.api.file.FileTree;
import org.gradle.api.tasks.util.PatternSet;

import mpern.sap.commerce.build.util.Extension;
import mpern.sap.commerce.build.util.ExtensionType;

/**
 * Responsible for the creation of
 * {@link mpern.sap.commerce.build.util.Extension} DTOs for the core build
 * plugin.
 */
public class ExtensionInfoLoader {

    private static final String CUSTOM_DIR = "custom";

    private final Project project;

    public ExtensionInfoLoader(Project project) {
        this.project = project;
    }

    /**
     * Gets the extensions from the Hybris project custom folder.
     *
     * @return found extensions
     */
    public Set<Extension> getExtensionsFromCustomFolder() {
        FileTree customDir = project.fileTree(HYBRIS_BIN_DIR + CUSTOM_DIR);

        System.out.println(project.getProjectDir().toPath());

        List<Extension> customExtensions = getFromDir(customDir, ExtensionType.CUSTOM);

        return Set.copyOf(customExtensions);
    }

    /**
     * Gets the extensions from the project dependecies in "hybrisPlatform"
     * configuration.
     *
     * @return found extensions
     */

    public Set<Extension> getExtensionsFromHybrisPlatformDependencies() {
        Set<Extension> extensions = new HashSet<>();

        Set<File> hybrisZipFiles = project.getConfigurations().getByName(HYBRIS_PLATFORM_CONFIGURATION).getFiles();
        for (File zipFile : hybrisZipFiles) {
            extensions.addAll(getFromHybrisPlatformDependency(zipFile));
        }

        return extensions;
    }

    /**
     * Gets the platform extension. Note that it does not have a real location in
     * its directory property.
     *
     * @return the platform extension
     */
    public Extension getPlatfromExtension() {
        return new Extension("platform", Path.of("hybris", "bin", "platform"), "platform", ExtensionType.SAP_PLATFORM,
                Collections.emptyList());
    }

    private List<Extension> getFromDir(FileTree dir, ExtensionType extensionType) {
        PatternSet extInfoPattern = new PatternSet();
        extInfoPattern.include("**/extensioninfo.xml");
        extInfoPattern.exclude("**/bin/platform/**");
        Set<File> files = dir.matching(extInfoPattern).getFiles();

        return files.stream()
                .map(f -> ExtensionXmlUtil.loadExtensionFromExtensioninfoXml(f, HYBRIS_BIN_DIR, extensionType))
                .collect(Collectors.toList());
    }

    private List<Extension> getFromHybrisPlatformDependency(File zipFile) {
        FileTree zip = project.zipTree(zipFile);
        return getFromDir(zip, ExtensionType.SAP_MODULE);
    }
}
