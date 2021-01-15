package mpern.sap.commerce.ccv2.validation.impl;

import static mpern.sap.commerce.ccv2.model.Property.ALLOWED_PERSONAS;

import java.util.*;

import mpern.sap.commerce.ccv2.model.Property;
import mpern.sap.commerce.ccv2.validation.Error;
import mpern.sap.commerce.ccv2.validation.Level;

public class SharedPropertyValidator {

    // https://help.sap.com/viewer/1be46286b36a4aa48205be5a96240672/LATEST/en-US/a30160b786b545959184898b51c737fa.html
    // @formatter:off
    public static final Set<String> MANAGED_PROPERTIES = new HashSet<>(Arrays.asList(
            "db.url",
            "db.driver",
            "db.username",
            "db.password",
            "db.tableprefix",

            "media.read.dir",
            "media.replication.dirs",
            "mediaweb.webroot",
            "media.globalSettings.cloudAzureBlobStorageStrategy.connection",
            "media.globalSettings.cloudAzureBlobStorageStrategy.public.base.url",

            "clustermode",
            "cluster.id",
            "cluster.maxid",
            "cluster.broadcast.methods",
            "cluster.broadcast.method.udp.multicastaddress",
            "cluster.broadcast.method.udp.port",

            "dynatrace.enabled",
            "dynatrace.agentlib",
            "dynatrace.name",
            "dynatrace.server",
            "tomcat.generaloptions.dynatrace",

            "tomcat.generaloptions.jmxsettings",
            "tomcat.jmx.ports",
            "tomcat.jmx.server.port",

            "tomcat.http.port",
            "tomcat.ssl.port",
            "tomcat.ajp.port",
            "tomcat.ajp.secureport",
            "proxy.http.port",
            "proxy.ssl.port",

            "tomcat.generaloptions",
            "java.mem",
            "tomcat.generaloptions.jmxsettings",
            "tomcat.generaloptions.jvmsettings",
            "tomcat.generaloptions.dynatrace",
            "tomcat.generaloptions.GC",

            "log4j.threshold",

            "installed.tenants",
            "tenant.restart.on.connection.error",

            "regionalcache.entityregion.evictionpolicy",
            "regioncache.stats.enabled",
            "cms.cache.enabled",
            "regioncache.entityregion.size",

            "storefront.btg.enabled",
            "storefront.resourceBundle.cacheSeconds",
            "showStorefrontDebugInfo",
            "storefront.show.debug.info",
            "storefront.granule.enabled",
            "storefront.staticResourceFilter.response.header.Cache-Control",
            "addonfilter.active",
            "default.session.timeout",

            "solrserver.instances.default.autostart",

            "datahub.security.https.enabled",

            //backoffice
//            "spring.session.enabled",
//            "spring.session.hac.save",
//            "backofficesearch.cronjob.nodegroup",
//            "spring.session.hac.cookie.name",
//            "spring.session.hac.cookie.path",
//            "task.engine.exclusive.mode",
//            "cluster.node.groups",

            "multicountrysampledataaddon.import.active"
    ));

    public static final Set<String> BACKOFFICE_MANAGED_PROPERTIES = new HashSet<>(Arrays.asList(
            "spring.session.enabled",
            "spring.session.hac.save",
            "backofficesearch.cronjob.nodegroup",
            "spring.session.hac.cookie.name",
            "spring.session.hac.cookie.path",
            "task.engine.exclusive.mode",
            "cluster.node.groups"
    ));

    // @formatter:on

    private final String locationPrefix;

    public SharedPropertyValidator(String locationPrefix) {
        this.locationPrefix = locationPrefix;
    }

    public List<Error> validateProperties(Collection<Property> properties) {
        int index = 0;
        List<Error> errors = new ArrayList<>();
        for (Property property : properties) {
            String location = String.format(locationPrefix + "properties[%d]", index);
            if (!ALLOWED_PERSONAS.contains(property.persona)) {
                errors.add(new Error.Builder().setLocation(location)
                        .setMessage("Persona `%s` not supported", property.persona).setCode("E-008").createError());
            }
            if (MANAGED_PROPERTIES.contains(property.key)
                    || (location.contains("backoffice") && BACKOFFICE_MANAGED_PROPERTIES.contains(property.key))) {
                errors.add(new Error.Builder().setLevel(Level.WARNING).setLocation(location)
                        .setMessage("Property `%s` is a managed property. Are you sure you need to modify it?",
                                property.key)
                        .setCode("W-001").createError());
            }
            index += 1;
        }
        return errors;
    }
}
