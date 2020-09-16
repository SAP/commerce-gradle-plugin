package mpern.sap.commerce.build.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HttpUtils {

    public static HttpURLConnection connectAndUpdateCookies(HttpURLConnection connection, CookieManager cookies)
            throws Exception {
        return connectAndUpdateCookies(connection, cookies, Collections.emptyMap());
    }

    public static HttpURLConnection connectAndUpdateCookies(HttpURLConnection connection, CookieManager cookies,
            Map<String, List<String>> headers) throws Exception {
        connection.connect();
        URI uri = connection.getURL().toURI();
        cookies.put(uri, connection.getHeaderFields());
        while (isRedirect(connection)) {
            connection = followRedirect(connection, cookies, headers);
            cookies.put(connection.getURL().toURI(), connection.getHeaderFields());
        }
        if (connection.getResponseCode() > 400) {
            throw new IllegalStateException(
                    "error connecting to " + connection.getURL() + " HTTP Status: " + connection.getResponseCode());
        }
        return connection;
    }

    private static boolean isRedirect(HttpURLConnection connection) throws Exception {
        return connection.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM
                || connection.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP
                || connection.getResponseCode() == HttpURLConnection.HTTP_SEE_OTHER;
    }

    private static HttpURLConnection followRedirect(final HttpURLConnection connection, CookieManager cookies,
            Map<String, List<String>> headers) throws Exception {
        String location = connection.getHeaderField("Location");
        URI newUri = connection.getURL().toURI().resolve(location);
        HttpURLConnection newConnection = open(newUri, cookies);
        headers.forEach((k, v) -> v.forEach(s -> {
            newConnection.addRequestProperty(k, s);
        }));
        return newConnection;
    }

    public static HttpURLConnection open(URI uri, CookieManager cookieManager) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setUseCaches(false);
        connection.setInstanceFollowRedirects(false);

        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        List<HttpCookie> cookies = cookieManager.getCookieStore().get(uri);
        String param = cookies.stream().map(HttpCookie::toString).collect(Collectors.joining("; "));
        connection.addRequestProperty("Cookie", param);

        return connection;
    }

    public static String readResponseBody(HttpURLConnection connection) throws Exception {
        String collect;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            collect = br.lines().collect(Collectors.joining());
        }

        return collect;
    }

    public static String urlFormEncode(Map<String, String> params) {
        return params.entrySet().stream().map(e -> {
            try {
                return URLEncoder.encode(e.getKey(), "UTF-8") + "=" + URLEncoder.encode(e.getValue(), "UTF-8");
            } catch (UnsupportedEncodingException e1) {
                throw new RuntimeException(e1);
            }
        }).collect(Collectors.joining("&"));
    }

    public static void postFormData(String formEncoded, HttpURLConnection next) throws IOException {
        next.setRequestMethod("POST");
        next.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        next.setRequestProperty("charset", "utf-8");
        byte[] postData = formEncoded.getBytes(StandardCharsets.UTF_8);
        int postDataLength = postData.length;
        next.setRequestProperty("Content-Length", Integer.toString(postDataLength));
        try (DataOutputStream wr = new DataOutputStream(next.getOutputStream())) {
            wr.write(postData);
        }
    }
}
