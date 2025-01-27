plugins {
    id("mpern.commons")
    id("mpern.plugin.basics")
}

dependencies {
    implementation(project(":plugin-commons"))
}

gradlePlugin {

    plugins {
        create("hybrisPlugin") {
            id = "sap.commerce.build"
            implementationClass = "mpern.sap.commerce.build.HybrisPlugin"

            displayName = "SAP Commerce Bootstrap & Build Plugin"
            description = """Manage the whole development lifecycle of your SAP Commerce Project with Gradle"""
            tags =
                setOf(
                    "sap commerce",
                    "sap hybris commerce",
                    "hybris",
                    "sap",
                    "commerce",
                    "bootstrap",
                    "build",
                )
        }
    }
}
