package mpern.sap.commerce.build;

import mpern.sap.commerce.build.rules.HybrisAntRule;
import mpern.sap.commerce.build.tasks.GlobClean;
import mpern.sap.commerce.build.tasks.HybrisAntTask;
import mpern.sap.commerce.build.tasks.SupportPortalDownload;
import mpern.sap.commerce.build.util.Version;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.TaskCollection;
import org.gradle.api.tasks.TaskExecutionException;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;

public class HybrisPlugin implements Plugin<Project> {

    public static final String HYBRIS_EXTENSION = "hybris";
    public static final String HYBRIS_BOOTSTRAP = "Hybris Platform Bootstrap";

    @Override
    public void apply(Project project) {
        HybrisPluginExtension extension = project.getExtensions().create(HYBRIS_EXTENSION, HybrisPluginExtension.class, project);

        extension.getCleanGlob().set("glob:**hybris/bin/{ext-**,platform**}");

        extension.getBootstrapInclude().set(project.provider(() -> Arrays.asList("hybris/**")));
        //this folder contains some utf-8 filenames that lead to issues on linux
        extension.getBootstrapExclude().set(project.provider(() -> Arrays.asList("hybris/bin/ext-content/npmancillary/resources/npm/node_modules/http-server/node_modules/ecstatic/test/**")));

        final Configuration hybrisPlatform = project.getConfigurations().create("hybrisPlatform")
                .setDescription("Hybris Platform Dependencies. Expects zip files that are unpacked into the project root folder");

        final Configuration dbDrivers = project.getConfigurations().create("dbDriver")
                .setDescription("JDBC Drivers. Automatically downloaded and configured during bootstrap");
        hybrisPlatform.defaultDependencies(dependencies -> {
            String v = extension.getVersion().get();
            Version version = Version.parseVersion(v);
            if (version.getPatch() == Integer.MAX_VALUE) {
                if (!v.endsWith(".")) {
                    v += ".";
                }
                v += "+";
            }
            dependencies.add(project.getDependencies().create("de.hybris.platform:hybris-commerce-suite:" + v + "@zip"));
        });

        Task bootstrap = project.task("bootstrapPlatform");
        bootstrap.setGroup(HYBRIS_BOOTSTRAP);
        bootstrap.setDescription("Bootstraps the configured hybris distribution with the configured DB drivers");

        File hybrisBin = project.file("hybris/bin");

        Task cleanPlatform = project.getTasks().create("cleanPlatform", GlobClean.class, t -> {
            t.getBaseFolder().set(hybrisBin);
            t.getGlob().set(extension.getCleanGlob());
        });
        cleanPlatform.setGroup(HYBRIS_BOOTSTRAP);
        cleanPlatform.setDescription("Cleans all hybris platform artifacts");

        Task cleanOnVersionChange = project.getTasks().create("cleanPlatformIfVersionChanged", GlobClean.class, t -> {
            t.getBaseFolder().set(hybrisBin);
            t.getGlob().set(extension.getCleanGlob());
            t.onlyIf(o -> versionMismatch(extension, t.getLogger()));
        });

        Task unpackPlatform = project.getTasks().create("unpackPlatform", t -> {
            t.onlyIf(o -> versionMismatch(extension, t.getLogger()));
        });
        unpackPlatform.mustRunAfter(cleanOnVersionChange);

        project.afterEvaluate(p -> unpackPlatform.doLast(t -> project.copy(c -> {
            c.from(project.provider(() -> hybrisPlatform.getFiles().stream().map(project::zipTree).collect(Collectors.toSet())));
            c.into(t.getProject().getProjectDir());
            c.include(extension.getBootstrapInclude().get());
            c.exclude(extension.getBootstrapExclude().get());
        })));

        Task setupDBDriver = project.getTasks().create("setupDbDriver", Copy.class, t -> {
            File driverDir = t.getProject().file("hybris/bin/platform/lib/dbdriver");
            t.from(dbDrivers);
            t.into(driverDir);
        });
        setupDBDriver.mustRunAfter(unpackPlatform);

        Task touchDbDriverLastUpdate = project.getTasks().create("touchLastUpdate", t -> t.doLast(a -> {
                    File driverDir = a.getProject().file("hybris/bin/platform/lib/dbdriver");
                    File lastUpdate = new File(driverDir, ".lastupdate");
                    try {
                        driverDir.mkdirs();
                        lastUpdate.createNewFile();
                        lastUpdate.setLastModified(new Date().getTime());
                    } catch (IOException e) {
                        throw new TaskExecutionException(a, e);
                    }
                }
        ));
        touchDbDriverLastUpdate.mustRunAfter(unpackPlatform, setupDBDriver);

        bootstrap.dependsOn(cleanOnVersionChange, unpackPlatform, setupDBDriver, touchDbDriverLastUpdate);

        project.getTasks().addRule(new HybrisAntRule(project));

        //sensible defaults
        Task yclean = project.getTasks().getByPath("yclean");
        Task ybuild = project.getTasks().getByPath("ybuild");
        Task yall = project.getTasks().getByPath("yall");
        Task ycustomize = project.getTasks().getByPath("ycustomize");
        Task yproduction = project.getTasks().getByPath("yproduction");

        ybuild.mustRunAfter(yclean, ycustomize);
        yall.mustRunAfter(yclean, ycustomize);
        yproduction.mustRunAfter(ybuild, yall);

        HybrisAntTask createConfigFolder = project.getTasks().create("createDefaultConfig", HybrisAntTask.class, configTask -> configTask.systemProperty("input.template", "develop"));
        createConfigFolder.onlyIf(t -> {
            boolean configPresent = project.file("hybris/config").exists();
            if (configPresent) {
                t.getLogger().lifecycle("hybris/config folder found, nothing to do");
            }
            return !configPresent;
        });
        createConfigFolder.setGroup(HYBRIS_BOOTSTRAP);
        createConfigFolder.setDescription("Launches hybris build to create the hybris config folder, if no config folder is present");
        createConfigFolder.mustRunAfter(bootstrap);

        project.getGradle().getTaskGraph().addTaskExecutionListener(new HybrisAntTask.HybrisAntConfigureAdapter());

        SupportPortalDownload.ConfigureSupportPortalDownload configureSupportPortalDownload = new SupportPortalDownload.ConfigureSupportPortalDownload();
        project.getGradle().getTaskGraph().addTaskExecutionGraphListener(configureSupportPortalDownload);

        project.afterEvaluate(p -> {
            TaskCollection<Task> matching = p.getTasks().matching(t -> t instanceof SupportPortalDownload);
            unpackPlatform.dependsOn(matching);
        });

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
        boolean nearMatch = current.equalsIgnorePatch(required) && required.getPatch() == Integer.MAX_VALUE;

        if (nearMatch) {
            logger.lifecycle("current version {}; required version: {} -> {}", current, required, "NEAR MATCH");
            logger.lifecycle("required version does not specify patch level; treating as equal");
        } else {
            logger.lifecycle("current version: {}; required version: {} -> {}", current, required, current.equals(required) ? "MATCH" : "MISMATCH");
        }
        return !(exactMatch || nearMatch);
    }

}
