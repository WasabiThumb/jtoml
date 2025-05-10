package tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

import gh.GitHubAPI
import gh.GitHubTree
import org.gradle.api.logging.LogLevel
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

abstract class FetchTestsTask : DefaultTask() {

    @get:OutputDirectory
    abstract val outDir: DirectoryProperty

    @get:Input
    @get:Optional
    abstract val accessToken: Property<String>

    //

    @TaskAction
    fun execute() {
        val commit = GitHubAPI.getLatestCommit("toml-lang", "toml-test")
        if (this.checkCommit(commit)) {
            this.logger.log(LogLevel.LIFECYCLE, "Up to date, skipping")
            return
        } else {
            this.logger.log(LogLevel.LIFECYCLE, "Fetching tests")
        }

        val tree = GitHubAPI.getTree("toml-lang", "toml-test", commit)
            .sub("tests")

        val dir = this.outDir.get()
            .asFile
            .toPath()

        this.writeTree(tree, dir)
        this.writeCommit(commit)
    }

    private fun writeTree(tree: GitHubTree, dir: Path) {
        val projectDir = this.project.layout.projectDirectory.asFile.toPath()
        this.logger.info("> ${projectDir.relativize(dir)}")

        if (!Files.exists(dir))
            Files.createDirectories(dir)

        for (node in tree) {
            val target = dir.resolve(node.name)

            if (node.isTree) {
                this.writeTree(node.asTree, target)
            } else if (node.isBlob) {
                node.asBlob.stream.use { inStream ->
                    Files.newOutputStream(
                        target,
                        StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
                    ).use { outStream ->
                        val buf = ByteArray(8192)
                        var read: Int

                        while (true) {
                            read = inStream.read(buf)
                            if (read == -1) break
                            outStream.write(buf, 0, read)
                        }
                        outStream.flush()
                    }
                }
            }
        }
    }

    private fun checkCommit(commit: String): Boolean {
        val f = this.tempDir.resolve("commit")
        if (!Files.exists(f)) return false
        Files.newInputStream(f, StandardOpenOption.READ).use { stream ->
            BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { reader ->
                return commit == reader.readLine()
            }
        }
    }

    private fun writeCommit(commit: String) {
        val f = this.tempDir.resolve("commit")
        Files.newOutputStream(
            f,
            StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
        ).use { stream ->
            OutputStreamWriter(stream, Charsets.UTF_8).use { writer ->
                writer.write("${commit}\n")
                writer.flush()
            }
        }
    }

    private val tempDir: Path
        get() {
            val dir = this.project.layout.buildDirectory.get()
                .asFile
                .toPath()
                .resolve("tmp")
                .resolve(this.name)

            if (!Files.exists(dir))
                Files.createDirectories(dir)

            return dir
        }

}