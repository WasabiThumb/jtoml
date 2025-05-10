package gh

import java.io.InputStream

class GitHubBlob(
    override val name: String,
    val owner: String,
    val repo: String,
    val sha: String,
    val size: Long
) : GitHubEntity {

    val stream: InputStream
        get() = GitHubAPI.getStream("/repos/${this.owner}/${this.repo}/git/blobs/${this.sha}", true)

    override val isTree: Boolean
        get() = false

    override val asTree: GitHubTree
        get() = throw UnsupportedOperationException()

    override val isBlob: Boolean
        get() = true

    override val asBlob: GitHubBlob
        get() = this

}