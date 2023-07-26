package mpern.sap.commerce.ccv2.tasks;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.Set;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

public abstract class GenerateLocalextensions extends DefaultTask {

    private static final String START = """
            <hybrisconfig xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:noNamespaceSchemaLocation='../bin/platform/resources/schemas/extensions.xsd'>
              <extensions>
                <path dir='${HYBRIS_BIN_DIR}' autoload='false' />
            """
            .stripIndent();
    private static final String END = """
              </extensions>
            </hybrisconfig>";
            """.stripIndent();

    private static final String EXTENSION = "    <extension name='%s' />\n";

    @TaskAction
    public void generateLocalextensions() throws IOException {
        Path target = getTarget().get().getAsFile().toPath();
        Set<String> extensions = getCloudExtensions().get();
        try (BufferedWriter writer = Files.newBufferedWriter(target, StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING)) {
            writer.write(START);
            writer.write(String.format("\n<!-- GENERATED by task %s at %s -->\n\n", getName(), Instant.now()));
            for (String extension : extensions) {
                writer.write(String.format(EXTENSION, extension));
            }
            writer.write(END);
        }
    }

    @OutputFile
    public abstract RegularFileProperty getTarget();

    @Input
    public abstract SetProperty<String> getCloudExtensions();
}