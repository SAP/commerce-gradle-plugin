package mpern.sap.commerce.build.util;

import java.net.CookieManager;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import mpern.sap.commerce.build.supportportal.SSOLogin;

public class SSOCredentialsCache {
    // maybe build a thread-safe memoizer?
    private Map<String, CookieManager> cache = new HashMap<>();

    public CookieManager getCookiesFor(String username, String password, URI uri) {
        String key = buildKey(username, password, uri);

        return cache.computeIfAbsent(key, k -> {
            try {
                return SSOLogin.withCredentails(username, password).forResource(uri);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private String buildKey(String username, String password, URI uri) {
        return String.format("%s|%s|%s", username, password, uri);
    }
}
