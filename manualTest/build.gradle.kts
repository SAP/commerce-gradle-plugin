import mpern.sap.commerce.build.tasks.HybrisAntTask

plugins {
    id("sap.commerce.build") version("SNAPSHOT")
    id("sap.commerce.build.ccv2") version("SNAPSHOT")
}

// val repositoryURL: String by project
// val repositoryUser: String by project
// val repositoryPass: String by project

repositories {
    maven {
        url = uri("https://common.repositories.cloud.sap/sap-cx-commerce-hybris-repository")
        credentials {
            username = System.getenv("SAP_ARTIFACTORY_USER")
            password = System.getenv("SAP_ARTIFACTORY_PASS")
        }
    }
    // flatDir { dirs("platform") }
    mavenCentral()
}

hybris {
    antTaskDependencies.set(listOf("bootstrapPlatform"))

    sparseBootstrap {
        enabled = true
    }
}

tasks.register("testManifestAccess") {
    // Option A
    val ver = CCV2.manifest.map { it.effectiveVersion }
    inputs.property("version", ver)
    // Option B
    val manifest = CCV2.manifest
    doLast {
        val m = manifest.get()
        logger.lifecycle("Version: {}, preview? {}", ver.get(), m.preview)
    }
}
