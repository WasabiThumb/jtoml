package gh

import com.google.gson.JsonArray
import org.gradle.kotlin.dsl.provideDelegate
import java.util.stream.IntStream

class GitHubTree(
    override val name: String,
    val owner: String,
    val repo: String,
    val sha: String
) : GitHubEntity, AbstractCollection<GitHubEntity>() {

    private val data: JsonArray by lazy {
        GitHubAPI.get("/repos/${this.owner}/${this.repo}/git/trees/${this.sha}")
            .asJsonObject
            .get("tree")
            .asJsonArray
    }

    override val isTree: Boolean
        get() = true

    override val asTree: GitHubTree
        get() = this

    override val isBlob: Boolean
        get() = false

    override val asBlob: GitHubBlob
        get() = throw UnsupportedOperationException()

    //

    override val size: Int
        get() = this.data.size()

    operator fun get(index: Int): GitHubEntity {
        return GitHubEntity.of(this.owner, this.repo, this.data.get(index).asJsonObject)
    }

    operator fun get(name: String): GitHubEntity? {
        for (i in 0 until this.size) {
            val ent = this[i]
            if (ent.name == name) return ent
        }
        return null
    }

    fun sub(name: String): GitHubTree {
        val ent = this[name] ?: throw IllegalArgumentException("Entity \"${name}\" does not exist in tree")
        if (!ent.isTree) throw IllegalArgumentException("Entity \"${name}\" is not a tree")
        return ent.asTree
    }

    override fun iterator(): Iterator<GitHubEntity> {
        return IntStream.range(0, this.size)
            .mapToObj(this::get)
            .iterator()
    }

}