import pl.allegro.tech.build.axion.release.domain.hooks.HookContext
import java.time.LocalDate

plugins {
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
        pre(
            "fileUpdate",
            mapOf(
                "encoding" to "utf-8",
                "file" to file("CHANGELOG.md"),
                "pattern" to KotlinClosure2({ _: String, _: HookContext -> "(?s)## \\[Unreleased\\]\\R(.*?)\\R## \\[" }),
                "replacement" to KotlinClosure2({ currentVersion: String, _: HookContext ->
                    val releaseDate = LocalDate.now()
                    """
                        ## [Unreleased]
                        <!-- uncomment headings as required -->

                        <!-- ### Added -->
                        <!-- for new features. -->

                        <!-- #### Changed -->
                        <!-- for changes in existing functionality. -->

                        <!-- ### Deprecated -->
                        <!-- for soon-to-be removed features. -->

                        <!-- ### Removed -->
                        <!-- for now removed features. -->

                        <!-- ### Fixed -->
                        <!-- for any bug fixes. -->

                        <!-- ### Security -->
                        <!-- in case of vulnerabilities. -->

                        ## [$currentVersion] $releaseDate
                        ${'$'}1
                        ## [
                    """.trimIndent()
                }),
            ),
        )
        pre(
            "fileUpdate",
            mapOf(
                "encoding" to "utf-8",
                "file" to file("CHANGELOG.md"),
                "pattern" to KotlinClosure2({ _: String, _: HookContext -> "\\[Unreleased\\]: https://github.com/SAP/commerce-gradle-plugin/compare/v([^\\s]+)\\.\\.HEAD" }),
                "replacement" to KotlinClosure2({ currentVersion: String, _: HookContext ->
                    """
                        [Unreleased]: https://github.com/SAP/commerce-gradle-plugin/compare/v$currentVersion..HEAD
                        [$currentVersion]: https://github.com/SAP/commerce-gradle-plugin/compare/v${'$'}1...v$currentVersion
                    """.trimIndent()
                }),
            ),
        )
        pre("commit")
    }
}

project.version = scmVersion.version
