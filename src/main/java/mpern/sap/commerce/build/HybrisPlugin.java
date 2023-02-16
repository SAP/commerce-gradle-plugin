package mpern.sap.commerce.build;

import static mpern.sap.commerce.ccv2.CloudV2Plugin.CCV2_EXTENSION;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.tools.ant.DirectoryScanner;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.UnknownDomainObjectException;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.TaskExecutionException;
import org.gradle.api.tasks.TaskProvider;

import mpern.sap.commerce.build.rules.HybrisAntRule;
import mpern.sap.commerce.build.tasks.GlobClean;
import mpern.sap.commerce.build.tasks.HybrisAntTask;
import mpern.sap.commerce.build.tasks.UnpackPlatformSparseTask;
import mpern.sap.commerce.build.util.Extension;
import mpern.sap.commerce.build.util.PlatformResolver;
import mpern.sap.commerce.build.util.Version;

public class HybrisPlugin implements Plugin<Project> {

    public static final String HYBRIS_EXTENSION = "hybris";
    public static final String HYBRIS_BOOTSTRAP = "SAP Commerce Bootstrap";
    public static final String HYBRIS_PLATFORM_CONFIGURATION = "hybrisPlatform";
    public static final String HYBRIS_BIN_DIR = "hybris/bin/";
    public static final String PLATFORM_NAME = "platform";

    private static boolean isDirEmpty(final Path directory) {
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory)) {
            return !dirStream.iterator().hasNext();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void apply(Project project) {
        HybrisPluginExtension extension = project.getExtensions().create(HYBRIS_EXTENSION, HybrisPluginExtension.class,
                project);

        extension.getCleanGlob().set("glob:**hybris/bin/{ext-**,platform**,modules**}");

        extension.getBootstrapInclude().set(project.provider(() -> Arrays.asList("hybris/**")));
        // this folder contains some utf-8 filenames that lead to issues on linux
        extension.getBootstrapExclude().set(project.provider(() -> Arrays.asList(
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

        Task bootstrap = project.task("bootstrapPlatform");
        bootstrap.setGroup(HYBRIS_BOOTSTRAP);
        bootstrap.setDescription("Bootstraps the configured hybris distribution with the configured DB drivers");

        File hybrisBin = project.file("hybris/bin");

        project.getTasks().register("cleanPlatform", GlobClean.class, t -> {
            t.setGroup(HYBRIS_BOOTSTRAP);
            t.setDescription("Cleans all hybris platform artifacts");

            t.getBaseFolder().set(hybrisBin);
            t.getGlob().set(extension.getCleanGlob());
        });

        TaskProvider<GlobClean> cleanOnVersionChange = project.getTasks().register("cleanPlatformIfVersionChanged",
                GlobClean.class, t -> {
                    t.getBaseFolder().set(hybrisBin);
                    t.getGlob().set(extension.getCleanGlob());
                    t.onlyIf(o -> versionMismatch(extension, t.getLogger()));
                });

        TaskProvider<Task> unpackPlatform = project.getTasks().register("unpackPlatform", t -> {
            t.onlyIf(o -> versionMismatch(extension, t.getLogger()));
            t.onlyIf(o -> !isSparseEnabled(extension, t.getLogger()));
            t.mustRunAfter(cleanOnVersionChange);
        });

        TaskProvider<UnpackPlatformSparseTask> unpackPlatformSparse = project.getTasks()
                .register("unpackPlatformSparse", UnpackPlatformSparseTask.class, t -> {
                    t.onlyIf(o -> isSparseEnabled(extension, t.getLogger()));
                    t.mustRunAfter(cleanOnVersionChange);
                });

        project.afterEvaluate(p -> unpackPlatform.get().doLast(t -> project.copy(c -> {
            c.from(project.provider(
                    () -> hybrisPlatform.getFiles().stream().map(project::zipTree).collect(Collectors.toList())));
            c.into(t.getProject().getProjectDir());
            c.include(extension.getBootstrapInclude().get());
            c.exclude(extension.getBootstrapExclude().get());
            c.setDuplicatesStrategy(DuplicatesStrategy.WARN);
        })));

        TaskProvider<Task> setupDBDriver = project.getTasks().register("setupDbDriver", t -> {
            t.mustRunAfter(unpackPlatform);
            t.doLast(l -> project.copy(c -> {
                File driverDir = t.getProject().file("hybris/bin/platform/lib/dbdriver");
                c.from(dbDrivers);
                c.into(driverDir);
                c.setDuplicatesStrategy(DuplicatesStrategy.WARN);
            }));
        });

        TaskProvider<Task> touchDbDriverLastUpdate = project.getTasks().register("touchLastUpdate", t -> {
            t.mustRunAfter(unpackPlatform, setupDBDriver);
            t.doLast(a -> {
                Path driverDir = a.getProject().file("hybris/bin/platform/lib/dbdriver").toPath();
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

        bootstrap.dependsOn(cleanOnVersionChange, unpackPlatform, unpackPlatformSparse, setupDBDriver,
                touchDbDriverLastUpdate);

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

        TaskProvider<HybrisAntTask> createConfigFolder = project.getTasks().register("createDefaultConfig",
                HybrisAntTask.class, t -> {
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

        project.getTasks().register("removeUnusedExtensions", t -> {
            t.setGroup(HYBRIS_BOOTSTRAP);
            t.setDescription("Remove unused extensions; helps save disk space if you work on multiple projects");

            t.doFirst(a -> {
                for (String defaultExclude : DirectoryScanner.getDefaultExcludes()) {
                    DirectoryScanner.removeDefaultExclude(defaultExclude);
                }
            });
            t.doLast(a -> {
                PlatformResolver resolver = new PlatformResolver(
                        extension.getPlatform().getPlatformHome().get().getAsFile().toPath());
                List<Extension> configured;
                try {
                    configured = resolver.getConfiguredExtensions();
                } catch (Exception e) {
                    return;
                }
                if (configured.isEmpty()) {
                    return;
                }
                Path bin = project.file("hybris/bin").toPath();
                ConfigurableFileTree files = project.fileTree(bin);
                files.exclude("platform/");
                files.exclude("custom/");
                // @formatter:off
                configured.stream()
                        .map(e -> e.directory)
                        .map(bin::relativize)
                        .map(p -> p + "/")
                        .filter(p -> !p.startsWith("platform/"))
                        .filter(p -> !p.startsWith("custom/"))
                        .forEach(files::exclude);
                // @formatter:on
                project.delete(files);
                try {
                    Files.walkFileTree(bin, new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                            if (isDirEmpty(dir)) {
                                Files.delete(dir);
                            }
                            return FileVisitResult.CONTINUE;
                        }
                    });
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                DirectoryScanner.resetDefaultExcludes();
            });
        });
        project.getGradle().getTaskGraph().addTaskExecutionListener(new HybrisAntTask.HybrisAntConfigureAdapter());
    }

    private boolean versionMismatch(HybrisPluginExtension extension, Logger logger) {
        Version current;
        try {
            current = Version.parseVersion(extension.getPlatform().getVersion().get());
        } catch (IllegalArgumentException e) {
            current = Version.UNDEFINED;
        }
        Version required;

        try {
            required = Version.parseVersion(extension.getVersion().get());
        } catch (IllegalArgumentException e) {
            required = Version.UNDEFINED;
        }

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
        boolean sparseEnabled = extension.getSparseBootstrap().getEnabled();
        logger.lifecycle("hybris.sparseBootstrap.enabled is {}", sparseEnabled);
        return sparseEnabled;
    }
}
