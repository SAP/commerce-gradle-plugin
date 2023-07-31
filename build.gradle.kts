import pl.allegro.tech.build.axion.release.domain.hooks.HookContext

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
        pre("commit")
    }
}

project.version = scmVersion.version
