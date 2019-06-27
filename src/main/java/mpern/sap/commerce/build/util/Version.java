package mpern.sap.commerce.build.util;

import java.util.Comparator;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Version implements Comparable<Version> {
    private static final Pattern NEW_VERSION = Pattern.compile("(\\d\\d)(\\d\\d)(\\.([1-9]?\\d))?");
    private static final Pattern OLD_VERSION = Pattern.compile("(\\d)\\.(\\d)\\.(\\d)(\\.([1-9]?\\d))?");
    public static final Version UNDEFINED = new Version(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, "<undefined>");
    public static final Comparator<Version> VERSION_COMPARATOR = Comparator.comparingInt(Version::getMajor)
            .thenComparingInt(Version::getMinor)
            .thenComparingInt(Version::getRelease)
            .thenComparingInt(Version::getPatch);

    private final int major;
    private final int minor;
    private final int release;
    private final int patch;
    private final String original;

    private Version(int major, int minor, int release, int patch, String original) {
        this.major = major;
        this.minor = minor;
        this.release = release;
        this.patch = patch;
        this.original = original;
    }

    public static Version parseVersion(String v) {
        Objects.requireNonNull(v);

        Matcher oldV = OLD_VERSION.matcher(v);
        Matcher newV = NEW_VERSION.matcher(v);

        if (newV.matches()) {
            int patch = Integer.MAX_VALUE;

            if (newV.groupCount() > 3 && newV.group(4) != null) {
                patch = Integer.parseInt(newV.group(4));
            }
            return new Version(Integer.parseInt(newV.group(1)), Integer.parseInt(newV.group(2)), 0, patch, v);
        } else if (oldV.matches()) {
            int patch = Integer.MAX_VALUE;
            if (oldV.groupCount() > 4 && oldV.group(5) != null) {
                patch = Integer.parseInt(oldV.group(5));
            }
            return new Version(Integer.parseInt(oldV.group(1)), Integer.parseInt(oldV.group(2)), Integer.parseInt(oldV.group(3)), patch, v);
        }
        String[] split = v.split("\\.");
        int major = Integer.MAX_VALUE, minor = Integer.MAX_VALUE, release = Integer.MAX_VALUE, patch = Integer.MAX_VALUE;
        switch (split.length) {
            case 4:
                patch = Integer.parseInt(split[3]);
            case 3:
                release = Integer.parseInt(split[2]);
            case 2:
                minor = Integer.parseInt(split[1]);
            case 1:
                major = Integer.parseInt(split[0]);
                break;
            default:
                throw new IllegalArgumentException("Could not parse " + v);
        }
        return new Version(major, minor, release, patch, v);
    }

    @Override
    public int compareTo(Version o) {
        return VERSION_COMPARATOR.compare(this, o);
    }

    public boolean equalsIgnorePatch(Version o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Version version = (Version) o;
        return major == version.major &&
                minor == version.minor &&
                release == version.release;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Version version = (Version) o;
        return major == version.major &&
                minor == version.minor &&
                release == version.release &&
                patch == version.patch;
    }

    @Override
    public int hashCode() {
        return Objects.hash(major, minor, release, patch);
    }

    @Override
    public String toString() {
        return original;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getRelease() {
        return release;
    }

    public int getPatch() {
        return patch;
    }
}