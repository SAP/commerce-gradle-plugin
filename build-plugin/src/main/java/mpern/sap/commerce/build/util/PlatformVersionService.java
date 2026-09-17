package mpern.sap.commerce.build.util;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.util.Properties;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.services.BuildService;
import org.gradle.api.services.BuildServiceParameters;

public abstract class PlatformVersionService implements BuildService<PlatformVersionService.Params>, AutoCloseable {

    public interface Params extends BuildServiceParameters {
        DirectoryProperty getPlatformDir();
    }

    public String getVersion() {
        File f = getParameters().getPlatformDir().get().file("build.number").getAsFile();
        if (!f.exists()) {
            return "NONE";
        }
        try {
            String contents = Files.readString(f.toPath());
            Properties props = new Properties();
            props.load(new StringReader(contents));
            return props.getProperty("version", "NONE");
        } catch (IOException e) {
            return "NONE";
        }
    }

    @Override
    public void close() {
    }
}
