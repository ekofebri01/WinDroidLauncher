package com.example.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.ExplorerItem
import com.example.model.ExplorerLocation
import com.example.model.ExplorerSortBy
import com.example.model.FileItemKind
import com.example.model.MountedSafFolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File

private val Context.explorerDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "win11_explorer_prefs"
)

class FileExplorerRepository(private val context: Context) {

    private val driveCRoot: File = File(context.filesDir, "win11_drive_c")
    private val recycleBinDir: File = File(driveCRoot, ".RecycleBin")
    private val trashMetaFile: File = File(driveCRoot, ".recycle_meta.txt")

    private object Keys {
        val MOUNTED_SAF_FOLDERS = stringPreferencesKey("mounted_saf_folders")
    }

    init {
        ensureWorkspaceInitialized()
    }

    private fun ensureWorkspaceInitialized() {
        val defaultFolders = listOf(
            "Desktop",
            "Documents",
            "Downloads",
            "Pictures",
            "Music",
            ".RecycleBin"
        )
        defaultFolders.forEach { rel ->
            File(driveCRoot, rel).mkdirs()
        }
        val welcomeFile = File(driveCRoot, "Documents/Welcome to Win11.txt")
        if (!welcomeFile.exists()) {
            runCatching {
                welcomeFile.writeText(
                    "Windows 11 File Explorer for Android\n" +
                        "====================================\n\n" +
                        "Tips:\n" +
                        "- Use '+ New' to create folders, text notes, or import files.\n" +
                        "- Use 'Mount Device Folder' to browse real Android storage folders (Downloads, DCIM, Documents) safely via Storage Access Framework.\n" +
                        "- Long-press any file or folder for Copy, Cut, Rename, and Delete actions.\n"
                )
            }
        }
        val notesFile = File(driveCRoot, "Documents/Quick Notes.md")
        if (!notesFile.exists()) {
            runCatching {
                notesFile.writeText(
                    "# Daily Notes\n\n- Customize desktop wallpaper in Personalize\n- Pin favorite apps to Taskbar\n"
                )
            }
        }
    }

    val mountedFoldersFlow: Flow<List<MountedSafFolder>> =
        context.explorerDataStore.data.map { prefs ->
            val raw = prefs[Keys.MOUNTED_SAF_FOLDERS].orEmpty()
            if (raw.isBlank()) emptyList()
            else raw.split("\n").mapNotNull { line ->
                val parts = line.split("|", limit = 2)
                if (parts.size == 2 && parts[0].isNotBlank()) {
                    MountedSafFolder(treeUriString = parts[0], displayName = parts[1])
                } else null
            }
        }

    suspend fun addMountedSafFolder(treeUri: Uri) {
        runCatching {
            context.contentResolver.takePersistableUriPermission(
                treeUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        }.onFailure {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    treeUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
        }

        val docId = DocumentsContract.getTreeDocumentId(treeUri)
        val docUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
        val name = queryDocumentName(docUri) ?: docId.substringAfterLast(':').ifBlank { "Device Folder" }

        context.explorerDataStore.edit { prefs ->
            val existing = prefs[Keys.MOUNTED_SAF_FOLDERS].orEmpty()
                .split("\n")
                .filter { it.isNotBlank() && !it.startsWith("${treeUri}|") }
            val updated = existing + "${treeUri}|${name.replace("|", " ")}"
            prefs[Keys.MOUNTED_SAF_FOLDERS] = updated.joinToString("\n")
        }
    }

    suspend fun removeMountedSafFolder(treeUriString: String) {
        context.explorerDataStore.edit { prefs ->
            val existing = prefs[Keys.MOUNTED_SAF_FOLDERS].orEmpty()
                .split("\n")
                .filter { it.isNotBlank() && !it.startsWith("$treeUriString|") }
            prefs[Keys.MOUNTED_SAF_FOLDERS] = existing.joinToString("\n")
        }
    }

    suspend fun listItems(
        location: ExplorerLocation,
        sortBy: ExplorerSortBy,
        ascending: Boolean,
        searchQuery: String
    ): List<ExplorerItem> = withContext(Dispatchers.IO) {
        val rawItems = when (location) {
            ExplorerLocation.Home -> emptyList()
            ExplorerLocation.ThisPc -> emptyList()
            ExplorerLocation.RecycleBin -> listRecycleBinItems()
            is ExplorerLocation.LocalFolder -> listLocalFolder(location.relativePath)
            is ExplorerLocation.SafFolder -> listSafFolder(location.treeUriString, location.documentUriString)
        }

        val filtered = if (searchQuery.isBlank()) {
            rawItems
        } else {
            rawItems.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }

        sortItems(filtered, sortBy, ascending)
    }

    suspend fun listRecentFiles(): List<ExplorerItem> = withContext(Dispatchers.IO) {
        val collected = mutableListOf<ExplorerItem>()
        driveCRoot.walkTopDown()
            .filter { it.isFile && !it.absolutePath.contains(".RecycleBin") && !it.name.startsWith(".") }
            .forEach { file ->
                val rel = file.relativeTo(driveCRoot).path
                collected.add(fileToExplorerItem(file, rel))
            }
        collected.sortedByDescending { it.lastModifiedMillis }.take(12)
    }

    private fun listLocalFolder(relativePath: String): List<ExplorerItem> {
        val targetDir = resolveLocalFile(relativePath) ?: return emptyList()
        if (!targetDir.exists() || !targetDir.isDirectory) return emptyList()
        return targetDir.listFiles()
            ?.filter { !it.name.startsWith(".") }
            ?.map { file ->
                val rel = if (relativePath.isBlank()) file.name else "$relativePath/${file.name}"
                fileToExplorerItem(file, rel)
            }
            ?: emptyList()
    }

    private fun listRecycleBinItems(): List<ExplorerItem> {
        if (!recycleBinDir.exists()) return emptyList()
        val meta = loadTrashMeta()
        return recycleBinDir.listFiles()?.map { file ->
            val rel = ".RecycleBin/${file.name}"
            fileToExplorerItem(file, rel, originalPath = meta[file.name])
        } ?: emptyList()
    }

    private fun listSafFolder(treeUriString: String, documentUriString: String): List<ExplorerItem> {
        val treeUri = Uri.parse(treeUriString)
        val docUri = Uri.parse(documentUriString)
        val docId = runCatching { DocumentsContract.getDocumentId(docUri) }
            .getOrElse { DocumentsContract.getTreeDocumentId(treeUri) }
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, docId)

        val projection = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_SIZE,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED
        )

        val result = mutableListOf<ExplorerItem>()
        runCatching {
            context.contentResolver.query(childrenUri, projection, null, null, null)?.use { cursor ->
                val idIdx = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                val nameIdx = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                val mimeIdx = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE)
                val sizeIdx = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE)
                val modIdx = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)

                while (cursor.moveToNext()) {
                    val childId = if (idIdx >= 0) cursor.getString(idIdx) else continue
                    val name = if (nameIdx >= 0) cursor.getString(nameIdx) ?: "Unnamed" else "Unnamed"
                    if (name.startsWith(".")) continue
                    val mime = if (mimeIdx >= 0) cursor.getString(mimeIdx) ?: "*/*" else "*/*"
                    val size = if (sizeIdx >= 0) cursor.getLong(sizeIdx) else 0L
                    val modified = if (modIdx >= 0) cursor.getLong(modIdx) else 0L
                    val isDir = mime == DocumentsContract.Document.MIME_TYPE_DIR
                    val childDocUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, childId).toString()

                    result.add(
                        ExplorerItem(
                            id = childDocUri,
                            name = name,
                            isDirectory = isDir,
                            sizeBytes = size,
                            lastModifiedMillis = modified,
                            mimeType = mime,
                            kind = if (isDir) FileItemKind.FOLDER else detectKind(name, mime),
                            safDocumentUri = childDocUri,
                            safTreeUri = treeUriString
                        )
                    )
                }
            }
        }
        return result
    }

    suspend fun createFolder(location: ExplorerLocation, folderName: String): Boolean = withContext(Dispatchers.IO) {
        val clean = sanitizeName(folderName) ?: return@withContext false
        when (location) {
            is ExplorerLocation.LocalFolder -> {
                val parent = resolveLocalFile(location.relativePath) ?: return@withContext false
                val target = uniqueFile(File(parent, clean))
                target.mkdirs()
            }
            is ExplorerLocation.SafFolder -> {
                runCatching {
                    DocumentsContract.createDocument(
                        context.contentResolver,
                        Uri.parse(location.documentUriString),
                        DocumentsContract.Document.MIME_TYPE_DIR,
                        clean
                    ) != null
                }.getOrDefault(false)
            }
            else -> false
        }
    }

    suspend fun createTextFile(location: ExplorerLocation, fileName: String, content: String = ""): Boolean =
        withContext(Dispatchers.IO) {
            val clean = sanitizeName(fileName)?.let {
                if (it.contains('.')) it else "$it.txt"
            } ?: return@withContext false
            when (location) {
                is ExplorerLocation.LocalFolder -> {
                    val parent = resolveLocalFile(location.relativePath) ?: return@withContext false
                    val target = uniqueFile(File(parent, clean))
                    runCatching {
                        target.writeText(content)
                        true
                    }.getOrDefault(false)
                }
                is ExplorerLocation.SafFolder -> {
                    runCatching {
                        val created = DocumentsContract.createDocument(
                            context.contentResolver,
                            Uri.parse(location.documentUriString),
                            "text/plain",
                            clean
                        ) ?: return@runCatching false
                        context.contentResolver.openOutputStream(created)?.use { out ->
                            out.write(content.toByteArray())
                        }
                        true
                    }.getOrDefault(false)
                }
                else -> false
            }
        }

    suspend fun importUriToLocalFolder(uri: Uri, targetRelativePath: String): Boolean = withContext(Dispatchers.IO) {
        val parent = resolveLocalFile(targetRelativePath) ?: return@withContext false
        val name = queryDocumentName(uri) ?: "Imported_${System.currentTimeMillis()}"
        val dest = uniqueFile(File(parent, sanitizeName(name) ?: "ImportedFile"))
        runCatching {
            context.contentResolver.openInputStream(uri)?.use { input ->
                dest.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return@runCatching false
            true
        }.getOrDefault(false)
    }

    suspend fun readTextContent(item: ExplorerItem): String? = withContext(Dispatchers.IO) {
        runCatching {
            when {
                item.localRelativePath != null -> {
                    val file = resolveLocalFile(item.localRelativePath) ?: return@runCatching null
                    if (file.length() > 512_000L) {
                        file.bufferedReader().use { it.readText().take(20_000) + "\n\n... (Truncated)" }
                    } else {
                        file.readText()
                    }
                }
                item.safDocumentUri != null -> {
                    context.contentResolver.openInputStream(Uri.parse(item.safDocumentUri))?.use { stream ->
                        stream.bufferedReader().readText().take(50_000)
                    }
                }
                else -> null
            }
        }.getOrNull()
    }

    suspend fun writeTextContent(item: ExplorerItem, content: String): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            when {
                item.localRelativePath != null -> {
                    val file = resolveLocalFile(item.localRelativePath) ?: return@runCatching false
                    file.writeText(content)
                    true
                }
                item.safDocumentUri != null -> {
                    context.contentResolver.openOutputStream(Uri.parse(item.safDocumentUri), "wt")?.use { out ->
                        out.write(content.toByteArray())
                    }
                    true
                }
                else -> false
            }
        }.getOrDefault(false)
    }

    suspend fun renameItem(item: ExplorerItem, newName: String): Boolean = withContext(Dispatchers.IO) {
        val clean = sanitizeName(newName) ?: return@withContext false
        when {
            item.localRelativePath != null -> {
                val src = resolveLocalFile(item.localRelativePath) ?: return@withContext false
                val dst = File(src.parentFile, clean)
                if (dst.exists()) return@withContext false
                src.renameTo(dst)
            }
            item.safDocumentUri != null -> {
                runCatching {
                    DocumentsContract.renameDocument(
                        context.contentResolver,
                        Uri.parse(item.safDocumentUri),
                        clean
                    ) != null
                }.getOrDefault(false)
            }
            else -> false
        }
    }

    suspend fun pasteItems(
        items: List<ExplorerItem>,
        isMove: Boolean,
        destination: ExplorerLocation
    ): Boolean = withContext(Dispatchers.IO) {
        if (destination !is ExplorerLocation.LocalFolder) return@withContext false
        val destDir = resolveLocalFile(destination.relativePath) ?: return@withContext false
        if (!destDir.exists()) destDir.mkdirs()

        var allOk = true
        items.forEach { item ->
            val srcRel = item.localRelativePath
            if (srcRel != null) {
                val src = resolveLocalFile(srcRel)
                if (src != null && src.exists()) {
                    val target = uniqueFile(File(destDir, src.name))
                    val copied = runCatching {
                        if (src.isDirectory) src.copyRecursively(target, overwrite = false)
                        else src.copyTo(target, overwrite = false).exists()
                    }.getOrDefault(false)
                    if (copied && isMove) {
                        src.deleteRecursively()
                    }
                    if (!copied) allOk = false
                }
            } else if (item.safDocumentUri != null && !item.isDirectory) {
                val target = uniqueFile(File(destDir, item.name))
                val copied = runCatching {
                    context.contentResolver.openInputStream(Uri.parse(item.safDocumentUri))?.use { input ->
                        target.outputStream().use { output -> input.copyTo(output) }
                    }
                    true
                }.getOrDefault(false)
                if (!copied) allOk = false
            }
        }
        allOk
    }

    suspend fun deleteItems(items: List<ExplorerItem>, fromRecycleBin: Boolean): Boolean =
        withContext(Dispatchers.IO) {
            var allOk = true
            val meta = loadTrashMeta().toMutableMap()
            items.forEach { item ->
                when {
                    item.localRelativePath != null -> {
                        val file = resolveLocalFile(item.localRelativePath)
                        if (file != null && file.exists()) {
                            if (fromRecycleBin) {
                                meta.remove(file.name)
                                if (!file.deleteRecursively()) allOk = false
                            } else {
                                recycleBinDir.mkdirs()
                                val trashed = uniqueFile(File(recycleBinDir, file.name))
                                val moved = file.renameTo(trashed) || run {
                                    val ok = file.copyRecursively(trashed, overwrite = false)
                                    if (ok) file.deleteRecursively()
                                    ok
                                }
                                if (moved) {
                                    meta[trashed.name] = item.localRelativePath
                                } else {
                                    allOk = false
                                }
                            }
                        }
                    }
                    item.safDocumentUri != null -> {
                        val deleted = runCatching {
                            DocumentsContract.deleteDocument(
                                context.contentResolver,
                                Uri.parse(item.safDocumentUri)
                            )
                        }.getOrDefault(false)
                        if (!deleted) allOk = false
                    }
                }
            }
            saveTrashMeta(meta)
            allOk
        }

    suspend fun restoreFromRecycleBin(items: List<ExplorerItem>): Boolean = withContext(Dispatchers.IO) {
        val meta = loadTrashMeta().toMutableMap()
        var allOk = true
        items.forEach { item ->
            val rel = item.localRelativePath ?: return@forEach
            val file = resolveLocalFile(rel) ?: return@forEach
            val origRel = meta[file.name] ?: "Documents/${file.name}"
            val origFile = resolveLocalFile(origRel) ?: File(driveCRoot, "Documents/${file.name}")
            origFile.parentFile?.mkdirs()
            val target = uniqueFile(origFile)
            val moved = file.renameTo(target) || run {
                val ok = file.copyRecursively(target, overwrite = false)
                if (ok) file.deleteRecursively()
                ok
            }
            if (moved) meta.remove(file.name) else allOk = false
        }
        saveTrashMeta(meta)
        allOk
    }

    suspend fun emptyRecycleBin(): Boolean = withContext(Dispatchers.IO) {
        val ok = recycleBinDir.listFiles()?.all { it.deleteRecursively() } ?: true
        saveTrashMeta(emptyMap())
        ok
    }

    fun getShareOrViewUri(item: ExplorerItem): Uri? {
        return when {
            item.safDocumentUri != null -> Uri.parse(item.safDocumentUri)
            item.localRelativePath != null -> {
                val file = resolveLocalFile(item.localRelativePath) ?: return null
                runCatching {
                    FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                }.getOrNull()
            }
            else -> null
        }
    }

    fun openExternally(item: ExplorerItem): Boolean {
        val uri = getShareOrViewUri(item) ?: return false
        return runCatching {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, item.mimeType.ifBlank { "*/*" })
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }

    fun shareItem(item: ExplorerItem): Boolean {
        val uri = getShareOrViewUri(item) ?: return false
        return runCatching {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = item.mimeType.ifBlank { "*/*" }
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(intent, "Share ${item.name}").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            true
        }.getOrDefault(false)
    }

    private fun resolveLocalFile(relativePath: String): File? {
        val clean = relativePath.trimStart('/')
        val target = if (clean.isBlank()) driveCRoot else File(driveCRoot, clean)
        val rootCanonical = driveCRoot.canonicalPath
        val targetCanonical = target.canonicalPath
        return if (targetCanonical == rootCanonical || targetCanonical.startsWith("$rootCanonical${File.separator}")) {
            target
        } else null
    }

    private fun fileToExplorerItem(
        file: File,
        relativePath: String,
        originalPath: String? = null
    ): ExplorerItem {
        val isDir = file.isDirectory
        val mime = if (isDir) {
            DocumentsContract.Document.MIME_TYPE_DIR
        } else {
            guessMimeType(file.name)
        }
        return ExplorerItem(
            id = "local_$relativePath",
            name = file.name,
            isDirectory = isDir,
            sizeBytes = if (isDir) (file.listFiles()?.count { !it.name.startsWith(".") }?.toLong() ?: 0L) else file.length(),
            lastModifiedMillis = file.lastModified(),
            mimeType = mime,
            kind = if (isDir) FileItemKind.FOLDER else detectKind(file.name, mime),
            localRelativePath = relativePath,
            originalPathBeforeTrash = originalPath
        )
    }

    private fun sortItems(
        items: List<ExplorerItem>,
        sortBy: ExplorerSortBy,
        ascending: Boolean
    ): List<ExplorerItem> {
        val comparator = when (sortBy) {
            ExplorerSortBy.NAME -> compareBy<ExplorerItem> { !it.isDirectory }.thenBy { it.name.lowercase() }
            ExplorerSortBy.DATE_MODIFIED -> compareBy<ExplorerItem> { !it.isDirectory }.thenBy { it.lastModifiedMillis }
            ExplorerSortBy.SIZE -> compareBy<ExplorerItem> { !it.isDirectory }.thenBy { it.sizeBytes }
            ExplorerSortBy.TYPE -> compareBy<ExplorerItem> { !it.isDirectory }.thenBy { it.kind.name }.thenBy { it.name.lowercase() }
        }
        val sorted = items.sortedWith(comparator)
        return if (ascending) sorted else {
            val dirs = sorted.filter { it.isDirectory }
            val files = sorted.filterNot { it.isDirectory }
            dirs.reversed() + files.reversed()
        }
    }

    private fun queryDocumentName(uri: Uri): String? {
        return runCatching {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (idx >= 0) cursor.getString(idx) else null
                } else null
            }
        }.getOrNull()
    }

    private fun loadTrashMeta(): Map<String, String> {
        if (!trashMetaFile.exists()) return emptyMap()
        return runCatching {
            trashMetaFile.readLines()
                .mapNotNull { line ->
                    val parts = line.split("=", limit = 2)
                    if (parts.size == 2) parts[0] to parts[1] else null
                }
                .toMap()
        }.getOrDefault(emptyMap())
    }

    private fun saveTrashMeta(meta: Map<String, String>) {
        runCatching {
            trashMetaFile.writeText(meta.entries.joinToString("\n") { "${it.key}=${it.value}" })
        }
    }

    private fun uniqueFile(desired: File): File {
        if (!desired.exists()) return desired
        val base = desired.nameWithoutExtension
        val ext = desired.extension
        var idx = 1
        while (true) {
            val candidateName = if (ext.isBlank()) "$base ($idx)" else "$base ($idx).$ext"
            val candidate = File(desired.parentFile, candidateName)
            if (!candidate.exists()) return candidate
            idx++
        }
    }

    private fun sanitizeName(raw: String): String? {
        val cleaned = raw.trim().replace("/", "").replace("\\", "")
        return cleaned.takeIf { it.isNotBlank() && it != "." && it != ".." }
    }

    private fun guessMimeType(name: String): String {
        val ext = name.substringAfterLast('.', "").lowercase()
        return when (ext) {
            "md", "txt", "log", "json", "xml", "kt", "java", "py", "csv" -> "text/plain"
            "pdf" -> "application/pdf"
            "zip", "rar", "7z", "tar", "gz" -> "application/zip"
            else -> MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "application/octet-stream"
        }
    }

    private fun detectKind(name: String, mime: String): FileItemKind {
        val ext = name.substringAfterLast('.', "").lowercase()
        return when {
            mime.startsWith("image/") || ext in setOf("png", "jpg", "jpeg", "webp", "gif", "bmp") -> FileItemKind.IMAGE
            mime.startsWith("video/") || ext in setOf("mp4", "mkv", "mov", "avi", "webm") -> FileItemKind.VIDEO
            mime.startsWith("audio/") || ext in setOf("mp3", "wav", "ogg", "flac", "m4a") -> FileItemKind.AUDIO
            mime == "application/pdf" || ext == "pdf" -> FileItemKind.PDF
            ext in setOf("zip", "rar", "7z", "tar", "gz", "apk") -> FileItemKind.ARCHIVE
            mime.startsWith("text/") || ext in setOf("txt", "md", "json", "xml", "kt", "java", "py", "csv", "log") -> FileItemKind.TEXT
            else -> FileItemKind.OTHER
        }
    }
}
