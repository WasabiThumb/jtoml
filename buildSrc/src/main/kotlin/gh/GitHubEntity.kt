package gh

import com.google.gson.JsonObject

interface GitHubEntity {

    val name: String

    val isTree: Boolean

    val asTree: GitHubTree

    val isBlob: Boolean

    val asBlob: GitHubBlob

    //

    companion object {

        fun of(owner: String, repo: String, data: JsonObject): GitHubEntity {
            val type = data.get("type").asString
            val sha = data.get("sha").asString
            val name = data.get("path").asString

            if ("tree" == type) {
                return GitHubTree(name, owner, repo, sha)
            } else if ("blob" == type) {
                return GitHubBlob(name, owner, repo, sha, data.get("size").asLong)
            } else {
                throw IllegalStateException("Unrecognized entity type (${type})")
            }
        }

    }

}