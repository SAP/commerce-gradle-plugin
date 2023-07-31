package mpern.sap.commerce.build.util;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public class Extension {

    public final String name;

    /** Relative location of the extension compared to the hybris/bin folder */
    public final Path relativeLocation;

    public final ExtensionType extensionType;

    public final List<String> requiredExtensions;

//
    public Extension(String name, Path relativeLocation) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(relativeLocation);
        this.name = name;
        this.relativeLocation = relativeLocation;
        this.requiredExtensions = Collections.emptyList();
        this.extensionType = ExtensionType.UNKNOWN;
    }

    public Extension(String name, Path relativeLocation, ExtensionType extensionType, List<String> requiredExtensions) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(extensionType);
        Objects.requireNonNull(requiredExtensions);

        this.name = name;
        this.relativeLocation = relativeLocation;
        this.extensionType = extensionType;
        this.requiredExtensions = Collections.unmodifiableList(requiredExtensions);
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
                .add("relativeLocation=" + relativeLocation).toString();
    }
}
