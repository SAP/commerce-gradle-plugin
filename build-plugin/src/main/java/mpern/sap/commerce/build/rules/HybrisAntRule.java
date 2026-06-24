package mpern.sap.commerce.build.rules;

import java.util.List;
import java.util.concurrent.Callable;

import org.gradle.api.Rule;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.tasks.TaskContainer;

import mpern.sap.commerce.build.tasks.HybrisAntTask;

public class HybrisAntRule implements Rule {

    public static final String PREFIX = "y";

    private final TaskContainer tasks;
    private final ListProperty<Object> antTaskDependencies;

    public HybrisAntRule(TaskContainer tasks, ListProperty<Object> antTaskDependencies) {
        this.tasks = tasks;
        this.antTaskDependencies = antTaskDependencies;
    }

    @Override
    public String getDescription() {
        return "Pattern: y<target>: Run hybris ant <target>";
    }

    @Override
    public void apply(String taskName) {
        if (taskName.startsWith(PREFIX)) {
            String antTarget = taskName.substring(PREFIX.length());
            tasks.register(taskName, HybrisAntTask.class, t -> {
                t.args(antTarget);
                t.dependsOn((Callable<List<Object>>) () -> antTaskDependencies.getOrNull());
            });
        }
    }
}
