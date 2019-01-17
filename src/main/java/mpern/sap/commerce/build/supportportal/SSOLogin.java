package mpern.sap.commerce.build.supportportal;

import mpern.sap.commerce.build.util.HttpUtils;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SSOLogin {

    private static Logger LOG = Logging.getLogger(SSOLogin.class);

    private String user;
    private String password;
    private CookieManager ssoCookies;

    private static final Pattern FORM_TAG = Pattern.compile("(?Ui)<form(.+?)>");
    private static final Pattern INPUT_TAG = Pattern.compile("(?Ui)<input(.+?)/?>");
    private static final Pattern HTML_ATTRIBUTE = Pattern.compile("(?Ui)(\\S+?)=[\"'](.*?)[\"']");

    private SSOLogin() {
    }

    public static SSOLogin withCredentails(String user, String password) {
        Objects.requireNonNull(user, "user must not be null");
        Objects.requireNonNull(password, "user must not be null");
        SSOLogin ssoLogin = new SSOLogin();
        ssoLogin.user = user;
        ssoLogin.password = password;

        ssoLogin.ssoCookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);

        return ssoLogin;
    }

    public CookieManager forResource(URI uri) throws Exception {

        //try to get ressource
        HttpURLConnection connection = HttpUtils.open(uri, ssoCookies);
        connection.setRequestMethod("GET");
        connection = HttpUtils.connectAndUpdateCookies(connection, ssoCookies);
        String body = HttpUtils.readResponseBody(connection);
        URI previousRequest = uri;
        connection.disconnect();

        //https://authn.hana.ondemand.com/saml2/sp/mds
        String action = parsePostAction(body);
        URI nextStep = previousRequest.resolve(action);
        body = performAutoPost(body, nextStep);
        previousRequest = nextStep;

        //https://accounts.sap.com/saml2/idp/sso/accounts.sap.com
        action = parsePostAction(body);
        nextStep = previousRequest.resolve(action);
        body = performAutoPost(body, nextStep);
        previousRequest = nextStep;

        //https://accounts.sap.com/saml2/idp/sso/accounts.sap.com - SSO Login!
        action = parsePostAction(body);
        nextStep = previousRequest.resolve(action);
        body = submitLogin(body, nextStep);
        previousRequest = nextStep;

        validateSuccessfulLogin(body);

        //SAML Response + redirect to target
        action = parsePostAction(body);
        nextStep = previousRequest.resolve(action);
        body = performAutoPost(body, nextStep);

        //and one final redirect to the originally requested resource. Now we are logged in
        action = parsePostAction(body);
        nextStep = previousRequest.resolve(action);
        body = performAutoPost(body, nextStep);

        validateFinishedFlow(body);

        return ssoCookies;
    }

    private String submitLogin(String body, URI nextStep) throws Exception {
        Map<String, String> requestFields = parseHiddenFields(body);
        requestFields.put("j_username", user);
        requestFields.put("j_password", password);
        String urlEncodedForm = HttpUtils.urlFormEncode(requestFields);
        HttpURLConnection loginRequest = HttpUtils.open(nextStep, ssoCookies);
        HttpUtils.postFormData(urlEncodedForm, loginRequest);
        loginRequest = HttpUtils.connectAndUpdateCookies(loginRequest, ssoCookies);
        body = HttpUtils.readResponseBody(loginRequest);
        loginRequest.disconnect();
        return body;
    }

    private void validateFinishedFlow(String body) {
        if (body.contains("https://authn.hana.ondemand.com/saml2")) {
            throw new IllegalStateException("Was not able to log in, final redirect to resource restarted SAML flow :(");
        }
    }

    private void validateSuccessfulLogin(String body) {
        Matcher matcher = FORM_TAG.matcher(body);
        if (matcher.find()) {
            Map<String, String> attributes = parseAttributes(matcher.group(1));
            String formId = attributes.getOrDefault("id", "UNDEFINED");
            if (!"samlRedirect".equals(formId)) {
                throw new IllegalStateException("could not find SAML redirect form after login. Wrong credentials?");
            }
        } else {
            throw new IllegalStateException("could not find SAML redirect form after login");
        }
    }

    private String performAutoPost(String body, URI nextSSOStep) throws Exception {
        Map<String, String> postRequest = parseHiddenFields(body);
        String formEncoded = HttpUtils.urlFormEncode(postRequest);
        HttpURLConnection next = HttpUtils.open(nextSSOStep, ssoCookies);
        HttpUtils.postFormData(formEncoded, next);
        next = HttpUtils.connectAndUpdateCookies(next, ssoCookies);
        body = HttpUtils.readResponseBody(next);
        next.disconnect();
        return body;
    }

    private Map<String, String> parseHiddenFields(String body) {
        Matcher input = INPUT_TAG.matcher(body);
        Map<String, String> hiddenInputs = new HashMap<>();
        while (input.find()) {
            Map<String, String> attributes = parseAttributes(input.group(1));
            if ("hidden".equalsIgnoreCase(attributes.getOrDefault("type", "UNKNOWN"))) {
                hiddenInputs.put(attributes.get("name"), attributes.get("value"));
            }
        }
        return hiddenInputs;
    }

    private Map<String, String> parseAttributes(String t) {
        Matcher a = HTML_ATTRIBUTE.matcher(t);
        Map<String, String> attributes = new HashMap<>();
        while (a.find()) {
            attributes.put(a.group(1).toLowerCase(), a.group(2));
        }
        return attributes;
    }

    private String parsePostAction(String body) throws Exception {
        Matcher matcher = FORM_TAG.matcher(body);

        if (matcher.find()) {
            String tagBody = matcher.group(1);
            Map<String, String> attributes = parseAttributes(tagBody);
            return attributes.computeIfAbsent("action", k -> {
                throw new IllegalStateException("could not find 'action'");
            });
        } else {
            throw new IllegalStateException("could not find <form> tag!");
        }
    }

}
