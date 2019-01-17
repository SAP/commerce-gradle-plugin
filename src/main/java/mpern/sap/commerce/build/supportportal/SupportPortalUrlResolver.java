package mpern.sap.commerce.build.supportportal;

import mpern.sap.commerce.build.util.HttpUtils;

import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SupportPortalUrlResolver {

    private URI source;

    private CookieManager ssoCookies;

    public static final URI SUPPORT_PORTAL_API;
    private static final URI DOWNLOAD_ITEM_SET;
    private static final URI HIERARCHY_ITEM_SET;

    static {
        try {
            SUPPORT_PORTAL_API = new URI("https://launchpad.support.sap.com/services/odata/svt/swdcuisrv/");
            DOWNLOAD_ITEM_SET = SUPPORT_PORTAL_API.resolve("DownloadItemSet");
            HIERARCHY_ITEM_SET = SUPPORT_PORTAL_API.resolve("HierarchyItemSet");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private SupportPortalUrlResolver() {
    }

    public static SupportPortalUrlResolver usingCookies(CookieManager cm) {
        SupportPortalUrlResolver d = new SupportPortalUrlResolver();
        d.ssoCookies = cm;
        return d;
    }

    public URI resolve(URI source) throws Exception {
        this.source = source;
        return resolveDownloadLink();
    }

    private URI resolveDownloadLink() throws Exception {
        URI fileList = getUrlToItemList(source);
        Map<String, String> downloadItemSet = fetchFirstODataEntry(fileList);
        String entryId = downloadItemSet.computeIfAbsent("entry-id", s -> {
            throw new IllegalStateException("could not find DownloadItemSet");
        });
        Map<String, String> detailedDownloadItemSet = fetchFirstODataEntry(new URI(entryId));

        String downloadDirectLink = detailedDownloadItemSet.computeIfAbsent("DownloadDirectLink", s -> {
            throw new IllegalStateException("download link not found");
        });

        return new URI(downloadDirectLink);
    }

    private Map<String, String> fetchFirstODataEntry(URI fileList) throws Exception {
        HttpURLConnection connection = HttpUtils.open(fileList, ssoCookies);
        try {
            String body = HttpUtils.readResponseBody(connection);
            List<Map<String, String>> entries = parseODataEntries(body);
            if (entries.isEmpty()) {
                throw new IllegalStateException("Could not find any DownloadItemSet");
            }
            return entries.get(0);
        } finally {
            connection.disconnect();
        }
    }

    private URI getUrlToItemList(URI source) throws Exception {
        validateUrl(source);

        String params = source.getRawFragment().replace("/softwarecenter/template/products/", "");

        URI newUri = new URI(HIERARCHY_ITEM_SET.getScheme(), HIERARCHY_ITEM_SET.getAuthority(),
                HIERARCHY_ITEM_SET.getPath(), params, null);

        HttpURLConnection connection = HttpUtils.open(newUri, ssoCookies);
        try {
            String body = HttpUtils.readResponseBody(connection);
            List<Map<String, String>> entries = parseODataEntries(body);
            Optional<String> listEvent = entries.stream()
                    .map(e -> e.getOrDefault("ListEvent", ""))
                    .filter(l -> !l.isEmpty())
                    .findFirst();
            String listingParams = listEvent.orElseThrow(() -> new IllegalStateException("Could not determine DownloadItemSet for " + newUri));
            listingParams = listingParams.replaceAll("&amp;", "&");

            URI itemSets = new URI(DOWNLOAD_ITEM_SET.getScheme(), DOWNLOAD_ITEM_SET.getAuthority(),
                    DOWNLOAD_ITEM_SET.getPath(), listingParams, DOWNLOAD_ITEM_SET.getFragment());
            return itemSets;
        } finally {
            connection.disconnect();
        }
    }

    private void validateUrl(URI source) {
        if (!source.getHost().equals("launchpad.support.sap.com") || !source.getFragment().startsWith("/softwarecenter/template/products/")) {
            throw new IllegalArgumentException(String.format("can not reslove %s. URI must be of following pattern: https://launchpad.support.sap.com/#/softwarecenter/template/products/...", source));
        }
    }

    List<Map<String, String>> parseODataEntries(String entries) {
        Pattern entryPattern = Pattern.compile("<entry[^>]*?>(.+?)</entry>");
        Pattern propertyPattern = Pattern.compile("<d:(.+?)>(.*?)</d:\\1>");
        List<Map<String, String>> result = new ArrayList<>();
        Matcher m = entryPattern.matcher(entries);
        while (m.find()) {
            Matcher pm = propertyPattern.matcher(m.group(1));
            Map<String, String> properties = new HashMap<>();
            while (pm.find()) {
                properties.put(pm.group(1), pm.group(2));
            }
            Pattern idPattern = Pattern.compile("<id>(.+?)</id>");
            Matcher id = idPattern.matcher(m.group(1));
            if (id.find()) {
                properties.put("entry-id", id.group(1));
            }
            result.add(properties);
        }

        return result;
    }
}
