package com.programmersbox.common

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.programmersbox.common.viewmodels.FavoritesViewModel
import com.programmersbox.common.viewmodels.RepoViewModel
import com.programmersbox.common.viewmodels.TopicViewModel

public expect fun getPlatformName(): String

expect val refreshIcon: Boolean

expect val useInfiniteLoader: Boolean

@Composable
expect fun TopicItemModification(item: GitHubTopic, content: @Composable () -> Unit)

@Composable
expect fun TopicDrawerLocation(vm: TopicViewModel, favoritesVM: FavoritesViewModel)

@Composable
expect fun BoxScope.ReposScrollBar(lazyListState: LazyListState)

@Composable
expect fun BoxScope.ScrollBar(scrollState: ScrollState)

@Composable
expect fun BoxScope.LoadingIndicator(isLoading: Boolean)

@Composable
expect fun SwipeRefreshWrapper(
    paddingValues: PaddingValues,
    isRefreshing: Boolean,
    onRefresh: suspend () -> Unit,
    content: @Composable () -> Unit
)

@Composable
expect fun MarkdownText(text: String, modifier: Modifier = Modifier)

@Composable
expect fun RowScope.RepoViewToggle(repoVM: RepoViewModel)

@Composable
expect fun RepoContentView(repoVM: RepoViewModel, modifier: Modifier, defaultContent: @Composable () -> Unit)

@Composable
expect fun RepoSetup(repoVM: RepoViewModel, content: @Composable () -> Unit)