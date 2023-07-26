import pl.allegro.tech.build.axion.release.domain.hooks.HookContext

plugins {
    id("groovy")
    `jvm-test-suite`
    id("com.gradle.plugin-publish")

    id("pl.allegro.tech.build.axion-release")
}

scmVersion {
    localOnly = true
    ignoreUncommittedChanges = true

    checks {
        aheadOfRemote = false
    }
    hooks {
        pre(
            "fileUpdate",
            mapOf(
                "encoding" to "utf-8",
                "file" to file("README.md"),
                "pattern" to KotlinClosure2({ pv: String, _: HookContext -> "$pv" }),
                "replacement" to KotlinClosure2({ cv: String, _: HookContext -> "$cv" }),
            ),
        )
        pre("commit")
    }
}

project.version = scmVersion.version

dependencies {
    implementation(project(":plugin-commons"))
}

testing {
    suites {
        configureEach {
            if (this is JvmTestSuite) {
                useJUnitJupiter()
                dependencies {
                    implementation(platform("org.spockframework:spock-bom:2.2-groovy-3.0"))
                    implementation("org.spockframework:spock-core")

                    implementation(project())
                    implementation(project(":test-utils"))
                }
                targets {
                    all {
                        testTask.configure {
                            testLogging {
                                events("passed", "failed", "skipped", "standardError")
                                showStandardStreams = true
                                showStackTraces = true
                                showExceptions = true
                            }
                        }
                    }
                }
            }
        }

        val integrationTest by registering(JvmTestSuite::class) {
            targets {
                all {
                    testTask.configure {
                        shouldRunAfter("test")
                    }
                }
            }
        }
        val functionalTest by registering(JvmTestSuite::class) {
            targets {
                all {
                    testTask.configure {
                        shouldRunAfter("test", integrationTest)
                    }
                }
            }
        }
    }
}

tasks.named("check") {
    dependsOn(testing.suites.named("integrationTest"), testing.suites.named("functionalTest"))
}

gradlePlugin {
    testSourceSets(sourceSets.get("functionalTest"))

    website = "https://github.com/SAP/commerce-gradle-plugin"
    vcsUrl = "https://github.com/SAP/commerce-gradle-plugin"
}