package mpern.sap.commerce.ccv2.tasks;

import static mpern.sap.commerce.ccv2.CloudV2Plugin.CCV2_EXTENSION;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.tasks.TaskAction;
import org.gradle.internal.logging.text.StyledTextOutput;
import org.gradle.internal.logging.text.StyledTextOutputFactory;

import mpern.sap.commerce.ccv2.CCv2Extension;
import mpern.sap.commerce.ccv2.validation.Error;
import mpern.sap.commerce.ccv2.validation.Level;
import mpern.sap.commerce.ccv2.validation.Validator;
import mpern.sap.commerce.ccv2.validation.impl.*;

public class ValidateManifest extends DefaultTask {

    private final StyledTextOutputFactory styledTextOutputFactory;

    @Inject
    public ValidateManifest(StyledTextOutputFactory styledTextOutputFactory) {
        this.styledTextOutputFactory = styledTextOutputFactory;
    }

    @TaskAction
    public void validateManifest() throws Exception {
        Path projectDir = getProject().getProjectDir().toPath();

        List<Validator> validators = new ArrayList<>();

        validators.add(new AspectValidator());
        validators.add(new CloudExtensionPackValidator());
        validators.add(new PropertyValidator());
        validators.add(new UseConfigValidator(projectDir));
        ManifestExtensionsResolver resolver = new ManifestExtensionsResolver(projectDir);
        validators.add(new CloudHotfolderValidator(projectDir, resolver));
        validators.add(new MediaConversionValidator(resolver));
        validators.add(new WebrootValidator(projectDir));
        validators.add(new SolrVersionValidator());
        validators.add(new IntExtPackValidator());

        boolean deepInspection = false;
        if (Files.exists(projectDir.resolve("hybris/bin/platform"))) {
            deepInspection = true;
            validators.add(new AddonValidator(resolver));
            validators.add(new AspectWebappValidator(resolver));
        }

        CCv2Extension extension = (CCv2Extension) getProject().getExtensions().getByName(CCV2_EXTENSION);

        List<Error> errors = new ArrayList<>();
        for (Validator validator : validators) {
            errors.addAll(validator.validate(extension.getManifest()));
        }
        errors.sort(Comparator.comparing(Error::getLevel).thenComparing(Error::getLocation));

        StyledTextOutput statusOut = styledTextOutputFactory.create(ValidateManifest.class);
        statusOut.withStyle(StyledTextOutput.Style.Header)
                .println("--------------------- Manifest Validation Results ----------------------\n");
        if (errors.isEmpty()) {
            statusOut.withStyle(StyledTextOutput.Style.Success).println("No issues detected");
        }
        for (Error error : errors) {
            switch (error.level) {
            case WARNING:
                statusOut.withStyle(StyledTextOutput.Style.Description).format("%s %s @ %s\n", error.level, error.code,
                        error.location);
                statusOut.withStyle(StyledTextOutput.Style.Description).println(error.message);
                statusOut.formatln(toLink(error.code));
                statusOut.println();
                break;
            case ERROR:
                statusOut.withStyle(StyledTextOutput.Style.FailureHeader).format("%s %s @ %s\n", error.level,
                        error.code, error.location);
                statusOut.withStyle(StyledTextOutput.Style.Failure).println(error.message);
                statusOut.formatln(toLink(error.code));
                statusOut.println();
                break;
            }
        }
        statusOut.withStyle(StyledTextOutput.Style.Header)
                .println("------------------------------------------------------------------------");
        if (!deepInspection) {
            statusOut.withStyle(StyledTextOutput.Style.Info)
                    .println("hybris/bin/platform not available. Cannot perform deep inspection.");
        }
        long numWarnings = errors.stream().filter(e -> Level.WARNING == e.level).count();
        long numErrors = errors.stream().filter(e -> Level.ERROR == e.level).count();
        statusOut.withStyle(StyledTextOutput.Style.Header).format("Errors: ");
        if (numErrors > 0) {
            statusOut.withStyle(StyledTextOutput.Style.FailureHeader).format("%d\n", numErrors);
        } else {
            statusOut.withStyle(StyledTextOutput.Style.SuccessHeader).format("%d\n", numErrors);
        }
        statusOut.withStyle(StyledTextOutput.Style.Header).append("Warnings: ");
        if (numWarnings > 0) {
            statusOut.withStyle(StyledTextOutput.Style.Description).format("%d\n", numWarnings);
        } else {
            statusOut.withStyle(StyledTextOutput.Style.SuccessHeader).format("%d\n", numWarnings);
        }
        if (numErrors > 0) {
            throw new InvalidUserDataException(String.format("Found %d errors in manifest.json", numErrors));
        }
    }

    private String toLink(String code) {
        code = code.toLowerCase();
        code = code.replace("-", "");
        return "https://github.com/SAP/commerce-gradle-plugin/blob/master/docs/ccv2-validation.md#" + code;
    }
}
