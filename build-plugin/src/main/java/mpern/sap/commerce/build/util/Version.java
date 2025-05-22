package mpern.sap.commerce.build.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Version implements Comparable<Version> {
    private static final Pattern COMBINED_VERSION = Pattern
            .compile("(\\d\\d)(\\d\\d)(-jdk(?<jdk>\\d+))?(\\.?(?<variant>FP\\d+|TEST)?)?(\\.(?<patch>\\d+))?");
    private static final Pattern OLD_VERSION = Pattern.compile("(\\d)\\.(\\d)\\.(\\d)(\\.([1-9]?\\d))?");
    public static final int UNDEFINED_PART = Integer.MAX_VALUE;
    public static final Version UNDEFINED = new Version(UNDEFINED_PART, UNDEFINED_PART, UNDEFINED_PART, UNDEFINED_PART,
            UNDEFINED_PART, "<undefined>");
    public static final Comparator<Version> VERSION_COMPARATOR = Comparator.comparingInt(Version::getMajor)
            .thenComparingInt(Version::getJdk).thenComparingInt(Version::getMinor).thenComparingInt(Version::getRelease)
            .thenComparingInt(Version::getPatch);

    private final int major;
    private final int minor;
    private final int release;
    private final int patch;
    private final String original;

    private final boolean preview;
    private final int jdk;

    private Version(int major, int minor, int release, int patch, int jdk, String original) {
        this.major = major;
        this.minor = minor;
        this.release = release;
        this.patch = patch;
        this.jdk = resolveJdkVersion(major, jdk);
        this.original = original;
        this.preview = false;
    }

    private Version(int major, int minor, int patch, boolean preview, int jdk, String original) {
        this.major = major;
        this.minor = minor;
        this.preview = preview;
        this.release = 0;
        this.patch = patch;
        this.jdk = resolveJdkVersion(major, jdk);
        this.original = original;
    }

    private static int resolveJdkVersion(int major, int jdk) {
        if (jdk > 0) {
            return jdk;
        }

        if (major < 19) {
            return 8;
        } else if (major >= 22) {
            return 17;
        }

        return 11;
    }

    public static Version parseVersion(String versionString) {
        return parseVersion(versionString, Collections.emptyMap());
    }

    public static Version parseVersion(String versionString, Map<String, Integer> previewToPlatformPatch) {
        Objects.requireNonNull(versionString);

        Matcher oldV = OLD_VERSION.matcher(versionString);
        Matcher fullV = COMBINED_VERSION.matcher(versionString);

        if (fullV.matches()) {
            String jdkPart = fullV.group("jdk");
            int jdk = 0;

            if (jdkPart != null) {
                try {
                    jdk = Integer.parseInt(jdkPart);
                } catch (NumberFormatException ignored) {
                    // ignore
                }
            }

            String variantPart = fullV.group("variant");
            boolean preview = variantPart != null && variantPart.startsWith("FP");

            if (preview) {
                return new Version(Integer.parseInt(fullV.group(1)), Integer.parseInt(fullV.group(2)),
                        previewToPlatformPatch.getOrDefault(versionString, UNDEFINED_PART), true, jdk, versionString);
            }

            int patch = UNDEFINED_PART;
            String patchPart = fullV.group("patch");

            if (patchPart != null) {
                try {
                    patch = Integer.parseInt(patchPart);
                } catch (NumberFormatException ignored) {
                    // ignore
                }
            }

            return new Version(Integer.parseInt(fullV.group(1)), Integer.parseInt(fullV.group(2)), 0, patch, jdk,
                    versionString);
        } else if (oldV.matches()) {
            int patch = UNDEFINED_PART;
            if (oldV.groupCount() > 4 && oldV.group(5) != null) {
                patch = Integer.parseInt(oldV.group(5));
            }
            return new Version(Integer.parseInt(oldV.group(1)), Integer.parseInt(oldV.group(2)),
                    Integer.parseInt(oldV.group(3)), patch, -1, versionString);
        }
        String[] split = versionString.split("\\.");
        int major, minor = UNDEFINED_PART, release = UNDEFINED_PART, patch = UNDEFINED_PART;
        try {
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
                throw new IllegalArgumentException("Could not parse " + versionString);
            }
            return new Version(major, minor, release, patch, -1, versionString);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Could not parse " + versionString);
        }
    }

    public Version withoutPatch() {
        return new Version(major, minor, release, UNDEFINED_PART, -1, original);
    }

    @Override
    public int compareTo(Version o) {
        return VERSION_COMPARATOR.compare(this, o);
    }

    public boolean equalsIgnorePatch(Version version) {
        if (this == version) {
            return true;
        }
        return major == version.major && minor == version.minor && release == version.release && jdk == version.jdk;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Version version = (Version) o;
        return major == version.major && minor == version.minor && release == version.release && patch == version.patch
                && jdk == version.jdk;
    }

    @Override
    public int hashCode() {
        return Objects.hash(major, minor, release, patch, preview);
    }

    @Override
    public String toString() {
        return this.preview ? buildPreviewString() : this.original;
    }

    private String buildPreviewString() {
        if (this.patch == UNDEFINED_PART) {
            return String.format("%s (PREVIEW)", this.original);
        } else {
            return String.format("%s (PREVIEW) [%d%d.%d]", this.original, this.major, this.minor, this.patch);
        }
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

    public int getJdk() {
        return jdk;
    }

    public String getDependencyVersion() {
        String v = this.original;
        if (this.getPatch() == UNDEFINED_PART) {
            if (!v.endsWith(".")) {
                v += ".";
            }
            v += "+";
        }
        return v;
    }

    public boolean isPreview() {
        return preview;
    }
}
