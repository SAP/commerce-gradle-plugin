rootProject.name = "commerce-gradle-plugin"
include("test-utils")
include("plugin-commons")
include("build-plugin")
include("cloud-plugin")

enableFeaturePreview("NO_IMPLICIT_LOOKUP_IN_PARENT_PROJECTS")
enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
