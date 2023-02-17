package mpern.sap.commerce.build.extensioninfo;

import static mpern.sap.commerce.build.HybrisPlugin.*;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import org.gradle.api.Project;
import org.gradle.api.file.FileTree;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.util.PatternSet;

import mpern.sap.commerce.build.HybrisPluginExtension;
import mpern.sap.commerce.build.util.Extension;
import mpern.sap.commerce.build.util.ExtensionType;
import mpern.sap.commerce.build.util.Stopwatch;

/**
 * Responsible for the creation of
 * {@link mpern.sap.commerce.build.util.Extension} DTOs for the core build
 * plugin.
 */
public class ExtensionInfoLoader {

    private static final String CUSTOM_DIR = "custom";

    private static final Set<String> PLATFORM_INNER_EXT_NAMES = new HashSet<>(
            Arrays.asList("advancedsavedquery", "catalog", "comments", "commons", "core", "deliveryzone", "europe1",
                    "hac", "impex", "maintenanceweb", "mediaweb", "oauth2", "paymentstandard", "platformservices",
                    "processing", "scripting", "testweb", "validation", "workflow"));

    private static final Logger LOG = Logging.getLogger(ExtensionInfoLoader.class);

    private final Project project;

    public ExtensionInfoLoader(Project project) {
        this.project = project;
    }

    /**
     * Gets the extensions from the Hybris project custom folder.
     *
     * @return found extensions
     */
    public Map<String, Extension> getExtensionsFromCustomFolder() {
        Stopwatch stopwatch = new Stopwatch();

        FileTree customDir = project.fileTree(HYBRIS_BIN_DIR + CUSTOM_DIR);
        Map<String, Extension> result = getFromDir(customDir, ExtensionType.CUSTOM);

        LOG.info("Loaded extensions information from project custom folder in {} ms", stopwatch.stop());

        return result;
    }

    /**
     * Gets the extensions from the project dependecies in "hybrisPlatform"
     * configuration.
     *
     * @return found extensions
     */
    public Map<String, Extension> getExtensionsFromHybrisPlatformDependencies() {
        Stopwatch stopwatch = new Stopwatch();

        Map<String, Extension> extensions = new HashMap<>();

        Set<File> hybrisZipFiles = project.getConfigurations().getByName(HYBRIS_PLATFORM_CONFIGURATION).getFiles();
        for (File zipFile : hybrisZipFiles) {
            extensions.putAll(getFromHybrisPlatformDependency(zipFile));
        }

        LOG.info("Loaded extensions information from hybrisPlatform dependencies in {} ms", stopwatch.stop());

        return extensions;
    }

    /**
     * Gets the platform extension. Note that it does not have a real location in
     * its directory property.
     *
     * @return the platform extension
     */
    public Extension getPlatfromExtension() {
        Path platformPath = FileSystems.getDefault().getPath("hybris/bin/platform");
        return new Extension(PLATFORM_NAME, platformPath, "platform", ExtensionType.SAP_PLATFORM,
                Collections.emptyList());
    }

    /**
     * Loads all the extensions needed, based on the localextensions.xml.
     *
     * @param allKnownExtensions all extensions known
     * @return the needed extensions
     */
    public Map<String, Extension> loadAllNeededExtensions(Map<String, Extension> allKnownExtensions) {
        Stopwatch stopwatch = new Stopwatch();

        Map<String, Extension> allNeededExtensions = new HashMap<>();

        Extension platform = allKnownExtensions.get(PLATFORM_NAME);
        if (platform == null) {
            throw new ExtensionInfoException("Platform extension not found");
        }
        allNeededExtensions.put(PLATFORM_NAME, platform);

        File localExtensionsXmlFile = project.file("hybris/config/localextensions.xml");
        if (!localExtensionsXmlFile.exists()) {
            throw new ExtensionInfoException(
                    "localextensions.xml file not found at " + localExtensionsXmlFile.getPath());
        }
        Set<String> declaredExtNames = ExtensionXmlUtil
                .loadExtensionNamesFromLocalExtensionsXML(localExtensionsXmlFile);
        for (String declaredExtName : declaredExtNames) {
            addExtensionAndAllDepedencies(declaredExtName, allNeededExtensions, allKnownExtensions);
        }

        // add alwaysIncluded extensions
        HybrisPluginExtension hybrisPluginExtension = (HybrisPluginExtension) project.getExtensions()
                .getByName(HYBRIS_EXTENSION);
        Set<String> alwaysIncluded = hybrisPluginExtension.getSparseBootstrap().getAlwaysIncluded();
        for (String alwaysIncludedExtName : alwaysIncluded) {
            addExtensionAndAllDepedencies(alwaysIncludedExtName, allNeededExtensions, allKnownExtensions);
        }

        LOG.info("Loaded all needed extensions from localextensions.xml in {} ms", stopwatch.stop());

        return allNeededExtensions;
    }

    /**
     * Loads all the extensions already existing in the project folder.
     *
     * @return the existing extensions
     */
    public Map<String, Extension> loadAlreadyExistingExtensions() {
        Stopwatch stopwatch = new Stopwatch();

        FileTree binDir = project.fileTree(HYBRIS_BIN_DIR);
        Map<String, Extension> existingExtensions = getFromDir(binDir, ExtensionType.RUNTIME_INSTALLED);

        /*
         * add platform if ext/core folder exists (other tasks may copy in platform, so
         * only platform check is not enough)
         */
        if (project.file(HYBRIS_BIN_DIR + "platform/ext/core").exists()) {
            Extension platformExt = getPlatfromExtension();
            existingExtensions.put(platformExt.name, platformExt);
        }

        LOG.info("Loaded existing extensions information from project folder in {} ms", stopwatch.stop());

        return existingExtensions;
    }

    private void addExtensionAndAllDepedencies(String extName, Map<String, Extension> allNeededExtensions,
            Map<String, Extension> allKnownExtensions) {

        if (isPlatformInnerExtension(extName)) {
            // protect from platform extensions declared by mistake
            return;
        }

        Extension extension = checkedGetFromKnownExtensions(extName, allKnownExtensions);
        allNeededExtensions.put(extName, extension);

        for (String requiredExtName : extension.requiredExtensions) {
            addExtensionAndAllDepedencies(requiredExtName, allNeededExtensions, allKnownExtensions);
        }
    }

    private Extension checkedGetFromKnownExtensions(String extName, Map<String, Extension> allKnownExtensions) {
        Extension extension = allKnownExtensions.get(extName);
        if (extension == null) {
            throw new ExtensionInfoException(
                    String.format("Extension %s needed, but not found in known extensions!", extName));
        }
        return extension;
    }

    private Map<String, Extension> getFromDir(FileTree dir, ExtensionType extensionType) {
        PatternSet extInfoPattern = new PatternSet();
        extInfoPattern.include("**/extensioninfo.xml");
        extInfoPattern.exclude("**/bin/platform/**");
        extInfoPattern.exclude("/platform/**");
        Set<File> files = dir.matching(extInfoPattern).getFiles();

        return files.stream()
                .map(f -> ExtensionXmlUtil.loadExtensionFromExtensioninfoXml(f, HYBRIS_BIN_DIR, extensionType))
                .collect(Collectors.toMap(k -> k.name, v -> v));
    }

    private Map<String, Extension> getFromHybrisPlatformDependency(File zipFile) {
        FileTree zip = project.zipTree(zipFile);
        return getFromDir(zip, ExtensionType.SAP_MODULE);
    }

    private boolean isPlatformInnerExtension(String extName) {
        return PLATFORM_INNER_EXT_NAMES.contains(extName);
    }
}
