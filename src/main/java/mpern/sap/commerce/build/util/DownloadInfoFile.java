package mpern.sap.commerce.build.util;

import groovy.json.JsonOutput;
import groovy.json.JsonSlurper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.stream.Collectors;

public class DownloadInfoFile {
    private String eTag = "";
    private String lastModified = "";
    private String cachedMd5Hash = "";
    private String originalFilename = "";
    private String downloadUrl = "";
    private String supportUrl = "";
    private String contentLength = "";

    private Path file;

    private DownloadInfoFile() {
    }

    protected DownloadInfoFile(Path p) throws IOException {
        this.file = p;
        readInfoFromFile();
    }

    @SuppressWarnings("unchecked")
    private void readInfoFromFile() throws IOException {
        if (!Files.exists(file)) {
            return;
        }
        JsonSlurper slurper = new JsonSlurper();
        String collect = Files.readAllLines(file, StandardCharsets.UTF_8).stream().collect(Collectors.joining(" "));
        if (collect.isEmpty()) {
            return;
        }
        try {
            Object parse = slurper.parse(collect.getBytes(StandardCharsets.UTF_8), "UTF-8");
            Map m = (Map) parse;
            this.seteTag((String) m.getOrDefault("eTag", ""));
            this.setLastModified((String) m.getOrDefault("lastModified", ""));
            this.setCachedMd5Hash((String) m.getOrDefault("cachedMd5Hash", ""));
            this.setOriginalFilename((String) m.getOrDefault("originalFilename", ""));
            this.setDownloadUrl((String) m.getOrDefault("downloadUrl", ""));
            this.setSupportUrl((String) m.getOrDefault("supportUrl", ""));
            this.setContentLength((String) m.getOrDefault("contentLength", ""));
        } catch (Exception e) {
            //ignore
            e.printStackTrace();
        }
    }

    public String geteTag() {
        return eTag;
    }

    public void seteTag(String eTag) {
        this.eTag = eTag;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public String getCachedMd5Hash() {
        return cachedMd5Hash;
    }

    public void setCachedMd5Hash(String cachedMd5Hash) {
        this.cachedMd5Hash = cachedMd5Hash;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getSupportUrl() {
        return supportUrl;
    }

    public void setSupportUrl(String supportUrl) {
        this.supportUrl = supportUrl;
    }

    public String getContentLength() {
        return contentLength;
    }

    public void setContentLength(String contentLength) {
        this.contentLength = contentLength;
    }

    public void write() throws IOException {
        String s = JsonOutput.prettyPrint(JsonOutput.toJson(this));
        Files.createDirectories(file.getParent());
        Files.write(file, s.getBytes(StandardCharsets.UTF_8), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public static DownloadInfoFile getInfoFor(Path p) throws IOException {
        Path infoFile = infoFileName(p);
        return new DownloadInfoFile(infoFile);
    }

    static Path infoFileName(Path p) {
        Path parent = p.getParent();
        return parent.resolve(p.getFileName() + ".download-info");
    }

    public void clear() {
        eTag = "";
        lastModified = "";
        cachedMd5Hash = "";
        originalFilename = "";
        downloadUrl = "";
        supportUrl = "";
    }
}
