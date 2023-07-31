package mpern.sap.commerce.ccv2.model;

import static mpern.sap.commerce.ccv2.model.util.ParseUtils.emptyOrList;
import static mpern.sap.commerce.ccv2.model.util.ParseUtils.toEmpty;

import java.util.List;
import java.util.Map;

public class Addon {

    public final String addon;
    public final String storefront;
    public final String template;
    public final List<String> addons;
    public final List<String> storefronts;

    private Addon(String addon, String storefront, String template, List<String> addons, List<String> storefronts) {
        this.addon = addon;
        this.storefront = storefront;
        this.template = template;
        this.addons = addons;
        this.storefronts = storefronts;
    }

    @SuppressWarnings("unchecked")
    public static Addon fromMap(Map<String, Object> jsonMap) {
        return new Addon(toEmpty((String) jsonMap.get("addon")), toEmpty((String) jsonMap.get("storefront")),
                toEmpty((String) jsonMap.get("template")), emptyOrList((List<String>) jsonMap.get("addons")),
                emptyOrList((List<String>) jsonMap.get("storefronts")));
    }
}
