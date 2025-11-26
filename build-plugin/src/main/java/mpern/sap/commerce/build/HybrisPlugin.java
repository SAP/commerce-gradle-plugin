package mpern.sap.commerce.build;

import static mpern.sap.commerce.commons.Constants.CCV2_EXTENSION;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.gradle.api.*;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.*;
import org.gradle.api.logging.Logger;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskExecutionException;
import org.gradle.api.tasks.TaskProvider;

import mpern.sap.commerce.build.rules.HybrisAntRule;
import mpern.sap.commerce.build.tasks.GlobClean;
import mpern.sap.commerce.build.tasks.HybrisAntTask;
import mpern.sap.commerce.build.tasks.UnpackPlatformSparseTask;
import mpern.sap.commerce.build.util.Version;

public class HybrisPlugin implements Plugin<Project> {

    public static final String HYBRIS_EXTENSION = "hybris";
    public static final String HYBRIS_BOOTSTRAP = "SAP Commerce Bootstrap";
    public static final String HYBRIS_PLATFORM_CONFIGURATION = "hybrisPlatform";
    public static final String HYBRIS_BIN_DIR = "hybris/bin/";
    public static final String PLATFORM_NAME = "platform";

    private final ProjectLayout layout;
    private final FileSystemOperations fileSystemOperations;
    private final ProviderFactory providerFactory;
    private final ArchiveOperations archiveOperations;

    @Inject
    public HybrisPlugin(ProjectLayout layout, FileSystemOperations fileSystemOperations,
            ArchiveOperations archiveOperations, ProviderFactory providerFactory) {
        this.layout = layout;
        this.fileSystemOperations = fileSystemOperations;
        this.providerFactory = providerFactory;
        this.archiveOperations = archiveOperations;
    }

    private static boolean isDirEmpty(final Path directory) {
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory)) {
            return !dirStream.iterator().hasNext();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void apply(Project project) {
        HybrisPluginExtension extension = project.getExtensions().create(HYBRIS_EXTENSION, HybrisPluginExtension.class);
        extension.getSparseBootstrap().getEnabled().convention(false);
        extension.getSparseBootstrap().getAlwaysIncluded().convention(Collections.emptySet());

        extension.getCleanGlob().convention("glob:**hybris/bin/{ext-**,platform**,modules**}");

        extension.getBootstrapInclude().convention(project.provider(() -> List.of("hybris/**")));
        // this folder contains some utf-8 filenames that lead to issues on linux
        extension.getBootstrapExclude().convention(project.provider(() -> List.of(
                "hybris/bin/ext-content/npmancillary/resources/npm/node_modules/http-server/node_modules/ecstatic/test/**")));

        final Configuration hybrisPlatform = project.getConfigurations().create(HYBRIS_PLATFORM_CONFIGURATION)
                .setDescription(
                        "Hybris Platform Dependencies. Expects zip files that are unpacked into the project root folder");

        final Configuration dbDrivers = project.getConfigurations().create("dbDriver")
                .setDescription("JDBC Drivers. Automatically downloaded and configured during bootstrap");
        hybrisPlatform.defaultDependencies(dependencies -> {
            boolean ccv2Plugin = false;
            try {
                project.getExtensions().getByName(CCV2_EXTENSION);
                ccv2Plugin = true;
            } catch (UnknownDomainObjectException e) {
                // ignore
            }
            if (ccv2Plugin) {
                return;
            }
            String v = extension.getVersion().get();
            Version version = Version.parseVersion(v);
            dependencies.add(project.getDependencies()
                    .create("de.hybris.platform:hybris-commerce-suite:" + version.getDependencyVersion() + "@zip"));

            // optional, add intExtPack if defined
            v = extension.getIntExtPackVersion().get();
            if (v.length() > 0) {
                version = Version.parseVersion(v);
                dependencies.add(project.getDependencies().create(
                        "de.hybris.platform:hybris-commerce-integrations:" + version.getDependencyVersion() + "@zip"));
            }
        });

        TaskProvider<?> bootstrap = project.getTasks().register("bootstrapPlatform", t -> {
            t.setGroup(HYBRIS_BOOTSTRAP);
            t.setDescription("Bootstraps the configured hybris distribution with the configured DB drivers");
        });

        File hybrisBin = project.file("hybris/bin");

        project.getTasks().register("cleanPlatform", GlobClean.class, t -> {
            t.setGroup(HYBRIS_BOOTSTRAP);
            t.setDescription("Cleans all hybris platform artifacts");

            t.getBaseFolder().set(hybrisBin.getAbsolutePath());
            t.getGlob().set(extension.getCleanGlob());
        });

        TaskProvider<GlobClean> cleanOnVersionChange = project.getTasks().register("cleanPlatformIfVersionChanged",
                GlobClean.class, t -> {
                    t.getBaseFolder().set(hybrisBin.getAbsolutePath());
                    t.getGlob().set(extension.getCleanGlob());
                    t.onlyIf(o -> versionMismatch(extension, t.getLogger()));
                });

        FileCollection hybrisPlafomCollecion = hybrisPlatform;
        TaskProvider<Task> unpackPlatform = project.getTasks().register("unpackPlatform", t -> {
            t.onlyIf(o -> versionMismatch(extension, t.getLogger()));
            t.onlyIf(o -> !isSparseEnabled(extension, t.getLogger()));
            t.mustRunAfter(cleanOnVersionChange);
            t.doLast(a -> fileSystemOperations.copy(c -> {
                c.from(providerFactory.provider(
                        () -> hybrisPlafomCollecion.getFiles().stream().map(archiveOperations::zipTree).toList()));
                c.into(layout.getProjectDirectory());
                c.include(extension.getBootstrapInclude().get());
                c.exclude(extension.getBootstrapExclude().get());
                c.setDuplicatesStrategy(DuplicatesStrategy.WARN);
            }));
        });

        TaskProvider<UnpackPlatformSparseTask> unpackPlatformSparse = project.getTasks()
                .register("unpackPlatformSparse", UnpackPlatformSparseTask.class, t -> {
                    t.onlyIf(o -> isSparseEnabled(extension, t.getLogger()));
                    t.mustRunAfter(cleanOnVersionChange);
                });

        FileCollection dbDriversCollection = dbDrivers;
        TaskProvider<Task> setupDBDriver = project.getTasks().register("setupDbDriver", t -> {
            t.mustRunAfter(unpackPlatform, unpackPlatformSparse);
            t.doLast(l -> fileSystemOperations.copy(c -> {
                File driverDir = layout.getProjectDirectory().file("hybris/bin/platform/lib/dbdriver").getAsFile();
                c.from(dbDriversCollection);
                c.into(driverDir);
                c.setDuplicatesStrategy(DuplicatesStrategy.WARN);
            }));
        });

        TaskProvider<Task> touchDbDriverLastUpdate = project.getTasks().register("touchLastUpdate", t -> {
            t.mustRunAfter(unpackPlatform, unpackPlatformSparse, setupDBDriver);
            t.doLast(a -> {
                Path driverDir = layout.getProjectDirectory().file("hybris/bin/platform/lib/dbdriver").getAsFile()
                        .toPath();
                Path lastUpdate = driverDir.resolve(".lastupdate");
                try {
                    Files.createDirectories(driverDir);
                    if (!Files.exists(lastUpdate)) {
                        Files.createFile(lastUpdate);
                    }
                    Files.setLastModifiedTime(lastUpdate, FileTime.from(Instant.now()));
                } catch (IOException e) {
                    throw new TaskExecutionException(a, e);
                }
            });
        });

        bootstrap.configure(t -> t.dependsOn(cleanOnVersionChange, unpackPlatform, unpackPlatformSparse, setupDBDriver,
                touchDbDriverLastUpdate));

        project.getTasks().addRule(new HybrisAntRule(project));
        // sensible defaults
        TaskProvider<Task> yclean = project.getTasks().named("yclean");
        TaskProvider<Task> ybuild = project.getTasks().named("ybuild");
        TaskProvider<Task> yall = project.getTasks().named("yall");
        TaskProvider<Task> ycustomize = project.getTasks().named("ycustomize");
        TaskProvider<Task> yproduction = project.getTasks().named("yproduction");
        ybuild.configure(t -> t.mustRunAfter(yclean, ycustomize));
        yall.configure(t -> t.mustRunAfter(yclean, ycustomize));
        yproduction.configure(t -> t.mustRunAfter(ybuild, yall));

        project.getTasks().register("createDefaultConfig", HybrisAntTask.class, t -> {
            t.setGroup(HYBRIS_BOOTSTRAP);
            t.setDescription(
                    "Launches hybris build to create the hybris config folder, if no config folder is present");
            t.mustRunAfter(bootstrap);
            t.args("createConfig");
            t.antProperty("input.template", "develop");
            t.onlyIf(s -> {
                boolean configPresent = project.file("hybris/config").exists();
                if (configPresent) {
                    t.getLogger().lifecycle("hybris/config folder found, nothing to do");
                }
                return !configPresent;
            });
        });
    }

    private boolean versionMismatch(HybrisPluginExtension extension, Logger logger) {
        Version current;
        try {
            current = Version.parseVersion(extension.getPlatform().getVersion().get(),
                    extension.getPreviewToPatchLevel().get());
        } catch (IllegalArgumentException e) {
            current = Version.UNDEFINED;
        }
        Version required = Version.parseVersion(extension.getVersion().get());

        boolean exactMatch = current.equals(required);
        boolean nearMatch = current.equalsIgnorePatch(required) && required.getPatch() == Version.UNDEFINED_PART;

        if (nearMatch) {
            logger.lifecycle("current version {}; required version: {} -> {}", current, required, "NEAR MATCH");
            logger.lifecycle("required version does not specify patch level; treating as equal");
        } else {
            logger.lifecycle("current version: {}; required version: {} -> {}", current, required,
                    current.equals(required) ? "MATCH" : "MISMATCH");
        }
        return !(exactMatch || nearMatch);
    }

    private boolean isSparseEnabled(HybrisPluginExtension extension, Logger logger) {
        boolean sparseEnabled = extension.getSparseBootstrap().getEnabled().get();
        logger.lifecycle("hybris.sparseBootstrap.enabled is {}", sparseEnabled);
        return sparseEnabled;
    }

}
