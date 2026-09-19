package tasks

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskAction
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.name

/** Used by the :all project to combine resources from peer tasks */
@CacheableTask
abstract class PeerResourcesTask : DefaultTask() {

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceDirs: Property<FileCollection>

    @get:OutputDirectory
    abstract val outDir: DirectoryProperty

    init {
        this.outDir.convention(project.layout.buildDirectory.dir(this.name))
    }

    //

    fun fromPeerProjects(peers: Iterable<Project>) {
        val fc = this.project.files()
        peers.forEach { peer ->
            val mainSourceSet = peer.extensions
                .findByType(SourceSetContainer::class.java)
                ?.findByName("main")
                ?: return@forEach

            val dirs = mainSourceSet.resources.srcDirs
            if (dirs.isEmpty()) return@forEach

            fc.from(dirs)
        }
        this.sourceDirs.set(fc)
    }

    @TaskAction
    fun execute() {
        val dirs = this.sourceDirs.get().files
        val out = this.outDir.get().asFile.toPath()
        this.logger.lifecycle("Collating resources from ${dirs.size} directories")
        dirs.forEach { dir ->
            val src = dir.toPath()
            if (!Files.isDirectory(src)) {
                this.logger.warn("- Skipping ${dir.absolutePath} (not a directory)")
                return@forEach
            }
            blitDir(src, out)
        }
    }

    //

    companion object {

        private fun blitDir(src: Path, target: Path) {
            if (!Files.isDirectory(src)) return
            if (!Files.exists(target)) Files.createDirectories(target)
            Files.list(src).forEach { file ->
                if (Files.isDirectory(file)) {
                    blitDir(file, target.resolve(file.name))
                } else {
                    blitFile(file, target.resolve(file.name))
                }
            }
        }

        private fun blitFile(src: Path, target: Path) {
            if (!Files.exists(target)) {
                Files.copy(src, target)
                return
            }

            // Treat as service files
            val set = mutableSetOf<String>()
            val temp = target.parent.resolve(target.name + ".tmp")
            var ok = false
            try {
                Files.newBufferedWriter(temp, Charsets.UTF_8).use { w ->
                    Files.newBufferedReader(src, Charsets.UTF_8).useLines { s ->
                        s.forEach {
                            if (it.isEmpty() || !set.add(it)) return@forEach
                            w.write(it)
                            w.write('\n'.code)
                        }
                    }
                    Files.newBufferedReader(target, Charsets.UTF_8).useLines { s ->
                        s.forEach {
                            if (it.isEmpty() || !set.add(it)) return@forEach
                            w.write(it)
                            w.write('\n'.code)
                        }
                    }
                    w.flush()
                }

                Files.delete(target)
                Files.move(temp, target)
                ok = true
            } finally {
                if (!ok) Files.delete(temp)
            }
        }

    }

}