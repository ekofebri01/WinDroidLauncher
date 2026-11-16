package com.example

import com.example.data.GitHubUpdateRepository
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun githubRepoSlugNormalization_handlesFormatsSafely() {
        assertEquals(
            "openai/win11-launcher",
            GitHubUpdateRepository.normalizeGitHubRepoSlug("openai/win11-launcher")
        )
        assertEquals(
            "openai/win11-launcher",
            GitHubUpdateRepository.normalizeGitHubRepoSlug("https://github.com/openai/win11-launcher.git")
        )
        assertEquals(
            "openai/win11-launcher",
            GitHubUpdateRepository.normalizeGitHubRepoSlug("github.com/openai/win11-launcher/")
        )
        assertNull(GitHubUpdateRepository.normalizeGitHubRepoSlug("invalid-repo"))
        assertNull(GitHubUpdateRepository.normalizeGitHubRepoSlug("   "))
    }

    @Test
    fun semanticVersionComparison_detectsNewerReleases() {
        assertTrue(GitHubUpdateRepository.compareSemanticVersions("1.0", "v1.1.0") > 0)
        assertTrue(GitHubUpdateRepository.compareSemanticVersions("1.2.3", "v2.0") > 0)
        assertEquals(0, GitHubUpdateRepository.compareSemanticVersions("1.0.0", "v1.0"))
        assertTrue(GitHubUpdateRepository.compareSemanticVersions("2.0.0", "v1.9.9") < 0)
    }
}

