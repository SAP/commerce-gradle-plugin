package mpern.sap.commerce.build.util;

/**
 * Type of extension.
 */
public enum ExtensionType {

    /**
     * Custom extension.
     */
    CUSTOM,

    /**
     * Extension from the SAP distribution modules.
     */
    SAP_MODULE,

    /**
     * SAP platform extension as a whole.
     */
    SAP_PLATFORM,

    /**
     * Used for all extensions installed already in the system and discovered from
     * the platform system config.
     */
    RUNTIME_INSTALLED
}
