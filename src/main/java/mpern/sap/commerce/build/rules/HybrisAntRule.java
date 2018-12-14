package mpern.sap.commerce.build.rules;

import mpern.sap.commerce.build.tasks.HybrisAntTask;
import org.gradle.api.Project;
import org.gradle.api.Rule;

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
            HybrisAntTask javaExec = project.getTasks().create(taskName, HybrisAntTask.class);
            javaExec.args(antTarget);
        }
    }
}
