package mpern.sap.commerce.build.tasks;

import mpern.sap.commerce.build.supportportal.SupportPortalUrlResolver;
import mpern.sap.commerce.build.util.DownloadInfoFile;
import mpern.sap.commerce.build.util.HashUtil;
import mpern.sap.commerce.build.util.HttpUtils;
import mpern.sap.commerce.build.util.PercentageProgressWriter;
import mpern.sap.commerce.build.util.SSOCredentialsCache;
import org.gradle.api.DefaultTask;
import org.gradle.api.Task;
import org.gradle.api.execution.TaskExecutionGraph;
import org.gradle.api.execution.TaskExecutionGraphListener;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;

import java.io.IOException;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SupportPortalDownload extends DefaultTask {

    private final Property<String> supportPortalUrl;
    private final Property<String> username;
    private final Property<String> password;
    private final RegularFileProperty targetFile;
    private final Property<String> md5Hash;
    private SSOCredentialsCache credentialsCache;

    public SupportPortalDownload() {
        supportPortalUrl = getProject().getObjects().property(String.class);
        username = getProject().getObjects().property(String.class);
        password = getProject().getObjects().property(String.class);
        md5Hash = getProject().getObjects().property(String.class);
        targetFile = newOutputFile();

        Spec<Task> hashesMatch = t -> {
            try {
                Path target = targetFile.get().getAsFile().toPath();
                if (!Files.exists(target)) {
                    return false;
                }
                DownloadInfoFile infoFile = getInfoFileForTarget();
                String expectedHash = md5Hash.get();
                String fileHash;
                if (!infoFile.getCachedMd5Hash().isEmpty()) {
                    fileHash = infoFile.getCachedMd5Hash();
                    getLogger().lifecycle("Using cached hash to compare", fileHash);
                } else {
                    getLogger().lifecycle("No valid hash found for {}, recalculating...", target.getFileName());
                    fileHash = HashUtil.md5Hash(target);
                    infoFile.setCachedMd5Hash(fileHash);
                    infoFile.write();
                }
                getLogger().lifecycle("{}: Expected md5: {} File md5: {}", target.getFileName(), expectedHash, fileHash);
                return fileHash.equals(expectedHash);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
        getOutputs().upToDateWhen(hashesMatch);
        onlyIf(t -> !hashesMatch.isSatisfiedBy(t));
    }

    private DownloadInfoFile getInfoFileForTarget() throws Exception {
        Path target = targetFile.get().getAsFile().toPath();
        DownloadInfoFile infoFile = DownloadInfoFile.getInfoFor(target);
        if (!infoFile.getSupportUrl().equals(supportPortalUrl.get())) {
            infoFile.clear();
        }
        if (!Files.exists(target)) {
            infoFile.setCachedMd5Hash("");
        }
        infoFile.setSupportUrl(supportPortalUrl.get());
        return infoFile;
    }

    @TaskAction
    public void downloadFile() {
        DownloadInfoFile infoFile = null;
        try {
            Path targetPath = targetFile.get().getAsFile().toPath();
            infoFile = getInfoFileForTarget();
            URI download;
            if (infoFile.getDownloadUrl().isEmpty()) {
                CookieManager ssoCookies = credentialsCache.getCookiesFor(username.get(), password.get(), SupportPortalUrlResolver.SUPPORT_PORTAL_API);
                download = SupportPortalUrlResolver.usingCookies(ssoCookies).resolve(new URI(supportPortalUrl.get()));
                infoFile.setDownloadUrl(download.toString());
            } else {
                download = new URI(infoFile.getDownloadUrl());
            }

            CookieManager cookies = new CookieManager();
            HttpURLConnection connection = HttpUtils.open(download, cookies);
            addHttpAuthHeader(connection);
            connection = HttpUtils.connectAndUpdateCookies(connection, cookies, connection.getRequestProperties());

            updateMetaData(connection.getHeaderFields(), infoFile);

            Path tempDownloadFile = targetPath.getParent().resolve(targetPath.getFileName() + ".progress");
            PercentageProgressWriter progressWriter = new PercentageProgressWriter("Downloading " + targetPath.getFileName(), Long.parseLong(infoFile.getContentLength()));
            downloadFile(connection, tempDownloadFile, progressWriter);

            Files.move(tempDownloadFile, targetPath, StandardCopyOption.REPLACE_EXISTING);
            ZonedDateTime reportedLastModified = ZonedDateTime.parse(infoFile.getLastModified(), DateTimeFormatter.RFC_1123_DATE_TIME);
            Files.setLastModifiedTime(targetPath, FileTime.from(reportedLastModified.toInstant()));

            String hash = HashUtil.md5Hash(targetPath);
            infoFile.setCachedMd5Hash(hash);
            String expectedHash = md5Hash.get();
            if (!hash.equals(expectedHash)) {
                throw new IllegalStateException(String.format("Download of %s not successful. Hashes don't match. Found: %s Expected: %s", targetPath.getFileName(), hash, expectedHash));
            }
        } catch (Exception e) {
            throw new TaskExecutionException(this, e);
        } finally {
            if (infoFile != null) {
                try {
                    infoFile.write();
                } catch (IOException e) {
                    //ignore
                }
            }
        }
    }

    private void updateMetaData(Map<String, List<String>> headerFields, DownloadInfoFile infoFile) {
        infoFile.seteTag(getFirstOrEmpty(headerFields.get("ETag")));
        infoFile.setLastModified(getFirstOrEmpty(headerFields.get("Last-Modified")));
        infoFile.setOriginalFilename(parseFilename(getFirstOrEmpty(headerFields.get("Content-Disposition"))));
        infoFile.setContentLength(getFirstOrEmpty(headerFields.get("Content-Length")));
    }

    private String getFirstOrEmpty(List<String> values) {
        String value = "";
        if (!values.isEmpty()) {
            value = values.get(0);
        }
        return value;
    }

    private void downloadFile(HttpURLConnection connection, Path tempDownloadFile, PercentageProgressWriter progressWriter) throws IOException {
        progressWriter.start();
        try (ReadableByteChannel input = Channels.newChannel(connection.getInputStream());
             FileChannel output = FileChannel.open(tempDownloadFile, EnumSet.of(StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))) {
            ByteBuffer transferBuffer = ByteBuffer.allocateDirect(512 * 1024);
            int bytesRead = 0;
            long total = 0;
            while ((bytesRead = input.read(transferBuffer)) != -1) {
                transferBuffer.flip();
                output.write(transferBuffer);
                transferBuffer.compact();
                total += bytesRead;

                progressWriter.logProgress(total);
            }
        }
        progressWriter.finish();
    }

    private String parseFilename(String headerField) {

        if (headerField == null || headerField.isEmpty()) {
            return "";
        }
        Pattern file = Pattern.compile("filename=\"(.+)\"");
        Matcher matcher = file.matcher(headerField);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return "";
        }
    }

    private void addHttpAuthHeader(HttpURLConnection connection) {
        String d = String.format("%s:%s", username.get(), password.get());
        String encoding = Base64.getEncoder().encodeToString(d.getBytes(StandardCharsets.UTF_8));
        connection.setRequestProperty("Authorization", "Basic " + encoding);
    }

    @Internal
    public SSOCredentialsCache getCredentialsCache() {
        return credentialsCache;
    }

    public void setCredentialsCache(SSOCredentialsCache credentialsCache) {
        this.credentialsCache = credentialsCache;
    }


    @Input
    public Property<String> getSupportPortalUrl() {
        return supportPortalUrl;
    }

    @Input
    public Property<String> getUsername() {
        return username;
    }

    @Input
    public Property<String> getPassword() {
        return password;
    }

    @OutputFile
    public RegularFileProperty getTargetFile() {
        return targetFile;
    }

    @Input
    public Property<String> getMd5Hash() {
        return md5Hash;
    }

    public static final class ConfigureSupportPortalDownload implements TaskExecutionGraphListener {

        private CookieManager ssoCookies;
        private SSOCredentialsCache ssoCredentialsCache;

        @Override
        public void graphPopulated(TaskExecutionGraph graph) {
            List<SupportPortalDownload> downloadTasks = graph.getAllTasks().stream().filter(t -> t instanceof SupportPortalDownload).map(t -> (SupportPortalDownload) t).collect(Collectors.toList());
            if (!downloadTasks.isEmpty()) {
                ssoCredentialsCache = new SSOCredentialsCache();
                downloadTasks.forEach(t -> t.setCredentialsCache(ssoCredentialsCache));
            }
        }
    }
}
