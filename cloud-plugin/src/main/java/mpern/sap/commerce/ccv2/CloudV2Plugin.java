package mpern.sap.commerce.ccv2;

import static mpern.sap.commerce.commons.Constants.CCV2_EXTENSION;

import java.io.File;
import java.time.Instant;
import java.util.*;

import org.gradle.api.*;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.WriteProperties;
import org.jetbrains.annotations.NotNull;

import groovy.json.JsonSlurper;

import mpern.sap.commerce.build.HybrisPlugin;
import mpern.sap.commerce.build.HybrisPluginExtension;
import mpern.sap.commerce.build.tasks.HybrisAntTask;
import mpern.sap.commerce.build.util.Version;
import mpern.sap.commerce.ccv2.model.*;
import mpern.sap.commerce.ccv2.tasks.GenerateLocalextensions;
import mpern.sap.commerce.ccv2.tasks.ValidateManifest;

public class CloudV2Plugin implements Plugin<Project> {

    public static final String EXTENSION_PACK = "cloudExtensionPack";
    private static final String GROUP = "CCv2 Build";
    private static final String MANIFEST_PATH = "manifest.json";
    private CCv2Extension extension;

    @Override
    public void apply(Project project) {
        File manifestFile = project.file(MANIFEST_PATH);

        if (!manifestFile.exists()) {
            throw new InvalidUserDataException(MANIFEST_PATH + " not found!");
        }
        Manifest manifest = parseManifest(manifestFile);

        extension = project.getExtensions().create(CCV2_EXTENSION, CCv2Extension.class, manifest);
        extension.getGeneratedConfiguration().set(project.file("generated-configuration"));

        final Configuration extensionPack = project.getConfigurations().create(EXTENSION_PACK);

        project.getPlugins().withType(HybrisPlugin.class, hybrisPlugin -> {
            Object o = project.getExtensions().getByName(HybrisPlugin.HYBRIS_EXTENSION);
            if (o instanceof HybrisPluginExtension hybrisPluginExtension) {
                configureDefaultDependencies(hybrisPluginExtension, project, manifest);
                configureAddonInstall(project, manifest.storefrontAddons);
                configureTests(project, manifest.tests);
                configureWebTests(project, manifest.webTests);
            }
        });
        configurePropertyFileGeneration(project, manifest);
        configureExtensionGeneration(project, manifest);

        project.getTasks().register("validateManifest", ValidateManifest.class, t -> {
            t.setGroup(GROUP);
            t.setDescription("Validate manifest.json for common errors");
        });
    }

    @NotNull
    @SuppressWarnings("unchecked")
    private Manifest parseManifest(File manifestFile) {
        JsonSlurper slurper = new JsonSlurper();
        Map<String, Object> parsed = (Map<String, Object>) slurper.parse(manifestFile);
        return Manifest.fromMap(parsed);
    }

    private void configureDefaultDependencies(HybrisPluginExtension extension, Project project, Manifest manifest) {
        extension.getVersion().set(project.provider(manifest::getEffectiveVersion));
        final Configuration hybrisPlatform = project.getConfigurations()
                .getByName(HybrisPlugin.HYBRIS_PLATFORM_CONFIGURATION);
        hybrisPlatform.defaultDependencies(dependencies -> {
            String v = extension.getVersion().get();
            Version version = Version.parseVersion(v);
            dependencies.add(project.getDependencies()
                    .create("de.hybris.platform:hybris-commerce-suite:" + version.getDependencyVersion() + "@zip"));
            manifest.extensionPacks.forEach(p -> {
                if (!p.artifact.isEmpty()) {
                    dependencies.add(project.getDependencies().create(p.artifact));
                } else {
                    Version parseVersion = Version.parseVersion(p.version);
                    dependencies.add(project.getDependencies().create(String.format("de.hybris.platform:%s:%s@zip",
                            p.name, parseVersion.getDependencyVersion())));
                }
            });
        });
    }

    private void configureAddonInstall(Project project, List<Addon> storefrontAddons) {
        TaskProvider<Task> installManifestAddons = project.getTasks().register("installManifestAddons", t -> {
            t.setGroup(GROUP);
            t.setDescription("runs ant addoninstall for all addons configured in manifest.json");
        });

        if (storefrontAddons.isEmpty()) {
            return;
        }
        for (int i = 0; i < storefrontAddons.size(); i++) {
            Addon addonInstall = storefrontAddons.get(i);

            List<String> storeFronts = new ArrayList<>(addonInstall.storefronts);
            if (!addonInstall.storefront.isEmpty()) {
                storeFronts.add(addonInstall.storefront);
            }
            List<String> addons = new ArrayList<>(addonInstall.addons);
            if (!addonInstall.addon.isEmpty()) {
                addons.add(addonInstall.addon);
            }
            String storefrontParameter = String.join(",", storeFronts);
            String addonParameter = String.join(",", addons);
            int finalI = i;
            TaskProvider<HybrisAntTask> install = project.getTasks().register(String.format("addonInstall_%d", i),
                    HybrisAntTask.class, t -> {
                        if (finalI > 0) {
                            t.mustRunAfter(String.format("addonInstall_%d", (finalI - 1)));
                        }
                        t.mustRunAfter("unpackPlatform", "unpackPlatformSparse");
                        t.args("addoninstall");
                        t.antProperty("addonnames", addonParameter);
                        t.antProperty("addonStorefront." + addonInstall.template, storefrontParameter);
                    });
            installManifestAddons.configure(t -> t.dependsOn(install));
        }
    }

    private void configureTests(Project project, TestConfiguration tests) {
        if (tests == TestConfiguration.NO_VALUE) {
            project.getTasks().register("cloudTests", HybrisAntTask.class, t -> {
                t.setGroup(GROUP);
                t.setDescription("run ant alltests with manifest configuration");
                t.getNoOp().set(Boolean.TRUE);
            });
            return;
        }
        TaskProvider<HybrisAntTask> allTests = project.getTasks().register("cloudTests", HybrisAntTask.class, t -> {
            t.setGroup(GROUP);
            t.setDescription("run ant alltests with manifest configuration");

            t.setArgs(Collections.singletonList("alltests"));
            t.antProperty("failbuildonerror", "yes");
        });
        allTests.configure(configureTest(tests));
    }

    private void configureWebTests(Project project, TestConfiguration test) {
        if (test == TestConfiguration.NO_VALUE) {
            project.getTasks().register("cloudWebTests", HybrisAntTask.class, t -> {
                t.setGroup(GROUP);
                t.setDescription("run ant allwebtests with manifest configuration");
                t.getNoOp().set(Boolean.TRUE);
            });
            return;
        }
        TaskProvider<HybrisAntTask> allWebTests = project.getTasks().register("cloudWebTests", HybrisAntTask.class,
                t -> {
                    t.setGroup(GROUP);
                    t.setDescription("run ant allwebtests with manifest configuration");

                    t.setArgs(Collections.singletonList("allwebtests"));
                    t.antProperty("failbuildonerror", "yes");
                });
        allWebTests.configure(configureTest(test));
    }

    private void configurePropertyFileGeneration(Project project, Manifest manifest) {

        Map<String, Map<String, Object>> allProps = new TreeMap<>();
        for (Property property : manifest.properties) {
            Map<String, Object> props = allProps.computeIfAbsent(propertyFileKey("common", property.persona),
                    k -> new LinkedHashMap<>());
            props.put(property.key, property.value);
        }
        for (Aspect aspect : manifest.aspects) {
            for (Property property : aspect.properties) {
                Map<String, Object> props = allProps.computeIfAbsent(propertyFileKey(aspect.name, property.persona),
                        k -> new LinkedHashMap<>());
                props.put(property.key, property.value);
            }
            for (Webapp webapp : aspect.webapps) {
                Map<String, Object> props = allProps.computeIfAbsent(propertyFileKey(aspect.name, ""),
                        k -> new LinkedHashMap<>());
                props.put(webapp.name + ".webroot", webapp.contextPath);
            }
        }
        TaskProvider<Task> generateCloudProperties = project.getTasks().register("generateCloudProperties", t -> {
            t.setGroup(GROUP);
            t.setDescription("generate property files per aspect and persona");
        });
        for (Map.Entry<String, Map<String, Object>> properties : allProps.entrySet()) {
            TaskProvider<WriteProperties> w = project.getTasks().register("write_" + properties.getKey(),
                    WriteProperties.class, t -> {
                        t.setEncoding("UTF-8");
                        // use deprecated property for backwards compat
                        t.setOutputFile(
                                extension.getGeneratedConfiguration().file(properties.getKey() + ".properties"));
                        t.setProperties(properties.getValue());
                        t.getInputs().file(project.file(MANIFEST_PATH));
                        t.setComment(String.format("GENERATED by task %s at %s", t.getName(), Instant.now()));
                    });
            generateCloudProperties.configure(t -> t.dependsOn(w));
        }
    }

    private void configureExtensionGeneration(Project project, Manifest manifest) {
        project.getTasks().register("generateCloudLocalextensions", GenerateLocalextensions.class, t -> {
            t.setGroup(GROUP);
            t.setDescription("generate localextensions.xml based on manifest");

            t.getTarget().set(extension.getGeneratedConfiguration().file("localextensions.xml"));
            t.getCloudExtensions().set(manifest.extensions);
        });
    }

    private Action<HybrisAntTask> configureTest(TestConfiguration tests) {
        return t -> {
            String extensions = String.join(",", tests.extensions);
            String annotations = String.join(",", tests.annotations);
            String packages = String.join(",", tests.packages);
            Set<String> ex = new LinkedHashSet<>(tests.excludedPackages);
            ex.add("de.hybris.*");
            ex.add("com.hybris.*");
            String excludedPackages = String.join(",", ex);

            t.antProperty("testclasses.extensions", extensions);
            t.antProperty("testclasses.annotations", annotations);
            t.antProperty("testclasses.packages", packages);
            t.antProperty("testclasses.packages.excluded", excludedPackages);
        };
    }

    private String propertyFileKey(String prefix, String suffix) {
        return suffix.isEmpty() ? prefix : prefix + "_" + suffix;
    }
}
