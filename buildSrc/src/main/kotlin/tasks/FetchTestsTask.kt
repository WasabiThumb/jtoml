package tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.lang.IllegalStateException
import java.lang.UnsupportedOperationException

import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.name

abstract class FetchTestsTask : DefaultTask() {

    @get:OutputDirectory
    abstract val outDir: DirectoryProperty

    private val tempDir: Path by lazy {
        val dir = this.project.layout.buildDirectory.get()
            .asFile
            .toPath()
            .resolve("tmp")
            .resolve(this.name)

        if (!Files.exists(dir))
            Files.createDirectories(dir)

        dir
    }

    //

    @TaskAction
    fun execute() {
        val dir = this.tempDir
        val repo = this.tempDir.resolve("toml-test")

        if (!Files.exists(repo)) {
            this.logger.lifecycle("Cloning toml-lang/toml-test")
            this.runGit(dir, "clone", "https://github.com/toml-lang/toml-test")
        } else {
            // this.logger.lifecycle("Checking for test suite updates")
            // this.runGit(repo, "fetch", "origin")
        }

        val currentSha = runGit(repo, "rev-parse", "HEAD").first()
        val targetTag = runGit(repo, "tag", "--sort=v:refname").last()
        val targetSha = runGit(repo, "rev-list", "-n", "1", targetTag).first()

        if (currentSha == targetSha) {
            this.logger.lifecycle("Test suite is up-to-date")
        } else {
            this.logger.lifecycle("Changing test suite version to $targetTag")
            this.runGit(repo, "checkout", targetSha)
        }

        val srcDir = repo.resolve("tests")
        val targetDir = this.outDir.get().asFile.toPath().resolve("tests")

        if (!Files.exists(srcDir)) {
            throw Error("\"tests\" directory not found in repository")
        }

        // Either symlink or copy src to target
        if (Files.exists(targetDir)) this.nuke(targetDir)
        try {
            Files.createSymbolicLink(targetDir, srcDir)
        } catch (e: UnsupportedOperationException) {
            this.logger.warn("Failed to create symbolic link, making full copy")
            this.copyDir(srcDir, targetDir)
        }
    }

    private fun copyDir(src: Path, target: Path) {
        if (!Files.exists(target))
            Files.createDirectories(target)

        Files.list(src).use { stream ->
            val iter = stream.iterator()
            while (iter.hasNext()) {
                val ent = iter.next()
                if (Files.isSymbolicLink(ent)) {
                    Files.createSymbolicLink(target.resolve(ent.name), Files.readSymbolicLink(ent))
                } else if (Files.isDirectory(ent, LinkOption.NOFOLLOW_LINKS)) {
                    this.copyDir(ent, target.resolve(ent.name))
                } else {
                    Files.copy(ent, target.resolve(ent.name))
                }
            }
        }
    }

    private fun nuke(ent: Path) {
        if (Files.isDirectory(ent, LinkOption.NOFOLLOW_LINKS)) {
            Files.list(ent).use { stream ->
                val iter = stream.iterator()
                while (iter.hasNext()) this.nuke(iter.next())
            }
        }
        Files.delete(ent)
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