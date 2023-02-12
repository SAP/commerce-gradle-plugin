package mpern.sap.commerce.build.tasks;

import java.util.HashSet;
import java.util.Set;

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
        Set<Extension> allKnownExtensions = getAllKnownExtensions(extensionInfoLoader);

        // Phase 2: build the list of needed hybris dependencies to be present
        Set<Extension> neededExtensions = getNeededExtensions(extensionInfoLoader);

        // Phase 3: build the list of extensions already present in the project
        // hybris/bin folder
        Set<Extension> alreadyExistingExtensions = getAlreadyExistingExtensions(extensionInfoLoader);

        // Phase 4: find the missing extensions, by removing from the needed list the
        // present ones

        // Phase 5: extract from the dependencies zips the missing extensions
    }

    private Set<Extension> getAllKnownExtensions(ExtensionInfoLoader extensionInfoLoader) {
        Set<Extension> customExtensions = extensionInfoLoader.getExtensionsFromCustomFolder();
        Set<Extension> hybrisDependenciesExtensions = extensionInfoLoader.getExtensionsFromHybrisPlatformDependencies();
        Extension platformExtension = extensionInfoLoader.getPlatfromExtension();

        Set<Extension> result = new HashSet<>(customExtensions.size() + hybrisDependenciesExtensions.size() + 1);
        result.addAll(customExtensions);
        result.addAll(hybrisDependenciesExtensions);
        result.add(platformExtension);

        return result;
    }

    private Set<Extension> getNeededExtensions(ExtensionInfoLoader extensionInfoLoader) {
        return null;
    }

    private Set<Extension> getAlreadyExistingExtensions(ExtensionInfoLoader extensionInfoLoader) {
        return null;
    }
}
