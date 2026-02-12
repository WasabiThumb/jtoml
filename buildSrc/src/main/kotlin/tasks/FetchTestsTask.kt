package tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.lang.IllegalStateException

import java.nio.file.Files
import java.nio.file.Path

abstract class FetchTestsTask : DefaultTask() {

    private val workDir: Path
    private val gitMetaDir: Path
    private val outDir: Path

    init {
        val workDir = this.temporaryDir.toPath()
        val gitMetaDir = workDir.resolve(".git")
        val outDir = workDir.resolve("tests")

        this.workDir = workDir
        this.gitMetaDir = gitMetaDir
        this.outDir = outDir
        this.outputs.dir(outDir)
    }

    //

    @TaskAction
    fun execute() {
        if (!Files.exists(this.gitMetaDir)) {
            this.logger.lifecycle("Cloning toml-lang/toml-test")
            this.runGit(this.workDir, "init")
            this.runGit(this.workDir, "remote", "add", "origin", "https://github.com/toml-lang/toml-test")
            this.runGit(this.workDir, "fetch", "origin")
            this.runGit(this.workDir, "checkout", "origin/main", "-ft")
        } else {
            // this.logger.lifecycle("Checking for test suite updates")
            // this.runGit(repo, "fetch", "origin")
        }

        val currentSha = runGit(this.workDir, "rev-parse", "HEAD").first()
        val targetTag = runGit(this.workDir, "tag", "--sort=v:refname").last { it.startsWith("v1") }
        val targetSha = runGit(this.workDir, "rev-list", "-n", "1", targetTag).first()

        if (currentSha == targetSha) {
            this.logger.lifecycle("Test suite is up-to-date")
        } else {
            this.logger.lifecycle("Changing test suite version to $targetTag")
            this.runGit(this.workDir, "checkout", targetSha)
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