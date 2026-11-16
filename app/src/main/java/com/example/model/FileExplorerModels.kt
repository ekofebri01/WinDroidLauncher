package com.example.model

import androidx.compose.runtime.Immutable

enum class ExplorerViewMode(val label: String) {
    DETAILS("Details"),
    LARGE_ICONS("Large icons"),
    TILES("Tiles")
}

enum class ExplorerSortBy(val label: String) {
    NAME("Name"),
    DATE_MODIFIED("Date modified"),
    SIZE("Size"),
    TYPE("Type")
}

sealed class ExplorerLocation {
    abstract val title: String

    data object Home : ExplorerLocation() {
        override val title: String = "Home"
    }

    data object ThisPc : ExplorerLocation() {
        override val title: String = "This PC"
    }

    data object RecycleBin : ExplorerLocation() {
        override val title: String = "Recycle Bin"
    }

    data class LocalFolder(
        val relativePath: String,
        override val title: String
    ) : ExplorerLocation()

    data class SafFolder(
        val treeUriString: String,
        val documentUriString: String,
        override val title: String,
        val breadcrumbs: List<SafBreadcrumb> = emptyList()
    ) : ExplorerLocation()
}

@Immutable
data class SafBreadcrumb(
    val documentUriString: String,
    val title: String
)

@Immutable
data class MountedSafFolder(
    val treeUriString: String,
    val displayName: String
)

enum class FileItemKind {
    FOLDER,
    TEXT,
    IMAGE,
    AUDIO,
    VIDEO,
    PDF,
    ARCHIVE,
    OTHER
}

@Immutable
data class ExplorerItem(
    val id: String,
    val name: String,
    val isDirectory: Boolean,
    val sizeBytes: Long,
    val lastModifiedMillis: Long,
    val mimeType: String,
    val kind: FileItemKind,
    val localRelativePath: String? = null,
    val safDocumentUri: String? = null,
    val safTreeUri: String? = null,
    val originalPathBeforeTrash: String? = null
)

@Immutable
data class ExplorerTab(
    val id: String,
    val location: ExplorerLocation,
    val backStack: List<ExplorerLocation> = emptyList(),
    val forwardStack: List<ExplorerLocation> = emptyList()
)

enum class ClipboardMode {
    COPY,
    CUT
}

@Immutable
data class ExplorerClipboard(
    val items: List<ExplorerItem>,
    val mode: ClipboardMode
)

@Immutable
data class ExplorerPreviewState(
    val item: ExplorerItem,
    val textContent: String? = null,
    val imageUriString: String? = null,
    val isEditableText: Boolean = false
)

@Immutable
data class FileExplorerState(
    val tabs: List<ExplorerTab> = listOf(
        ExplorerTab(id = "tab_initial", location = ExplorerLocation.Home)
    ),
    val activeTabIndex: Int = 0,
    val currentItems: List<ExplorerItem> = emptyList(),
    val recentFiles: List<ExplorerItem> = emptyList(),
    val mountedSafFolders: List<MountedSafFolder> = emptyList(),
    val selectedIds: Set<String> = emptySet(),
    val clipboard: ExplorerClipboard? = null,
    val viewMode: ExplorerViewMode = ExplorerViewMode.DETAILS,
    val sortBy: ExplorerSortBy = ExplorerSortBy.NAME,
    val sortAscending: Boolean = true,
    val searchQuery: String = "",
    val showDetailsPane: Boolean = false,
    val isLoading: Boolean = false,
    val previewState: ExplorerPreviewState? = null
) {
    val activeTab: ExplorerTab
        get() = tabs.getOrElse(activeTabIndex) { tabs.first() }
}
