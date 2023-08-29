package com.programmersbox.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleLeft
import androidx.compose.material.icons.filled.ArrowCircleRight
import androidx.compose.material.icons.filled.WebAsset
import androidx.compose.material.icons.filled.WebAssetOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.dp
import com.programmersbox.common.viewmodels.FavoritesViewModel
import com.programmersbox.common.viewmodels.RepoViewModel
import com.programmersbox.common.viewmodels.TopicViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.cef.browser.CefBrowser
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.HorizontalSplitPane
import org.jetbrains.compose.splitpane.rememberSplitPaneState
import java.awt.Cursor

public actual fun getPlatformName(): String {
    return "Desktop"
}

@Composable
public fun UIShow() {
    App()
}

actual val refreshIcon = true

actual val useInfiniteLoader = false

@Composable
actual fun TopicItemModification(item: GitHubTopic, content: @Composable () -> Unit) {
    val actions = LocalAppActions.current
    val uriHandler = LocalUriHandler.current
    ContextMenuArea(
        items = {
            listOf(
                ContextMenuItem("Open") { actions.onCardClick(item) },
                ContextMenuItem("Open in New Tab") { actions.onNewTabOpen(item) },
                ContextMenuItem("Open in New Window") { actions.onNewWindow(item) },
                ContextMenuItem("Open in Browser") { uriHandler.openUri(item.htmlUrl) },
                ContextMenuItem("Share") { actions.onShareClick(item) },
            )
        },
        content = content
    )
}

@OptIn(ExperimentalSplitPaneApi::class)
@Composable
actual fun TopicDrawerLocation(vm: TopicViewModel, favoritesVM: FavoritesViewModel) {
    val splitter = rememberSplitPaneState()

    HorizontalSplitPane(splitPaneState = splitter) {
        first(250.dp) { TopicDrawer(vm) }
        second(550.dp) { GithubTopicUI(vm, favoritesVM) }

        splitter {
            visiblePart {
                Box(
                    Modifier.width(2.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.onBackground)
                )
            }
            handle {
                Box(
                    Modifier
                        .markAsHandle()
                        .pointerHoverIcon(PointerIcon(Cursor(Cursor.E_RESIZE_CURSOR)))
                        .background(SolidColor(Color.Gray), alpha = 0.50f)
                        .width(2.dp)
                        .fillMaxHeight()
                )
            }
        }
    }
}

@Composable
actual fun BoxScope.ReposScrollBar(lazyListState: LazyListState) {
    VerticalScrollbar(
        adapter = rememberScrollbarAdapter(lazyListState),
        modifier = Modifier
            .align(Alignment.CenterEnd)
            .fillMaxHeight()
            .padding(end = 4.dp)
    )
}

@Composable
actual fun BoxScope.ScrollBar(scrollState: ScrollState) {
    VerticalScrollbar(
        adapter = rememberScrollbarAdapter(scrollState),
        modifier = Modifier
            .align(Alignment.CenterEnd)
            .fillMaxHeight()
    )
}

@Composable
actual fun SwipeRefreshWrapper(
    paddingValues: PaddingValues,
    isRefreshing: Boolean,
    onRefresh: suspend () -> Unit,
    content: @Composable () -> Unit
) = content()

@Composable
actual fun BoxScope.LoadingIndicator(isLoading: Boolean) {
    AnimatedVisibility(isLoading, modifier = Modifier.align(Alignment.TopCenter)) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.TopCenter))
    }
}

@Composable
actual fun MarkdownText(text: String, modifier: Modifier) {
    /*Markdown(
        content = text,
        modifier = modifier
    )*/
}

@Composable
actual fun RepoSetup(repoVM: RepoViewModel, content: @Composable () -> Unit) {
    val browser = LocalBrowserHandler.current
    CompositionLocalProvider(
        LocalBrowser provides remember { browser.createBrowser(repoVM.item.htmlUrl) }
    ) {
        content()
    }
}

public val LocalBrowser: ProvidableCompositionLocal<CefBrowser> =
    staticCompositionLocalOf { error("Nope") }

@Composable
actual fun RowScope.RepoViewToggle(repoVM: RepoViewModel) {
    NavigationBarItem(
        selected = repoVM.showWebView,
        onClick = { repoVM.showWebView = !repoVM.showWebView },
        icon = {
            Icon(
                if (repoVM.showWebView) Icons.Default.WebAsset else Icons.Default.WebAssetOff,
                null
            )
        },
        label = { Text("Show WebView") }
    )

    AnimatedVisibility(
        repoVM.showWebView,
        modifier = Modifier.weight(1f)
    ) {
        val browser = LocalBrowser.current
        Row {
            NavigationBarItem(
                selected = false,
                onClick = { browser.goBack() },
                icon = { Icon(Icons.Default.ArrowCircleLeft, null) },
                label = { Text("Back") },
                enabled = repoVM.showWebView
            )

            NavigationBarItem(
                selected = false,
                onClick = { browser.goForward() },
                icon = { Icon(Icons.Default.ArrowCircleRight, null) },
                label = { Text("Forward") },
                enabled = repoVM.showWebView
            )
        }
    }
}

@Composable
actual fun RepoContentView(repoVM: RepoViewModel, modifier: Modifier, defaultContent: @Composable () -> Unit) {
    Crossfade(repoVM.showWebView) { target ->
        when (target) {
            true -> WebView(LocalBrowser.current.uiComponent, modifier)
            false -> defaultContent()
        }
    }
}