package mpern.sap.commerce.ccv1;

import mpern.sap.commerce.build.HybrisPlugin;
import mpern.sap.commerce.ccv1.tasks.MergePropertyFiles;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.Delete;
import org.gradle.api.tasks.StopExecutionException;
import org.gradle.api.tasks.WriteProperties;
import org.gradle.api.tasks.bundling.Zip;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CloudServicesPackagingPlugin implements Plugin<Project> {

    private static final Logger LOG = Logging.getLogger(CloudServicesPackagingPlugin.class);

    public static final String EXTENSION = "CCV1";
    public static final String COMMON_CONFIG = "common";
    public static final String GROUP = "CCV1 Packaging";
    public static final String[] HYBRIS_CONFIG_EXCLUDE = {"**/hybrislicence.jar", "solr/**", "tomcat/**", "customer*.properties", "localextensions.xml"};
    public static final String[] DATAHUB_CONFIG_EXCLUDE = {"**/local.properties", "**/logback.xml", "customer*.properties"};
    public static final String[] SOLR_CONFIG_EXCLUDE = {};

    @Override
    public void apply(Project project) {
        PackagingExtension extension = project.getExtensions().create(EXTENSION, PackagingExtension.class, project);
        extension.getPlatformZip().set(project.file("hybris/temp/hybris/hybrisServer/hybrisServer-Platform.zip"));
        extension.getAllExtensionsZip().set(project.file("hybris/temp/hybris/hybrisServer/hybrisServer-AllExtensions.zip"));
        extension.getEnvironments().set(project.provider(() -> new HashSet<>(Arrays.asList("dev", "stag", "prod"))));
        extension.getPreProductionEnvironment().set("stag");
        extension.getProjectID().set(project.provider(project::getName));

        extension.getConfigurationFolder().set(project.file("ccv1-configuration"));
        extension.getDistributionFolder().set(project.file("dist"));
        extension.getTempFolder().set(project.file("temp"));


        Task bootstrap = project.getTasks().create("bootstrapCCV1Config");
        bootstrap.setGroup(GROUP);
        bootstrap.setDescription("Creates environment config folders, if they don't exist");
        project.afterEvaluate(p -> setupBootstrap(p, bootstrap, extension));

        Delete cleanTempFolder = project.getTasks().create("cleanTemp", Delete.class, t -> {
            t.delete(extension.getTempFolder());
        });
        cleanTempFolder.setGroup(GROUP);
        cleanTempFolder.setDescription("cleans temp folder used to assemble the final package");

        Task buildPackage = project.getTasks().create("buildCCV1Package");
        buildPackage.setGroup(GROUP);
        buildPackage.setDescription("Builds a distribution package based on Deployment Packaging Guidelines (v.2.3.3)");
        project.afterEvaluate(p -> setupPackaging(p, buildPackage, extension));

        project.getPlugins().withType(HybrisPlugin.class, hybrisPlugin -> {
            //sensible defaults
            buildPackage.mustRunAfter(project.getTasks().getByPath("yproduction"));
        });
    }

    private void setupBootstrap(Project p, Task bootstrap, PackagingExtension extension) {
        bootstrap.doLast(tsk -> {
            try {
                Path configurationFolder = extension.getConfigurationFolder().getAsFile().get().toPath();
                Set<String> environments = new HashSet<>(extension.getEnvironments().get());
                environments.add(COMMON_CONFIG);
                for (String environment : environments) {
                    Path hybrisConfigFolder = configurationFolder.resolve(environment).resolve("hybris");
                    if (!Files.exists(hybrisConfigFolder)) {
                        Files.createDirectories(hybrisConfigFolder);
                    }
                    if (extension.getDatahub().getOrElse(Boolean.FALSE)) {
                        Path datahubConfigFolder = configurationFolder.resolve(environment).resolve("datahub");
                        if (!Files.exists(datahubConfigFolder)) {
                            Files.createDirectories(datahubConfigFolder);
                        }
                    }
                    if (extension.getSolr().getOrElse(Boolean.FALSE)) {
                        Path solrConfigFolder = configurationFolder.resolve(environment).resolve("solr");
                        if (!Files.exists(solrConfigFolder)) {
                            Files.createDirectories(solrConfigFolder);
                        }
                    }
                }
            } catch (IOException e) {
                tsk.getLogger().error("could not setup config folders", e);
            }
        });
    }

    private void setupPackaging(Project p, Task buildPackage, PackagingExtension extension) {
        Set<String> configuredEnvironments = extension.getEnvironments().get();

        if (configuredEnvironments.isEmpty()) {
            return;
        }

        String packageName = extension.getPackageName().get();
        Path packageFolder = extension.getTempFolder().getAsFile().get().toPath().resolve(packageName);

        Delete cleanTargetFolder = p.getTasks().create("cleanTempFolder", Delete.class, t -> {
            t.delete(packageFolder);
        });

        WriteProperties writeProps = p.getTasks().create("writeMetaData", WriteProperties.class, t -> {
            t.property("package_version", "2.3");
            t.property("datahub_infra", extension.getDatahub().getOrElse(Boolean.FALSE));
            t.property("pre-production-env", extension.getPreProductionEnvironment().get());
            t.setOutputFile(packageFolder.resolve("metadata.properties"));
        });
        writeProps.dependsOn(cleanTargetFolder);

        Zip zipPackage = p.getTasks().create("zipCCV1Package", Zip.class, z -> {
            z.from(packageFolder);
            z.into(packageName);
            z.setBaseName(packageName);
            z.setVersion("");
            z.setClassifier("");
            z.setDestinationDir(extension.getDistributionFolder().getAsFile().get());
        });

        zipPackage.dependsOn(writeProps);

        setupPlatformPackaging(p, extension, packageFolder, zipPackage, cleanTargetFolder);

        if (extension.getDatahub().getOrElse(Boolean.FALSE)) {
            setupDatahubPackaging(p, extension, packageFolder, zipPackage, cleanTargetFolder);
        }

        if (extension.getSolr().getOrElse(Boolean.FALSE)) {
            setupSolrPackaging(p, extension, packageFolder, zipPackage, cleanTargetFolder);
        }

        Task md5Sum = p.getTasks().create("md5Sum", t -> t.doLast(a -> {
            Map<String, Object> args = new HashMap<>();
            args.put("file", zipPackage.getArchivePath());
            args.put("format", "MD5SUM");
            args.put("fileext", ".MD5");
            p.getAnt().invokeMethod("checksum", args);

            String archiveName = zipPackage.getArchiveName();
            Path resolve = zipPackage.getDestinationDir().toPath().resolve(archiveName + ".MD5");
            Path target = zipPackage.getDestinationDir().toPath().resolve(archiveName.substring(0, archiveName.lastIndexOf('.')) + ".md5");
            try {
                Files.delete(target);
            } catch (IOException e) {
                //we dont care
            }
            try {
                Files.move(resolve, target);
            } catch (IOException e) {
                throw new GradleException("could not move md5 file", e);
            }
        }));
        md5Sum.dependsOn(zipPackage);

        buildPackage.dependsOn(md5Sum);
    }

    private void setupPlatformPackaging(Project p, PackagingExtension extension, Path packageFolder, Task zipPackageFolder, Task cleanTargetFolder) {
        Path hybrisBin = packageFolder.resolve("hybris/bin");

        Copy copyPlatform = p.getTasks().create("copyPlatform", Copy.class, t -> {
            t.from(extension.getPlatformZip());
            t.into(hybrisBin);
            t.onlyIf(a -> {
                if (a.getInputs().getSourceFiles().isEmpty()) {
                    throw new StopExecutionException("no platform file found");
                }
                return true;
            });
        });
        copyPlatform.dependsOn(cleanTargetFolder);
        zipPackageFolder.dependsOn(copyPlatform);


        Copy copyAllExtensions = p.getTasks().create("copyAllExtensions", Copy.class, t -> {
            t.from(extension.getAllExtensionsZip());
            t.into(hybrisBin);
            t.onlyIf(a -> {
                if (a.getInputs().getSourceFiles().isEmpty()) {
                    throw new StopExecutionException("no allExtensions file found");
                }
                return true;
            });
        });
        copyAllExtensions.dependsOn(cleanTargetFolder);
        zipPackageFolder.dependsOn(copyAllExtensions);

        Set<String> configuredEnvironments = extension.getEnvironments().get();

        Path configurationFolder = extension.getConfigurationFolder().getAsFile().get().toPath();

        for (String environment : configuredEnvironments) {
            Path commonFolder = configurationFolder.resolve(COMMON_CONFIG).resolve("hybris");
            Path environmentFolder = configurationFolder.resolve(environment).resolve("hybris");
            Path targetFolder = packageFolder.resolve("hybris/config/" + environment);

            Copy copyCommonHybrisConfigToTarget = p.getTasks().create("copyCommonHybris_" + environment, Copy.class, t -> {
                t.from(commonFolder);
                t.into(targetFolder);
                t.setDuplicatesStrategy(DuplicatesStrategy.INCLUDE);
                t.exclude(HYBRIS_CONFIG_EXCLUDE);
            });
            copyCommonHybrisConfigToTarget.dependsOn(cleanTargetFolder);

            Copy copyEnvHyrisConfigToTarget = p.getTasks().create("copyEnvHybris_" + environment, Copy.class, t -> {
                t.from(environmentFolder);
                t.into(targetFolder);
                t.setDuplicatesStrategy(DuplicatesStrategy.INCLUDE);
                t.exclude(HYBRIS_CONFIG_EXCLUDE);
            });
            copyEnvHyrisConfigToTarget.dependsOn(copyCommonHybrisConfigToTarget);

            for (String mergeProperty : Arrays.asList("customer.adm.properties", "customer.app.properties")) {
                Task mergeCustomerProperties = p.getTasks().create(String.format("mergeCustomerProperties_%s_%s", environment, mergeProperty), MergePropertyFiles.class, t -> {
                    t.getInputFiles().setFrom(Arrays.asList(
                            commonFolder.resolve("customer.properties"),
                            commonFolder.resolve(mergeProperty),
                            environmentFolder.resolve("customer.properties"),
                            environmentFolder.resolve(mergeProperty)
                    ));
                    t.setOutputFile(targetFolder.resolve(mergeProperty));
                    t.setComment("Generated " + Instant.now().toString());
                });
                mergeCustomerProperties.dependsOn(copyEnvHyrisConfigToTarget);
                zipPackageFolder.dependsOn(mergeCustomerProperties);
            }

            for (Path localExtension : Arrays.asList(targetFolder.resolve("localextensions.app.xml"), targetFolder.resolve("localextensions.adm.xml"))) {
                Task copyLocalExtensionsFromEnv = p.getTasks().create("copyLocalExtensions" + environment + "_" + localExtension.getFileName(), Copy.class, t -> {
                    t.onlyIf(tsk -> {
                        Path local = environmentFolder.resolve("localextensions.xml");
                        return Files.exists(local) && !(Files.exists(localExtension));
                    });
                    t.from(environmentFolder.resolve("localextensions.xml"));
                    t.into(targetFolder);
                    t.rename(".*", localExtension.getFileName().toString());
                });
                copyLocalExtensionsFromEnv.dependsOn(copyEnvHyrisConfigToTarget);
                Task copyLocalExtensionsFromCommon = p.getTasks().create("copyLocalExtensions_common_" + environment + "_" + localExtension.getFileName(), Copy.class, t -> {
                    t.onlyIf(tsk -> {
                        Path local = commonFolder.resolve("localextensions.xml");
                        return Files.exists(local) && !(Files.exists(localExtension));
                    });
                    t.from(commonFolder.resolve("localextensions.xml"));
                    t.into(targetFolder);
                    t.rename(".*", localExtension.getFileName().toString());
                });
                copyLocalExtensionsFromCommon.dependsOn(copyLocalExtensionsFromEnv);
                zipPackageFolder.dependsOn(copyLocalExtensionsFromCommon);
            }
        }
        for (String environment : configuredEnvironments) {
            Path commonMiscFolder = configurationFolder.resolve(COMMON_CONFIG).resolve("misc");
            Path environmentMiscFolder = configurationFolder.resolve(environment).resolve("misc");
            Path packageMiscFolder = packageFolder.resolve("hybris/misc/" + environment);

            Copy copyCommonMiscToTarget = p.getTasks().create("copyCommonMisc_" + environment, Copy.class, t -> {
                t.from(commonMiscFolder);
                t.into(packageMiscFolder);
                t.setDuplicatesStrategy(DuplicatesStrategy.INCLUDE);
            });
            copyCommonMiscToTarget.dependsOn(cleanTargetFolder);

            Copy copyEnvMiscToTarget = p.getTasks().create("copyEnvMisc_" + environment, Copy.class, t -> {
                t.from(environmentMiscFolder);
                t.into(packageMiscFolder);
                t.setDuplicatesStrategy(DuplicatesStrategy.INCLUDE);
            });
            copyEnvMiscToTarget.dependsOn(copyCommonMiscToTarget);
            zipPackageFolder.dependsOn(copyEnvMiscToTarget);
        }
    }

    private void setupSolrPackaging(Project p, PackagingExtension extension, Path packageFolder, Zip zipPackage, Task cleanTargetFolder) {
        // FIXME This is only POC for Solr configuration only.
        Set<String> environments = extension.getEnvironments().get();
        Path configurationFolder = extension.getConfigurationFolder().getAsFile().get().toPath();
        for (String environment : environments) {
            Path sourceFolder = configurationFolder.resolve(environment).resolve("solr");
            Path commonFolder = configurationFolder.resolve(COMMON_CONFIG).resolve("solr");
            Path targetFolder = packageFolder.resolve("solr/config/" + environment);

            Copy copySolrCommonConfig = p.getTasks().create("copySolrCommonEnv_" + environment, Copy.class, t -> {
                t.from(commonFolder);
                t.into(targetFolder);
                t.setDuplicatesStrategy(DuplicatesStrategy.INCLUDE);
                t.exclude(SOLR_CONFIG_EXCLUDE);
            });
            copySolrCommonConfig.dependsOn(cleanTargetFolder);

            Copy copySolrConfig = p.getTasks().create("copySolrEnv_" + environment, Copy.class, t -> {
                t.from(sourceFolder);
                t.into(targetFolder);
                t.setDuplicatesStrategy(DuplicatesStrategy.INCLUDE);
                t.exclude(SOLR_CONFIG_EXCLUDE);
            });
            copySolrConfig.dependsOn(copySolrCommonConfig);

            zipPackage.dependsOn(copySolrConfig);
        }
    }

    private void setupDatahubPackaging(Project p, PackagingExtension extension, Path packageFolder, Zip zipPackage, Task cleanTargetFolder) {
        Copy copyDataHubWar = p.getTasks().create("copyDataHubWar", Copy.class, t -> {
            t.from(extension.getDatahubWar(), s -> s.rename(".*", "datahub-webapp.war"));
            t.into(packageFolder.resolve("datahub/bin"));
            t.onlyIf(a -> {
                if (a.getInputs().getSourceFiles().isEmpty()) {
                    throw new StopExecutionException("no datahub file found");
                }
                return true;
            });
        });
        copyDataHubWar.dependsOn(cleanTargetFolder);
        zipPackage.dependsOn(copyDataHubWar);

        Set<String> environments = extension.getEnvironments().get();
        Path configurationFolder = extension.getConfigurationFolder().getAsFile().get().toPath();
        for (String environment : environments) {
            Path sourceFolder = configurationFolder.resolve(environment).resolve("datahub");
            Path commonFolder = configurationFolder.resolve(COMMON_CONFIG).resolve("datahub");
            Path targetFolder = packageFolder.resolve("datahub/config/" + environment);

            Copy copyCommonConfig = p.getTasks().create("copyDatahubCommonEnv_" + environment, Copy.class, t -> {
                t.from(commonFolder);
                t.into(targetFolder);
                t.setDuplicatesStrategy(DuplicatesStrategy.INCLUDE);
                t.exclude(DATAHUB_CONFIG_EXCLUDE);
            });
            copyCommonConfig.dependsOn(cleanTargetFolder);

            Copy copyDatahubConfig = p.getTasks().create("copyDatahubEnv_" + environment, Copy.class, t -> {
                t.from(sourceFolder);
                t.into(targetFolder);
                t.setDuplicatesStrategy(DuplicatesStrategy.INCLUDE);
                t.exclude(DATAHUB_CONFIG_EXCLUDE);
            });
            copyDatahubConfig.dependsOn(copyCommonConfig);

            MergePropertyFiles mergeProperties = p.getTasks().create("mergeDatahub_customer.properties_" + environment, MergePropertyFiles.class, t -> {
                t.getInputFiles().setFrom(Arrays.asList(
                        commonFolder.resolve("customer.properties"),
                        sourceFolder.resolve("customer.properties")
                ));
                t.setOutputFile(targetFolder.resolve("customer.properties"));
            });
            mergeProperties.dependsOn(copyDatahubConfig);

            zipPackage.dependsOn(mergeProperties);
        }
    }
}

