package mpern.sap.commerce.build.tasks;

import java.util.HashMap;
import java.util.Map;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import mpern.sap.commerce.build.extensioninfo.ExtensionInfoLoader;
import mpern.sap.commerce.build.util.Extension;

/**
 * Task implementation to unpack the platform in sparse mode.
 */
public class UnpackPlatformSparseTask extends DefaultTask {

    @TaskAction
    public void unpack() {
        getLogger().lifecycle("Unpack platform sparse action");
        getLogger().warn("Unpack platform sparse action");

        ExtensionInfoLoader extensionInfoLoader = new ExtensionInfoLoader(getProject());

        // Phase 1: gather information about all known extensions and their direct
        // dependencies
        Map<String, Extension> allKnownExtensions = getAllKnownExtensions(extensionInfoLoader);

        // Phase 2: build the list of needed hybris dependencies to be present
        Map<String, Extension> neededExtensions = getNeededExtensions(extensionInfoLoader, allKnownExtensions);

        // Phase 3: build the list of extensions already present in the project
        // hybris/bin folder
        Map<String, Extension> alreadyExistingExtensions = getAlreadyExistingExtensions(extensionInfoLoader);

        // Phase 4: find the missing extensions, by removing from the needed list the
        // present ones

        // Phase 5: extract from the dependencies zips the missing extensions
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

    private Map<String, Extension> getNeededExtensions(ExtensionInfoLoader extensionInfoLoader,
            Map<String, Extension> allKnownExtensions) {
        return extensionInfoLoader.loadAllNeededExtensions(allKnownExtensions);
    }

    private Map<String, Extension> getAlreadyExistingExtensions(ExtensionInfoLoader extensionInfoLoader) {
        return null;
    }
}
