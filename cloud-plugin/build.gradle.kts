plugins {
    id("mpern.commons")
    id("mpern.plugin.basics")
}

dependencies {
    implementation(project(":build-plugin"))
    integrationTestImplementation(project(":build-plugin"))
}

gradlePlugin {
    plugins {
        create("ccv2BuildSupport") {
            id = "sap.commerce.build.ccv2"
            implementationClass = "mpern.sap.commerce.ccv2.CloudV2Plugin"

            displayName = "SAP Commerce Cloud in the Public Cloud Build Support Plugin"
            description = """Use the CCv2 manifest.json to configure and build your local development environment"""
            tags =
                setOf(
                    "sap commerce",
                    "sap hybris commerce",
                    "hybris",
                    "sap",
                    "commerce",
                    "ccv2",
                    "public cloud",
                    "manifest",
                )
        }
    }
}
