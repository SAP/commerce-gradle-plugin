package mpern.sap.commerce.build.util;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public class Extension {

    public final String name;
    public final Path directory;

    /** Relative location of the extension compared to the hybris/bin folder */
    public String relativeLocation;

    public ExtensionType extensionType;

    public List<String> requiredExtensions;

    public Extension(String name, Path directory) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(directory);
        this.name = name;
        this.directory = directory;
    }

    public Extension(String name, Path directory, String relativeLocation, ExtensionType extensionType,
            List<String> requiredExtensions) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(directory);
        Objects.requireNonNull(extensionType);
        Objects.requireNonNull(requiredExtensions);

        this.name = name;
        this.directory = directory;
        this.relativeLocation = relativeLocation;
        this.extensionType = extensionType;
        this.requiredExtensions = requiredExtensions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Extension extension = (Extension) o;
        return name.equals(extension.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Extension.class.getSimpleName() + "[", "]").add("name='" + name + "'")
                .add("relativeLocation=" + relativeLocation).add("directory=" + directory).toString();
    }
}
