import mpern.sap.commerce.build.tasks.HybrisAntTask

plugins {
    id("sap.commerce.build") version("SNAPSHOT")
    id("sap.commerce.build.ccv2") version("SNAPSHOT")
}

val repositoryURL: String by project
val repositoryUser: String by project
val repositoryPass: String by project

repositories {
    maven {
        url = uri(repositoryURL)
        credentials {
            username = repositoryUser
            password = repositoryPass
        }
    }
    flatDir { dirs("platform") }
    mavenCentral()
}

hybris {
    antTaskDependencies.set(listOf("bootstrapPlatform"))

    sparseBootstrap {
        enabled = true
    }
}

tasks.register("testManifestAccess") {
    doLast {
        logger.lifecycle("Version: {}, preview? {}", CCV2.manifest.effectiveVersion, CCV2.manifest.preview)
    }
}