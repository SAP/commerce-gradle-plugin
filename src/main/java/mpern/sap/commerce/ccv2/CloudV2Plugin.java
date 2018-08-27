package mpern.sap.commerce.ccv2;

import groovy.json.JsonSlurper;
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
import org.gradle.api.Action;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.WriteProperties;

import java.io.File;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class CloudV2Plugin implements Plugin<Project> {

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


        project.getPlugins().withType(HybrisPlugin.class, hybrisPlugin -> {
            Object byName = project.getExtensions().getByName(HybrisPlugin.HYBRIS_EXTENSION);
            if (byName instanceof HybrisPluginExtension) {
                project.afterEvaluate(pro -> ((HybrisPluginExtension) byName).getVersion().set(ManifestVersionMapper.mapToBuildVersion(manifest.commerceSuiteVersion)));
                configureAddonInstall(project, manifest.storefrontAddons);
                configureTests(project, manifest.tests);
                configureWebTests(project, manifest.webTests);
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
        for (Addon c : storefrontAddons) {
            HybrisAntTask install = project.getTasks().create(String.format("addonInstall_%s_%s_%s", c.addon, c.storefront, c.template), HybrisAntTask.class, t -> {
                t.args("addoninstall");
                t.antProperty("addonnames", c.addon);
                t.antProperty("addonStorefront." + c.template, c.storefront);
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

    private void configureWebTests(Project project, TestConfiguration test) {
        if (test == TestConfiguration.NO_VALUE) {
            return;
        }
        HybrisAntTask allWebTests = project.getTasks().create("cloudWebTests", HybrisAntTask.class, configureTest(test));
        allWebTests.setArgs(Collections.singletonList("allwebtests"));
        allWebTests.setGroup(GROUP);
        allWebTests.setDescription("run ant allwebtests with manifest configuration");
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

    private String propertyFileKey(String prefix, String suffix) {
        return suffix.isEmpty() ? prefix : prefix + "_" + suffix;
    }

    private void configureExtensionGeneration(Project project, Manifest manifest) {
        project.getTasks().create("generateCloudLocalextensions", GenerateLocalextensions.class, t -> {
            t.setGroup(GROUP);
            t.setDescription("generate localextensions.xml based on manifest");

            t.getTarget().set(extension.getGeneratedConfiguration().file("localextensions.xml"));
            t.getCloudExtensions().set(manifest.extensions);
        });
    }
}
