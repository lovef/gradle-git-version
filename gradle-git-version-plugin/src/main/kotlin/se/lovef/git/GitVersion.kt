package se.lovef.git

class GitVersion(private val git: Git, val baseVersion: String) {

    val version: String
        get() = tag?.substring(1) ?: "$baseVersion-SNAPSHOT"

    val tag: String?
        get() = tags.let { if (it.size == 1) it.first() else null }

    fun createTag(): String {
        assertNoReleaseTags()

        val prefix = "v$baseVersion."
        val newPatch = newPatch(prefix)
        val newTag = "$prefix$newPatch"

        git.tag(newTag)

        return newTag
    }

    private fun assertNoReleaseTags() {
        val tags = tags
        if (tags.isNotEmpty()) {
            throw AlreadyTaggedException(tags)
        }
    }

    private fun newPatch(prefix: String): Int {
        return git.matchingTags(prefix)
            .map { it.substring(prefix.length).toInt() }
            .max()
            ?.let { it + 1 } ?: 0
    }

    private val tags: List<String>
        get() = git.currentTags
            .filter { it.startsWith("v$baseVersion.") }

}

abstract class GitVersionException(message: String) : RuntimeException(message)

class AlreadyTaggedException(
    val tags: List<String>
) : GitVersionException("Already tagged with $tags")
