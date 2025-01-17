package eu.kanade.presentation.library.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.FlipToBack
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SelectAll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.components.DropdownMenu
import eu.kanade.presentation.components.Pill
import eu.kanade.presentation.library.LibraryState
import eu.kanade.presentation.theme.active
import eu.kanade.presentation.util.calculateWindowWidthSizeClass
import eu.kanade.tachiyomi.R
import kotlinx.coroutines.delay

@Composable
fun LibraryToolbar(
    state: LibraryState,
    title: LibraryToolbarTitle,
    incognitoMode: Boolean,
    downloadedOnlyMode: Boolean,
    onClickUnselectAll: () -> Unit,
    onClickSelectAll: () -> Unit,
    onClickInvertSelection: () -> Unit,
    onClickFilter: () -> Unit,
    onClickRefresh: () -> Unit,
    // SY -->
    onClickSyncExh: () -> Unit,
    // SY <--
    scrollBehavior: TopAppBarScrollBehavior?,
) = when {
    state.selectionMode -> LibrarySelectionToolbar(
        state = state,
        incognitoMode = incognitoMode,
        downloadedOnlyMode = downloadedOnlyMode,
        onClickUnselectAll = onClickUnselectAll,
        onClickSelectAll = onClickSelectAll,
        onClickInvertSelection = onClickInvertSelection,
    )
    state.searchQuery != null -> LibrarySearchToolbar(
        searchQuery = state.searchQuery!!,
        incognitoMode = incognitoMode,
        downloadedOnlyMode = downloadedOnlyMode,
        onChangeSearchQuery = { state.searchQuery = it },
        onClickCloseSearch = { state.searchQuery = null },
        scrollBehavior = scrollBehavior,
    )
    else -> LibraryRegularToolbar(
        title = title,
        hasFilters = state.hasActiveFilters,
        incognitoMode = incognitoMode,
        downloadedOnlyMode = downloadedOnlyMode,
        onClickSearch = { state.searchQuery = "" },
        onClickFilter = onClickFilter,
        onClickRefresh = onClickRefresh,
        // SY -->
        showSyncExh = state.showSyncExh,
        onClickSyncExh = onClickSyncExh,
        // SY <--
        scrollBehavior = scrollBehavior,
    )
}

@Composable
fun LibraryRegularToolbar(
    title: LibraryToolbarTitle,
    hasFilters: Boolean,
    incognitoMode: Boolean,
    downloadedOnlyMode: Boolean,
    onClickSearch: () -> Unit,
    onClickFilter: () -> Unit,
    onClickRefresh: () -> Unit,
    // SY -->
    showSyncExh: Boolean,
    onClickSyncExh: () -> Unit,
    // SY <--
    scrollBehavior: TopAppBarScrollBehavior?,
) {
    val pillAlpha = if (isSystemInDarkTheme()) 0.12f else 0.08f
    val filterTint = if (hasFilters) MaterialTheme.colorScheme.active else LocalContentColor.current
    AppBar(
        titleContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title.text,
                    maxLines = 1,
                    modifier = Modifier.weight(1f, false),
                    overflow = TextOverflow.Ellipsis,
                )
                if (title.numberOfManga != null) {
                    Pill(
                        text = "${title.numberOfManga}",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = pillAlpha),
                        fontSize = 14.sp,
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = onClickSearch) {
                Icon(Icons.Outlined.Search, contentDescription = stringResource(R.string.action_search))
            }
            IconButton(onClick = onClickFilter) {
                Icon(Icons.Outlined.FilterList, contentDescription = stringResource(R.string.action_filter), tint = filterTint)
            }
            // SY -->
            val moveGlobalUpdate = showSyncExh && calculateWindowWidthSizeClass() == WindowWidthSizeClass.Compact
            if (!moveGlobalUpdate) {
                IconButton(onClick = onClickRefresh) {
                    Icon(Icons.Outlined.Refresh, contentDescription = stringResource(R.string.pref_category_library_update))
                }
            }
            var showOverflow by remember { mutableStateOf(false) }
            if (showSyncExh) {
                IconButton(onClick = { showOverflow = true }) {
                    Icon(Icons.Outlined.MoreVert, contentDescription = "more")
                }
            }
            DropdownMenu(showOverflow, onDismissRequest = { showOverflow = false }) {
                if (moveGlobalUpdate) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.pref_category_library_update)) },
                        onClick = onClickRefresh,
                    )
                }
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.sync_favorites)) },
                    onClick = onClickSyncExh,
                )
            }
            // SY <--
        },
        incognitoMode = incognitoMode,
        downloadedOnlyMode = downloadedOnlyMode,
        scrollBehavior = scrollBehavior,
    )
}

@Composable
fun LibrarySelectionToolbar(
    state: LibraryState,
    incognitoMode: Boolean,
    downloadedOnlyMode: Boolean,
    onClickUnselectAll: () -> Unit,
    onClickSelectAll: () -> Unit,
    onClickInvertSelection: () -> Unit,
) {
    val backgroundColor by TopAppBarDefaults.smallTopAppBarColors().containerColor(1f)
    AppBar(
        modifier = Modifier
            .drawBehind {
                drawRect(backgroundColor.copy(alpha = 1f))
            },
        titleContent = { Text(text = "${state.selection.size}") },
        actions = {
            IconButton(onClick = onClickSelectAll) {
                Icon(Icons.Outlined.SelectAll, contentDescription = "search")
            }
            IconButton(onClick = onClickInvertSelection) {
                Icon(Icons.Outlined.FlipToBack, contentDescription = "invert")
            }
        },
        isActionMode = true,
        onCancelActionMode = onClickUnselectAll,
        incognitoMode = incognitoMode,
        downloadedOnlyMode = downloadedOnlyMode,
    )
}

@Composable
fun LibrarySearchToolbar(
    searchQuery: String,
    incognitoMode: Boolean,
    downloadedOnlyMode: Boolean,
    onChangeSearchQuery: (String) -> Unit,
    onClickCloseSearch: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior?,
) {
    val focusRequester = remember { FocusRequester.Default }
    AppBar(
        navigateUp = onClickCloseSearch,
        titleContent = {
            BasicTextField(
                value = searchQuery,
                onValueChange = onChangeSearchQuery,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onBackground),
                singleLine = true,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
            )
            LaunchedEffect(focusRequester) {
                // TODO: https://issuetracker.google.com/issues/204502668
                delay(100)
                focusRequester.requestFocus()
            }
        },
        incognitoMode = incognitoMode,
        downloadedOnlyMode = downloadedOnlyMode,
        scrollBehavior = scrollBehavior,
    )
}

data class LibraryToolbarTitle(
    val text: String,
    val numberOfManga: Int? = null,
)
