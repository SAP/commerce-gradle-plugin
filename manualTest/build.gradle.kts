plugins {
    id("mpern.sap.commerce.build") version("SNAPSHOT")
    id("mpern.sap.commerce.build.ccv2") version("SNAPSHOT")
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
    jcenter()
}
