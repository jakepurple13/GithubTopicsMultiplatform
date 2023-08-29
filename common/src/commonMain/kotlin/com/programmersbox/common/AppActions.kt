package com.programmersbox.common

import androidx.compose.runtime.Immutable

@Immutable
data class AppActions(
    val onCardClick: (GitHubTopic) -> Unit = {},
    val onNewTabOpen: (GitHubTopic) -> Unit = {},
    val onNewWindow: (GitHubTopic) -> Unit = {},
    val onShareClick: (GitHubTopic) -> Unit = {},
    val onSettingsClick: () -> Unit = {},
    val showLibrariesUsed: () -> Unit = {},
    val showFavorites: () -> Unit = {}
)