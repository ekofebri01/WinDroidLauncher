package com.example.data

import android.content.Context
import com.example.BuildConfig
import com.example.model.GitHubUpdateState
import com.example.model.GitHubUpdateStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class GitHubUpdateRepository {

    suspend fun checkLatestUpdate(rawRepoInput: String): GitHubUpdateState = withContext(Dispatchers.IO) {
        val currentVersion = BuildConfig.VERSION_NAME
        val slug = normalizeGitHubRepoSlug(rawRepoInput)
        if (slug == null) {
            return@withContext GitHubUpdateState(
                status = GitHubUpdateStatus.NOT_CONFIGURED,
                currentVersion = currentVersion,
                errorMessage = "Masukkan repositori GitHub dengan format owner/repo"
            )
        }

        val releaseResult = runCatching { fetchLatestRelease(slug, currentVersion) }
        val releaseState = releaseResult.getOrNull()
        if (releaseState != null) {
            return@withContext releaseState
        }

        val err = releaseResult.exceptionOrNull()
        val msg = err?.message ?: "Gagal menghubungi GitHub"
        GitHubUpdateState(
            status = GitHubUpdateStatus.ERROR,
            currentVersion = currentVersion,
            errorMessage = msg,
            lastCheckedMillis = System.currentTimeMillis()
        )
    }

    suspend fun downloadApk(
        context: Context,
        apkUrl: String,
        versionTag: String,
        onProgress: (downloadedBytes: Long, totalBytes: Long) -> Unit
    ): File = withContext(Dispatchers.IO) {
        val updatesDir = File(context.cacheDir, "updates").apply { mkdirs() }
        // Clean stale update files before downloading the new release
        updatesDir.listFiles()?.forEach { oldFile ->
            runCatching { oldFile.delete() }
        }

        val safeTag = versionTag.replace(Regex("[^A-Za-z0-9._-]"), "_")
        val tempFile = File(updatesDir, "win11_update_${safeTag}.apk.part")
        val targetFile = File(updatesDir, "win11_update_${safeTag}.apk")

        var currentUrl = apkUrl
        var connection: HttpURLConnection? = null
        var redirects = 0

        try {
            while (true) {
                connection = (URL(currentUrl).openConnection() as HttpURLConnection).apply {
                    instanceFollowRedirects = false
                    requestMethod = "GET"
                    connectTimeout = 15_000
                    readTimeout = 30_000
                    setRequestProperty("User-Agent", "Win11Launcher-Android/${BuildConfig.VERSION_NAME}")
                    setRequestProperty("Accept", "application/octet-stream")
                }
                val code = connection.responseCode
                if (code in 300..399) {
                    val loc = connection.getHeaderField("Location")
                        ?: throw IllegalStateException("Redirect URL kosong saat mengunduh APK")
                    connection.disconnect()
                    redirects++
                    if (redirects > 6) throw IllegalStateException("Terlalu banyak redirect saat mengunduh APK")
                    currentUrl = URL(URL(currentUrl), loc).toString()
                } else if (code in 200..299) {
                    break
                } else {
                    throw IllegalStateException("Gagal mengunduh APK (HTTP $code)")
                }
            }

            val conn = connection ?: throw IllegalStateException("Koneksi gagal")
            val totalBytes = conn.contentLengthLong.coerceAtLeast(0L)
            var downloaded = 0L
            var lastEmit = 0L

            conn.inputStream.use { input ->
                FileOutputStream(tempFile).use { output ->
                    val buffer = ByteArray(16 * 1024)
                    while (true) {
                        val read = input.read(buffer)
                        if (read <= 0) break
                        output.write(buffer, 0, read)
                        downloaded += read
                        val now = System.currentTimeMillis()
                        if (now - lastEmit >= 75L || downloaded == totalBytes) {
                            lastEmit = now
                            onProgress(downloaded, totalBytes)
                        }
                    }
                    output.flush()
                }
            }
            onProgress(downloaded, totalBytes.coerceAtLeast(downloaded))

            // Verify APK is a valid ZIP archive (PK\x03\x04)
            val header = ByteArray(4)
            val headerRead = tempFile.inputStream().use { it.read(header) }
            val isZip = headerRead == 4 &&
                header[0] == 0x50.toByte() &&
                header[1] == 0x4B.toByte() &&
                header[2] == 0x03.toByte() &&
                header[3] == 0x04.toByte()
            if (!isZip || tempFile.length() < 1024L) {
                tempFile.delete()
                throw IllegalStateException("File APK yang diunduh tidak valid atau rusak")
            }

            if (targetFile.exists()) targetFile.delete()
            if (!tempFile.renameTo(targetFile)) {
                tempFile.copyTo(targetFile, overwrite = true)
                tempFile.delete()
            }
            targetFile
        } finally {
            connection?.disconnect()
            if (tempFile.exists()) tempFile.delete()
        }
    }

    fun cleanupUpdateFiles(context: Context, specificPath: String? = null): Boolean {
        var deletedAny = false
        if (!specificPath.isNullOrBlank()) {
            val f = File(specificPath)
            if (f.exists() && f.delete()) {
                deletedAny = true
            }
        }
        val updatesDir = File(context.cacheDir, "updates")
        if (updatesDir.exists()) {
            updatesDir.listFiles()?.forEach { file ->
                if (file.delete()) deletedAny = true
            }
        }
        return deletedAny
    }

    private fun fetchLatestRelease(slug: String, currentVersion: String): GitHubUpdateState {
        val releaseEndpoint = "https://api.github.com/repos/$slug/releases/latest"
        val releaseConn = openGitHubConnection(releaseEndpoint)
        try {
            val code = releaseConn.responseCode
            if (code in 200..299) {
                val body = releaseConn.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(body)
                val tag = json.optString("tag_name").trim()
                if (tag.isBlank()) {
                    throw IllegalStateException("Tag rilis GitHub tidak ditemukan")
                }
                val title = json.optString("name").ifBlank { tag }
                val notes = json.optString("body").trim().takeIf { it.isNotBlank() }
                val htmlUrl = json.optString("html_url").ifBlank { "https://github.com/$slug/releases" }
                val publishedAt = json.optString("published_at").takeIf { it.isNotBlank() }
                val (apkUrl, apkSize) = extractApkAsset(json.optJSONArray("assets"))

                val hasUpdate = compareSemanticVersions(currentVersion, tag) > 0
                return GitHubUpdateState(
                    status = if (hasUpdate) GitHubUpdateStatus.UPDATE_AVAILABLE else GitHubUpdateStatus.UP_TO_DATE,
                    currentVersion = currentVersion,
                    latestTag = tag,
                    releaseTitle = title,
                    releaseNotes = notes,
                    releaseUrl = htmlUrl,
                    apkAssetUrl = apkUrl,
                    apkSizeBytes = apkSize,
                    publishedAt = publishedAt,
                    lastCheckedMillis = System.currentTimeMillis()
                )
            } else if (code == 404) {
                return fetchLatestTagFallback(slug, currentVersion)
            } else if (code == 403 || code == 429) {
                throw IllegalStateException("Batas permintaan API GitHub tercapai (HTTP $code). Coba lagi nanti.")
            } else {
                throw IllegalStateException("GitHub API error (HTTP $code)")
            }
        } finally {
            releaseConn.disconnect()
        }
    }

    private fun fetchLatestTagFallback(slug: String, currentVersion: String): GitHubUpdateState {
        val tagsEndpoint = "https://api.github.com/repos/$slug/tags?per_page=1"
        val tagsConn = openGitHubConnection(tagsEndpoint)
        try {
            val code = tagsConn.responseCode
            if (code !in 200..299) {
                if (code == 404) {
                    throw IllegalStateException("Repositori '$slug' tidak ditemukan di GitHub")
                }
                throw IllegalStateException("GitHub Tags API error (HTTP $code)")
            }
            val body = tagsConn.inputStream.bufferedReader().use { it.readText() }
            val arr = JSONArray(body)
            if (arr.length() == 0) {
                return GitHubUpdateState(
                    status = GitHubUpdateStatus.UP_TO_DATE,
                    currentVersion = currentVersion,
                    latestTag = currentVersion,
                    releaseTitle = "Belum ada rilis atau tag di $slug",
                    releaseUrl = "https://github.com/$slug",
                    lastCheckedMillis = System.currentTimeMillis()
                )
            }
            val first = arr.getJSONObject(0)
            val tag = first.optString("name").trim()
            val hasUpdate = compareSemanticVersions(currentVersion, tag) > 0
            return GitHubUpdateState(
                status = if (hasUpdate) GitHubUpdateStatus.UPDATE_AVAILABLE else GitHubUpdateStatus.UP_TO_DATE,
                currentVersion = currentVersion,
                latestTag = tag,
                releaseTitle = "Tag $tag",
                releaseUrl = "https://github.com/$slug/releases/tag/$tag",
                lastCheckedMillis = System.currentTimeMillis()
            )
        } finally {
            tagsConn.disconnect()
        }
    }

    private fun openGitHubConnection(url: String): HttpURLConnection {
        return (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 8000
            readTimeout = 8000
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
            setRequestProperty("User-Agent", "Win11Launcher-Android/${BuildConfig.VERSION_NAME}")
        }
    }

    private fun extractApkAsset(assets: JSONArray?): Pair<String?, Long> {
        if (assets == null) return null to 0L
        for (i in 0 until assets.length()) {
            val obj = assets.optJSONObject(i) ?: continue
            val name = obj.optString("name")
            val dl = obj.optString("browser_download_url")
            val size = obj.optLong("size", 0L)
            if (name.endsWith(".apk", ignoreCase = true) && dl.startsWith("https://")) {
                return dl to size
            }
        }
        return null to 0L
    }

    companion object {
        fun normalizeGitHubRepoSlug(rawInput: String): String? {
            val trimmed = rawInput.trim()
                .removeSuffix("/")
                .removeSuffix(".git")
            if (trimmed.isBlank()) return null

            val pathPart = when {
                trimmed.startsWith("https://github.com/", ignoreCase = true) ->
                    trimmed.substring("https://github.com/".length)
                trimmed.startsWith("http://github.com/", ignoreCase = true) ->
                    trimmed.substring("http://github.com/".length)
                trimmed.startsWith("github.com/", ignoreCase = true) ->
                    trimmed.substring("github.com/".length)
                else -> trimmed
            }

            val segments = pathPart.split('/').filter { it.isNotBlank() }
            if (segments.size < 2) return null
            val owner = segments[0]
            val repo = segments[1]
            val validPattern = Regex("^[A-Za-z0-9._-]+$")
            if (!validPattern.matches(owner) || !validPattern.matches(repo)) return null
            return "$owner/$repo"
        }

        fun compareSemanticVersions(currentVersion: String, latestVersion: String): Int {
            fun parseNumbers(raw: String): List<Int> {
                val clean = raw.trim()
                    .removePrefix("v")
                    .removePrefix("V")
                    .substringBefore('-')
                    .substringBefore('+')
                return clean.split('.')
                    .mapNotNull { seg -> seg.filter { it.isDigit() }.toIntOrNull() }
                    .ifEmpty { listOf(0) }
            }

            val curParts = parseNumbers(currentVersion)
            val latParts = parseNumbers(latestVersion)
            val maxLen = maxOf(curParts.size, latParts.size)
            for (i in 0 until maxLen) {
                val c = curParts.getOrElse(i) { 0 }
                val l = latParts.getOrElse(i) { 0 }
                if (l != c) return l.compareTo(c)
            }
            return 0
        }
    }
}
