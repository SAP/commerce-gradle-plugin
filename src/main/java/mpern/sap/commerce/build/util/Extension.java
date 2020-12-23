package mpern.sap.commerce.build.util;

import java.nio.file.Path;
import java.util.Objects;

public class Extension {
    public final String name;
    public final Path directory;

    public Extension(String name, Path directory) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(directory);
        this.name = name;
        this.directory = directory;
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
}
