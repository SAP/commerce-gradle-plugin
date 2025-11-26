package mpern.sap.commerce.build.tasks;

import static mpern.sap.commerce.build.HybrisPlugin.*;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.file.*;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.TaskAction;

import mpern.sap.commerce.build.HybrisPluginExtension;
import mpern.sap.commerce.build.extensioninfo.ExtensionInfoLoader;
import mpern.sap.commerce.build.util.Extension;
import mpern.sap.commerce.build.util.ExtensionType;

/**
 * Task implementation to unpack the platform in sparse mode.
 */
public class UnpackPlatformSparseTask extends DefaultTask {

    private final File projecDir;
    private final HybrisPluginExtension hybrisPluginExtension;
    private final FileSystemOperations fileSystemOperations;
    private final FileCollection hybrisDependencies;
    private final ArchiveOperations archiveOperations;
    private final ExtensionInfoLoader extensionInfoLoader;

    @Inject
    public UnpackPlatformSparseTask(FileSystemOperations fileSystemOperations, ArchiveOperations archiveOperations,
            ObjectFactory objectFactory) {
        this.projecDir = getProject().getProjectDir();
        this.hybrisPluginExtension = (HybrisPluginExtension) getProject().getExtensions().getByName(HYBRIS_EXTENSION);
        this.fileSystemOperations = fileSystemOperations;
        this.archiveOperations = archiveOperations;
        this.hybrisDependencies = getProject().getConfigurations().getByName(HYBRIS_PLATFORM_CONFIGURATION);

        this.extensionInfoLoader = objectFactory.newInstance(ExtensionInfoLoader.class, hybrisPluginExtension,
                hybrisDependencies);
    }

    @TaskAction
    public void unpack() {
        getLogger().lifecycle("Unpacking platform in sparse mode");

        // Phase 1: gather information about all known extensions and their direct
        // dependencies
        Map<String, Extension> allKnownExtensions = getAllKnownExtensions(extensionInfoLoader);

        // Phase 2: build the list of all needed hybris dependencies to be present
        Map<String, Extension> allNeededExtensions = getAllNeededExtensions(extensionInfoLoader, allKnownExtensions);

        // Phase 3: build the list of extensions already present in the project
        // hybris/bin folder
        Map<String, Extension> alreadyExistingExtensions = getAlreadyExistingExtensions(extensionInfoLoader);

        // Phase 4: find the missing extensions, by removing from the needed list the
        // present ones
        Map<String, Extension> missingExtensions = getMissingHybrisExtensions(allNeededExtensions,
                alreadyExistingExtensions);

        if (missingExtensions.isEmpty()) {
            getLogger().lifecycle("No missing SAP Commerce extensions, nothing to unpack");
            return;
        }

        // Phase 5: extract from the dependencies zips the missing extensions
        copyMissingExtensions(missingExtensions);
    }

    private Map<String, Extension> getAllKnownExtensions(ExtensionInfoLoader extensionInfoLoader) {
        Map<String, Extension> customExtensions = extensionInfoLoader.getExtensionsFromCustomFolder();
        Map<String, Extension> hybrisDependenciesExtensions = extensionInfoLoader
                .getExtensionsFromHybrisPlatformDependencies();
        Extension platformExtension = extensionInfoLoader.getPlatfromExtension();

        Map<String, Extension> result = new HashMap<>(
                customExtensions.size() + hybrisDependenciesExtensions.size() + 1);
        result.putAll(customExtensions);
        result.putAll(hybrisDependenciesExtensions);
        result.put(platformExtension.name, platformExtension);

        return result;
    }

    private Map<String, Extension> getAllNeededExtensions(ExtensionInfoLoader extensionInfoLoader,
            Map<String, Extension> allKnownExtensions) {
        return extensionInfoLoader.loadAllNeededExtensions(allKnownExtensions);
    }

    private Map<String, Extension> getAlreadyExistingExtensions(ExtensionInfoLoader extensionInfoLoader) {
        return extensionInfoLoader.loadAlreadyExistingExtensions();
    }

    private Map<String, Extension> getMissingHybrisExtensions(Map<String, Extension> allNeededExtensions,
            Map<String, Extension> alreadyExistingExtensions) {

        Map<String, Extension> allMissingExtensions = allNeededExtensions.entrySet().stream()
                .filter(entry -> !alreadyExistingExtensions.containsKey(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        /*
         * make sure we do not have missing custom extensions (should not be possible,
         * as it would fail when loading the needed extensions)
         */
        List<String> allMissingCustomExtensions = allMissingExtensions.values().stream()
                .filter(ext -> ext.extensionType == ExtensionType.CUSTOM).map(ext -> ext.name).toList();
        if (!allMissingCustomExtensions.isEmpty()) {
            getLogger().lifecycle("Some custom extensions are missing: {}, aborting", allMissingCustomExtensions);
            throw new InvalidUserDataException(
                    String.format("Some custom extensions are missing: %s", allMissingCustomExtensions));
        }

        return allMissingExtensions;
    }

    private void copyMissingExtensions(Map<String, Extension> missingExtensions) {
        if (missingExtensions.isEmpty()) {
            getLogger().lifecycle("All extensions are present, nothing to copy");
            return;
        }

        getLogger().lifecycle("Some needed SAP Commerce Suite extensions are missing, copying them");

        /*
         * Take the project dependencies, search the missing extensions and copy them
         * into the project.
         */
        Set<File> dependencies = hybrisDependencies.getFiles();
        for (File dependency : dependencies) {
            FileTree zip = archiveOperations.zipTree(dependency);
            getLogger().lifecycle("Copying missing extensions from project dependency {}", dependency.getName());
            fileSystemOperations.copy(c -> {
                c.from(zip);
                c.into(projecDir);
                c.include(getDependencyCopyIncludes(missingExtensions));
                c.exclude(hybrisPluginExtension.getBootstrapExclude().get());
            });
            getLogger().lifecycle("Copied missing extensions from project dependency {}", dependency.getName());
        }
    }

    private String[] getDependencyCopyIncludes(Map<String, Extension> missingExtensions) {
        List<String> missingExtensionsNames = new ArrayList<>();
        for (Map.Entry<String, Extension> missingExtensionEntry : missingExtensions.entrySet()) {
            getLogger().info("Adding extension {} - {} to the copy includes", missingExtensionEntry.getKey(),
                    missingExtensionEntry.getValue().relativeLocation);
            missingExtensionsNames.add(HYBRIS_BIN_DIR + missingExtensionEntry.getValue().relativeLocation + "/");
        }

        return missingExtensionsNames.toArray(new String[0]);
    }
}
