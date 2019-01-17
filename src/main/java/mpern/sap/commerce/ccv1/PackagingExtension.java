package mpern.sap.commerce.ccv1;

import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.SetProperty;

import javax.inject.Inject;

public class PackagingExtension {
    private final Property<Boolean> datahub;
    private final Property<Boolean> solr;

    private final Property<String> preProductionEnvironment;
    private final Property<String> customerID;
    private final Property<String> projectID;
    private final Provider<String> packageName;

    private final SetProperty<String> environments;

    private final RegularFileProperty datahubWar;
    private final RegularFileProperty platformZip;
    private final RegularFileProperty allExtensionsZip;

    private final DirectoryProperty configurationFolder;
    private final DirectoryProperty distributionFolder;
    private final DirectoryProperty tempFolder;

    @Inject
    public PackagingExtension(Project project) {
        datahub = project.getObjects().property(Boolean.class);
        datahub.set(Boolean.FALSE);
        solr = project.getObjects().property(Boolean.class);
        solr.set(Boolean.FALSE);
        preProductionEnvironment = project.getObjects().property(String.class);
        customerID = project.getObjects().property(String.class);
        projectID = project.getObjects().property(String.class);

        packageName = project.provider(() -> buildPackageName(project));

        datahubWar = project.getLayout().fileProperty();
        platformZip = project.getLayout().fileProperty();
        allExtensionsZip = project.getLayout().fileProperty();

        environments = project.getObjects().setProperty(String.class);

        configurationFolder = project.getLayout().directoryProperty();
        distributionFolder = project.getLayout().directoryProperty();
        tempFolder = project.getLayout().directoryProperty();
    }

    public Property<Boolean> getDatahub() {
        return datahub;
    }

    public Property<Boolean> getSolr() {
        return solr;
    }

    public Property<String> getPreProductionEnvironment() {
        return preProductionEnvironment;
    }

    public Property<String> getCustomerID() {
        return customerID;
    }

    public Property<String> getProjectID() {
        return projectID;
    }

    public Provider<String> getPackageName() {
        return packageName;
    }

    public RegularFileProperty getDatahubWar() {
        return datahubWar;
    }

    public RegularFileProperty getPlatformZip() {
        return platformZip;
    }

    public RegularFileProperty getAllExtensionsZip() {
        return allExtensionsZip;
    }

    public SetProperty<String> getEnvironments() {
        return environments;
    }

    public DirectoryProperty getConfigurationFolder() {
        return configurationFolder;
    }

    public DirectoryProperty getDistributionFolder() {
        return distributionFolder;
    }

    public DirectoryProperty getTempFolder() {
        return tempFolder;
    }

    private String buildPackageName(Project project) {
        String customerId = customerID.getOrElse("");
        return (customerId.isEmpty() ? "" : customerId + "-") + projectID.get() + "_v" + project.getVersion().toString();
    }
}
