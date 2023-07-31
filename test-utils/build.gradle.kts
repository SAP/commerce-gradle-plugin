plugins {
    id("mpern.commons")
}

val generated = "$buildDir/genSrc/main/java"

sourceSets["main"].java {
    srcDir(generated)
}

val generateSources by tasks.registering {

    inputs.property("projectDir", project.projectDir)

    outputs.dir(generated)

    doFirst {
        val structure = file("$generated/mpern/sap/commerce/test/TestConstants.java")
        val resourcesDir = project.projectDir.resolve("src/main/resources").toString().replace("\\", "\\\\")
        structure.parentFile.mkdirs()
        structure.writeText(
            """
        package mpern.sap.commerce.test;
        
        import java.nio.file.Path;

        public class TestConstants {
        
            public static final Path TEST_RESOURCES = Path.of("$resourcesDir");

            public static Path testResource(String fileOrFolder) {
                return TEST_RESOURCES.resolve(fileOrFolder);
            }
        }
        """.stripIndent(),
        )
    }
}

tasks.named("compileJava") {
    dependsOn(generateSources)
}
