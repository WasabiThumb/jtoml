package tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.Properties

@CacheableTask
abstract class GeneratePackageMetadataTask : DefaultTask() {

    @get:Input
    protected abstract val libraryVersion: Property<String>

    @get:Input
    protected abstract val gitBranch: Property<String>

    @get:Input
    protected abstract val gitCommit: Property<String>

    @get:Input
    protected abstract val ci: Property<Boolean>

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val licenseFile: RegularFileProperty

    @get:OutputDirectory
    abstract val outDir: DirectoryProperty

    init {
        this.libraryVersion.convention(this.project.provider { "${this.project.rootProject.version}" })
        this.licenseFile.convention { this.project.rootProject.file("LICENSE.txt") }
        this.outDir.convention(this.project.layout.buildDirectory.dir("tmp/${this.name}"))
        this.ci.convention(this.project.providers.environmentVariable("CI").map { truthyEnv(it) }.orElse(false))
        val ext = this.project.extensions.findByName("indraGit")
        if (ext == null) {
            this.logger.warn("No indraGit extension, cannot write git info")
        } else {
            this.gitBranch.convention(gitBranchFromIndraGitExtension(ext))
            this.gitCommit.convention(gitCommitFromIndraGitExtension(ext))
        }
    }

    //

    @TaskAction
    fun generate() {
        val dest = this.outDir.asFile.get().toPath()
        if (!Files.exists(dest)) Files.createDirectories(dest)

        // Copy LICENSE.txt
        val licenseDest = dest.resolve("LICENSE.txt")
        Files.copy(this.licenseFile.asFile.get().toPath(), licenseDest, StandardCopyOption.REPLACE_EXISTING)

        // Create meta.properties
        val propertiesDest = dest.resolve("meta.properties")
        val properties = createMetaProperties()
        Files.newBufferedWriter(propertiesDest, Charsets.UTF_8).use { w ->
            properties.store(w, "Metadata for JToml (https://github.com/WasabiThumb/jtoml)")
        }
    }

    private fun createMetaProperties(): Properties {
        val ret = Properties()
        ret.setProperty(PROPERTY_LIBRARY_VERSION, this.libraryVersion.get())
        if (this.ci.get()) {
            ret.setProperty(PROPERTY_BUILD_CI, "true")
        } else {
            ret.setProperty(PROPERTY_BUILD_CI, "false")
            ret.setProperty(PROPERTY_VCS_BRANCH, this.gitBranch.get())
            ret.setProperty(PROPERTY_VCS_COMMIT, this.gitCommit.get())
        }
        return ret
    }

    //

    companion object {

        private const val PROPERTY_LIBRARY_VERSION = "library.version"
        private const val PROPERTY_BUILD_CI = "build.ci"
        private const val PROPERTY_VCS_BRANCH = "vcs.branch"
        private const val PROPERTY_VCS_COMMIT = "vcs.commit"

        private fun truthyEnv(value: String?): Boolean {
            if (value.isNullOrBlank()) return false
            if ("0" == value) return false
            return !"false".contentEquals(value, ignoreCase = true)
        }

        // The right thing to do would be to just add indraGit
        // to the buildSrc classpath, but I would rather not
        // try to figure that out.

        @Suppress("UNCHECKED_CAST")
        private fun gitBranchFromIndraGitExtension(ext: Any): Provider<String> {
            val cls = ext.javaClass
            val method = cls.getMethod("branchName")
            return method.invoke(ext) as Provider<String>
        }

        @Suppress("UNCHECKED_CAST")
        private fun gitCommitFromIndraGitExtension(ext: Any): Provider<String> {
            val cls = ext.javaClass
            val method = cls.getMethod("commit")
            val objectId = method.invoke(ext) as Provider<*>
            return objectId.map { gitCommitFromObjectId(it) }
        }

        @Suppress("UNCHECKED_CAST")
        private fun gitCommitFromObjectId(id: Any): String {
            val cls = id.javaClass
            val method = cls.getMethod("name")
            return method.invoke(id) as String
        }

    }

}