package mpern.sap.commerce.build.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import groovy.lang.Tuple2;

public class PlatformResolver {
    private final Path platformHome;

    public PlatformResolver(Path platformHome) {
        this.platformHome = platformHome;
    }

    private Optional<URLClassLoader> bootstrapClassLoader() throws Exception {
        Path bootstrap = platformHome.resolve("bootstrap");
        if (!Files.exists(bootstrap)) {
            return Optional.empty();
        }
        List<Path> entries = Files.list(bootstrap.resolve("bin")).collect(Collectors.toCollection(ArrayList::new));
        entries.add(bootstrap.resolve("resources"));
        try {
            URL[] classpathEntries = entries.stream().map(p -> {
                try {
                    return p.toUri().toURL();
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            }).toArray(URL[]::new);
            return Optional.of(new URLClassLoader(classpathEntries));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Tuple2<Class<?>, Object> platformConfig(URLClassLoader classLoader) throws Exception {
        Class<?> systemConfigClass = classLoader.loadClass("de.hybris.bootstrap.config.SystemConfig");
        Field f = systemConfigClass.getDeclaredField("singleton");
        f.setAccessible(true);
        f.set(null, null);

        Class<?> configUtilClass = classLoader.loadClass("de.hybris.bootstrap.config.ConfigUtil");
        Method loadConfig = configUtilClass.getDeclaredMethod("getSystemConfig", String.class);
        Object systemConfig = loadConfig.invoke(null, platformHome.toAbsolutePath().toString());

        Class<?> platformConfigClass = classLoader.loadClass("de.hybris.bootstrap.config.PlatformConfig");
        Field platformInstance = platformConfigClass.getDeclaredField("instance");
        platformInstance.setAccessible(true);
        platformInstance.set(null, null);

        Method loadPlatformConfig = platformConfigClass.getDeclaredMethod("getInstance", systemConfigClass);
        Object config = loadPlatformConfig.invoke(null, systemConfig);
        return new Tuple2<>(platformConfigClass, config);
    }

    public List<Extension> getAllAvailableExtensions() throws Exception {
        Optional<URLClassLoader> urlClassLoader = bootstrapClassLoader();
        if (urlClassLoader.isPresent()) {
            try {
                System.setProperty("platform.extensions", "*");
                Tuple2<Class<?>, Object> platformConfig = platformConfig(urlClassLoader.get());
                List<?> extensions = (List<?>) platformConfig.getFirst()
                        .getDeclaredMethod("getExtensionInfosInBuildOrder").invoke(platformConfig.getSecond());
                return convertToExtensionInfo(urlClassLoader.get(), extensions);
            } finally {
                System.clearProperty("platform.extensions");
            }
        } else {
            return Collections.emptyList();
        }
    }

    public List<Extension> getConfiguredExtensions() throws Exception {
        Optional<URLClassLoader> urlClassLoader = bootstrapClassLoader();
        if (urlClassLoader.isPresent()) {
            Tuple2<Class<?>, Object> platformConfig = platformConfig(urlClassLoader.get());
            List<?> extensions = (List<?>) platformConfig.getFirst().getDeclaredMethod("getExtensionInfosInBuildOrder")
                    .invoke(platformConfig.getSecond());
            return convertToExtensionInfo(urlClassLoader.get(), extensions);
        } else {
            return Collections.emptyList();
        }
    }

    private List<Extension> convertToExtensionInfo(URLClassLoader classLoader, List<?> raw) throws Exception {
        Class<?> extensionInfoClass = classLoader.loadClass("de.hybris.bootstrap.config.ExtensionInfo");
        Method getName = extensionInfoClass.getDeclaredMethod("getName");
        Method getDirectory = extensionInfoClass.getDeclaredMethod("getExtensionDirectory");

        return raw.stream().map(o -> {
            try {
                String name = (java.lang.String) getName.invoke(o);
                Path dir = ((java.io.File) getDirectory.invoke(o)).toPath();
                return new Extension(name, dir);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
    }
}
