import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("groovy")
    `jvm-test-suite`
    id("com.gradle.plugin-publish")
    id("com.github.johnrengelman.shadow")
}

dependencies {
    implementation(project(":plugin-commons"))
}

testing {
    suites {
        configureEach {
            if (this is JvmTestSuite) {
                useJUnitJupiter()
                dependencies {
                    implementation(platform("org.spockframework:spock-bom:2.3-groovy-3.0"))
                    implementation("org.spockframework:spock-core")

                    implementation(project())
                    implementation(project(":test-utils"))

                    implementation(gradleTestKit())
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

        register("functionalTest", JvmTestSuite::class) {
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

tasks {
    named<ShadowJar>("shadowJar") {
        archiveClassifier = ""
    }
}
