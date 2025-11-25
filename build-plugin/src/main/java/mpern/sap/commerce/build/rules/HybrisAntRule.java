package mpern.sap.commerce.build.rules;

import static mpern.sap.commerce.build.HybrisPlugin.HYBRIS_EXTENSION;

import java.util.List;
import java.util.concurrent.Callable;

import org.gradle.api.Project;
import org.gradle.api.Rule;

import mpern.sap.commerce.build.HybrisPluginExtension;
import mpern.sap.commerce.build.tasks.HybrisAntTask;

public class HybrisAntRule implements Rule {

    public static final String PREFIX = "y";
    private final Project project;

    public HybrisAntRule(Project project) {
        this.project = project;
    }

    @Override
    public String getDescription() {
        return "Pattern: y<target>: Run hybris ant <target>";
    }

    @Override
    public void apply(String taskName) {
        if (taskName.startsWith(PREFIX)) {
            String antTarget = taskName.substring(PREFIX.length());
            project.getTasks().register(taskName, HybrisAntTask.class, t -> {
                t.args(antTarget);
                t.dependsOn((Callable<List<Object>>) () -> {
                    HybrisPluginExtension extension = (HybrisPluginExtension) project.getExtensions()
                            .getByName(HYBRIS_EXTENSION);
                    return extension.getAntTaskDependencies().getOrNull();
                });
            });
        }
    }
}
