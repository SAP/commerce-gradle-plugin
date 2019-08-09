package mpern.sap.commerce.ccv2;

import groovy.json.JsonSlurper;
import groovy.lang.Tuple2;
import mpern.sap.commerce.build.HybrisPlugin;
import mpern.sap.commerce.build.HybrisPluginExtension;
import mpern.sap.commerce.build.tasks.HybrisAntTask;
import mpern.sap.commerce.ccv2.model.Addon;
import mpern.sap.commerce.ccv2.model.Aspect;
import mpern.sap.commerce.ccv2.model.Manifest;
import mpern.sap.commerce.ccv2.model.Property;
import mpern.sap.commerce.ccv2.model.TestConfiguration;
import mpern.sap.commerce.ccv2.model.Webapp;
import mpern.sap.commerce.ccv2.tasks.GenerateLocalextensions;
import mpern.sap.commerce.ccv2.tasks.PatchLocalExtensions;
import org.gradle.api.Action;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.Delete;
import org.gradle.api.tasks.WriteProperties;

import java.io.File;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

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
        JsonSlurper slurper = new JsonSlurper();
        Map parsed = (Map) slurper.parse(manifestFile);
        Manifest manifest = Manifest.fromMap(parsed);

        extension = project.getExtensions().create("CCV2", CCv2Extension.class, project, manifest);
        extension.getGeneratedConfiguration().set(project.file("generated-configuration"));
        extension.getCloudExtensionPackFolder().set(project.file("cloud-extension-pack"));

        final Configuration extensionPack = project.getConfigurations().create(EXTENSION_PACK);
        extensionPack.defaultDependencies(deps -> {
            //de/hybris/platform/hybris-cloud-extension-pack/1905.06/
            String commerceSuiteVersion = manifest.commerceSuiteVersion;
            String cepVersion = commerceSuiteVersion.replaceAll("(.+)(\\.\\d\\d)?", "$1.+");

            deps.add(project.getDependencies().create("de.hybris.platform:hybris-cloud-extension-pack:" + cepVersion + "@zip"));
        });


        project.getPlugins().withType(HybrisPlugin.class, hybrisPlugin -> {
            Object byName = project.getExtensions().getByName(HybrisPlugin.HYBRIS_EXTENSION);
            if (byName instanceof HybrisPluginExtension) {
                ((HybrisPluginExtension) byName).getVersion().set(project.provider(() -> manifest.commerceSuiteVersion));
                configureAddonInstall(project, manifest.storefrontAddons);
                configureTests(project, manifest.tests);
                configureWebTests(project, manifest.webTests);
                configureCloudExtensionPackBootstrap(project, manifest.useCloudExtensionPack);
            }
        });
        configurePropertyFileGeneration(project, manifest);
        configureExtensionGeneration(project, manifest);
    }

    private void configureAddonInstall(Project project, List<Addon> storefrontAddons) {
        if (storefrontAddons.isEmpty()) {
            return;
        }
        Task installManifestAddons = project.getTasks().create("installManifestAddons");
        installManifestAddons.setGroup(GROUP);
        installManifestAddons.setDescription("runs ant addoninstall for all addons configured in manifest.json");

        Map<Tuple2<String, String>, Set<String>> addonsPerStorefront = new HashMap<>();
        for (Addon c : storefrontAddons) {
            Tuple2<String, String> templateStorefront = new Tuple2<>(c.template, c.storefront);
            addonsPerStorefront.computeIfAbsent(templateStorefront, t -> new LinkedHashSet<>()).add(c.addon);
        }
        for (Map.Entry<Tuple2<String, String>, Set<String>> tuple2SetEntry : addonsPerStorefront.entrySet()) {
            Tuple2<String, String> templateStorefront = tuple2SetEntry.getKey();
            Set<String> addons = tuple2SetEntry.getValue();
            HybrisAntTask install = project.getTasks().create(
                    String.format("addonInstall_%s_%s", templateStorefront.getFirst(), templateStorefront.getSecond()),
                    HybrisAntTask.class,
                    t -> {
                        t.args("addoninstall");
                        t.antProperty("addonnames", String.join(",", addons));
                        t.antProperty("addonStorefront." + templateStorefront.getFirst(), templateStorefront.getSecond());
                    });
            installManifestAddons.dependsOn(install);
        }
    }

    private void configureTests(Project project, TestConfiguration tests) {
        if (tests == TestConfiguration.NO_VALUE) {
            return;
        }
        HybrisAntTask allTests = project.getTasks().create("cloudTests", HybrisAntTask.class, configureTest(tests));
        allTests.setArgs(Collections.singletonList("alltests"));
        allTests.setGroup(GROUP);
        allTests.setDescription("run ant alltests with manifest configuration");
    }

    private void configureWebTests(Project project, TestConfiguration test) {
        if (test == TestConfiguration.NO_VALUE) {
            return;
        }
        HybrisAntTask allWebTests = project.getTasks().create("cloudWebTests", HybrisAntTask.class, configureTest(test));
        allWebTests.setArgs(Collections.singletonList("allwebtests"));
        allWebTests.setGroup(GROUP);
        allWebTests.setDescription("run ant allwebtests with manifest configuration");
    }

    private void configureCloudExtensionPackBootstrap(Project project, boolean useCloudExtensionPack) {
        if (!useCloudExtensionPack) {
            return;
        }
        Task cleanCep = project.getTasks().create("cleanCloudExtensionPack", Delete.class, d -> {
            d.delete(extension.getCloudExtensionPackFolder().getAsFile());
        });
        Copy unpackCep = project.getTasks().create("unpackCloudExtensionPack", Copy.class, c -> {
            c.dependsOn(cleanCep);
            c.from(project.provider(() -> project.getConfigurations()
                            .getByName(EXTENSION_PACK)
                            .getFiles()
                            .stream()
                            .map(project::zipTree)
                            .collect(Collectors.toSet())
                    )
            );
            c.into(extension.getCloudExtensionPackFolder().getAsFile());
            c.doLast(a -> project.getConfigurations().getByName(EXTENSION_PACK)
                    .getResolvedConfiguration()
                    .getFirstLevelModuleDependencies()
                    .forEach(r -> a.getLogger().lifecycle("Using Cloud Extension Pack: {}", r.getModuleVersion())));
        });

        Task bootstrapPlatform = project.getTasks().getByName("bootstrapPlatform");
        bootstrapPlatform.dependsOn(unpackCep);
        String reservedTypeCodes = "hybris/bin/platform/ext/core/resources/core/unittest/reservedTypecodes.txt";
        Copy copyTypeCodes = project.getTasks().create("copyCEPTypeCode", Copy.class, c -> {
            c.mustRunAfter(unpackCep);
            c.mustRunAfter("unpackPlatform");
            c.from(extension.getCloudExtensionPackFolder().file(reservedTypeCodes));
            c.into(project.file(reservedTypeCodes).getParent());
        });
        bootstrapPlatform.dependsOn(copyTypeCodes);
        PatchLocalExtensions patch = project.getTasks().create("patchLocalExtensions", PatchLocalExtensions.class, p -> {
            p.getTarget().set(project.file("hybris/config/localextensions.xml"));
            p.getCepFolder().set(extension.getCloudExtensionPackFolder());
        });
        bootstrapPlatform.dependsOn(patch);
    }

    private void configurePropertyFileGeneration(Project project, Manifest manifest) {

        Map<String, Map<String, Object>> allProps = new TreeMap<>();
        for (Property property : manifest.properties) {
            Map<String, Object> props = allProps.computeIfAbsent(propertyFileKey("common", property.persona), k -> new LinkedHashMap<>());
            props.put(property.key, property.value);
        }
        for (Aspect aspect : manifest.aspects) {
            for (Property property : aspect.properties) {
                Map<String, Object> props = allProps.computeIfAbsent(propertyFileKey(aspect.name, property.persona), k -> new LinkedHashMap<>());
                props.put(property.key, property.value);
            }
            for (Webapp webapp : aspect.webapps) {
                Map<String, Object> props = allProps.computeIfAbsent(propertyFileKey(aspect.name, ""), k -> new LinkedHashMap<>());
                props.put(webapp.name + ".webroot", webapp.contextPath);
            }
        }
        Task generateCloudProperties = project.getTasks().create("generateCloudProperties");
        generateCloudProperties.setGroup(GROUP);
        generateCloudProperties.setDescription("generate property files per aspect and persona");
        for (Map.Entry<String, Map<String, Object>> properties : allProps.entrySet()) {
            WriteProperties w = project.getTasks().create("write_" + properties.getKey(), WriteProperties.class, t -> {
                t.setEncoding("UTF-8");
                t.setOutputFile(extension.getGeneratedConfiguration().file(properties.getKey() + ".properties"));
                t.setProperties(properties.getValue());
                t.getInputs().file(project.file(MANIFEST_PATH));
                t.setComment(String.format("GENERATED by task %s at %s", t.getName(), Instant.now()));
            });
            generateCloudProperties.dependsOn(w);
        }
    }

    private void configureExtensionGeneration(Project project, Manifest manifest) {
        project.getTasks().create("generateCloudLocalextensions", GenerateLocalextensions.class, t -> {
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
