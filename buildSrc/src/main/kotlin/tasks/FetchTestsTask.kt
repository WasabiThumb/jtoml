package tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.lang.IllegalStateException

import java.nio.file.Files
import java.nio.file.Path

@CacheableTask
abstract class FetchTestsTask : DefaultTask() {

    @get:Input
    abstract val versionPattern: Property<Regex>

    @get:Internal
    protected abstract val workDir: DirectoryProperty

    @get:OutputDirectory
    protected abstract val outDir: DirectoryProperty

    init {
        val work = project.layout.buildDirectory.dir(this.name)
        this.versionPattern.convention(Regex("^v2\\..*"))
        this.workDir.convention(work)
        this.outDir.convention(work.map { it.dir("tests") })
    }

    //

    @TaskAction
    fun execute() {
        val versionPattern = this.versionPattern.get()
        val workDir = this.workDir.get().asFile.toPath()
        val gitMetaDir = workDir.resolve(".git")

        if (!Files.exists(gitMetaDir)) {
            this.logger.lifecycle("Cloning toml-lang/toml-test")
            this.runGit(workDir, "init")
            this.runGit(workDir, "remote", "add", "origin", "https://github.com/toml-lang/toml-test")
            this.runGit(workDir, "fetch", "origin")
            this.runGit(workDir, "checkout", "origin/main", "-ft")
        } else {
            this.runGit(workDir, "fetch", "origin")
        }

        val currentSha = runGit(workDir, "rev-parse", "HEAD").first()
        val targetTag = runGit(workDir, "tag", "--sort=v:refname").last { versionPattern.matches(it) }
        val targetSha = runGit(workDir, "rev-list", "-n", "1", targetTag).first()

        if (currentSha == targetSha) {
            this.logger.lifecycle("Test suite is up-to-date")
        } else {
            this.logger.lifecycle("Changing test suite version to $targetTag")
            this.runGit(workDir, "checkout", targetSha)
        }
    }

    private fun runGit(dir: Path, vararg args: String): List<String> {
        val output: MutableList<String> = mutableListOf()
        val process = ProcessBuilder(listOf("git", *args))
            .redirectErrorStream(true)
            .directory(dir.toFile())
            .start()

        process.inputReader(Charsets.UTF_8).use { reader ->
            var line: String?
            while (true) {
                line = reader.readLine()
                if (line == null) break
                if (line.isBlank()) continue
                output.add(line)
            }
        }

        val ex: Int = process.waitFor()
        if (ex != 0)
            throw IllegalStateException("\"git ${args.joinToString(" ")}\" exited with non-zero status code $ex")

        return output
    }

}