package mpern.sap.commerce.ccv2.model;

import java.util.Map;

import static mpern.sap.commerce.ccv2.model.util.ParseUtils.validateNullOrWhitespace;

public class Addon {

    public final String addon;
    public final String storefront;
    public final String template;

    private Addon(String addon, String storefront, String template) {
        this.addon = addon;
        this.storefront = storefront;
        this.template = template;
    }

    public static Addon fromMap(Map<String, Object> jsonMap) {
        return new Addon(
                validateNullOrWhitespace((String) jsonMap.get("addon"), "Addon.addon must have a value"),
                validateNullOrWhitespace((String) jsonMap.get("storefront"), "Addon.storefront must have a value"),
                validateNullOrWhitespace((String) jsonMap.get("template"), "Addon.template must have a value")
        );
    }
}
