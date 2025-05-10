package gh

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.stream.JsonReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import kotlin.Throws

object GitHubAPI {

    private val gson: Gson = Gson()
    var accessToken: String? = null

    //

    @Throws(IOException::class)
    fun getStream(path: String, raw: Boolean = false): InputStream {
        val url = URI.create("https://api.github.com${path}").toURL()
        val c = url.openConnection() as HttpURLConnection

        c.setRequestProperty("User-Agent", "jtoml; wasabithumbs@gmail.com")
        c.setRequestProperty(
            "Accept",
            if (raw) "application/vnd.github.raw+json" else "application/vnd.github+json"
        )
        c.setRequestProperty("X-GitHub-Api-Version", "2022-11-28")

        if (this.accessToken != null) {
            c.setRequestProperty("Authorization", "Bearer ${this.accessToken}")
        }

        if (c.responseCode == 403 || c.responseCode == 429) {
            throw Error("API rate limit exceeded. Please add an access token and/or try again in 1 hour.")
        }

        return c.inputStream
    }

    @Throws(IOException::class)
    fun get(path: String): JsonElement {
        this.getStream(path, false).use { stream ->
            JsonReader(InputStreamReader(stream, Charsets.UTF_8)).use { reader ->
                return gson.fromJson(reader, JsonElement::class.java)
            }
        }
    }

    @Throws(IOException::class)
    fun getLatestCommit(owner: String, repo: String): String {
        return get("/repos/${owner}/${repo}/commits")
            .asJsonArray
            .get(0)
            .asJsonObject
            .get("sha")
            .asJsonPrimitive
            .asString
    }

    @Throws(IOException::class)
    fun getTree(owner: String, repo: String, commitSha: String): GitHubTree {
        return GitHubTree(repo, owner, repo, commitSha)
    }

}