package com.programmersbox.common

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeUIViewController
import com.programmersbox.common.components.IconsButton
import com.programmersbox.common.viewmodels.FavoritesViewModel
import com.programmersbox.common.viewmodels.RepoViewModel
import com.programmersbox.common.viewmodels.TopicViewModel
import kotlinx.coroutines.launch
import platform.UIKit.UIViewController

public actual fun getPlatformName(): String {
    return "iOS"
}

@Composable
private fun UIShow() {
    App()
}

public fun MainViewController(): UIViewController = ComposeUIViewController {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                UIShow()
            }
        }
    }
}

actual val refreshIcon = false

actual val useInfiniteLoader = true

@Composable
actual fun TopicItemModification(item: GitHubTopic, content: @Composable () -> Unit) {
    content()
}

@OptIn(ExperimentalMaterial3Api::class)
val LocalTopicDrawerState = staticCompositionLocalOf<DrawerState> { error("Nothing Here!") }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun TopicDrawerLocation(
    vm: TopicViewModel,
    favoritesVM: FavoritesViewModel
) {
    val drawerState = LocalTopicDrawerState.current
    val scope = rememberCoroutineScope()

    DismissibleNavigationDrawer(
        drawerContent = { DismissibleDrawerSheet { TopicDrawer(vm) } },
        drawerState = drawerState
    ) {
        GithubTopicUI(
            vm = vm,
            favoritesVM = favoritesVM,
            navigationIcon = {
                IconsButton(
                    onClick = { scope.launch { if (drawerState.isOpen) drawerState.close() else drawerState.open() } },
                    icon = Icons.Default.Menu
                )
            }
        )
    }
}

@Composable
actual fun BoxScope.ReposScrollBar(lazyListState: LazyListState) {
}

@Composable
actual fun BoxScope.ScrollBar(scrollState: ScrollState) {
}

@Composable
actual fun SwipeRefreshWrapper(
    paddingValues: PaddingValues,
    isRefreshing: Boolean,
    onRefresh: suspend () -> Unit,
    content: @Composable () -> Unit
) {
    content()
}

@Composable
actual fun BoxScope.LoadingIndicator(isLoading: Boolean) {
}

@Composable
actual fun RowScope.RepoViewToggle(repoVM: RepoViewModel) {
}

@Composable
actual fun RepoContentView(repoVM: RepoViewModel, modifier: Modifier, defaultContent: @Composable () -> Unit) {
    defaultContent()
}

@Composable
actual fun RepoSetup(repoVM: RepoViewModel, content: @Composable () -> Unit) {
    content()
}

@Composable
actual fun MarkdownText(text: String, modifier: Modifier) {

}