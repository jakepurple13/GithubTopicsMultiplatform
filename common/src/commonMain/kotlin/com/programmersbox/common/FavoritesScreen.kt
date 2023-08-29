package com.programmersbox.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.programmersbox.common.components.IconsButton
import com.programmersbox.common.components.TopicItem
import com.programmersbox.common.viewmodels.FavoritesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesUi(
    favoritesVM: FavoritesViewModel,
    backAction: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favorites") },
                navigationIcon = { IconsButton(onClick = backAction, icon = Icons.Default.ArrowBack) },
                actions = { Text("${favoritesVM.items.size} favorites") },
                scrollBehavior = scrollBehavior
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { padding ->
        val state = rememberLazyListState()
        Box(
            modifier = Modifier
                .padding(padding)
                .padding(vertical = 2.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                state = state
            ) {
                items(favoritesVM.items) {
                    FavoriteItem(it) { b ->
                        if (b) favoritesVM.removeFavorite(it)
                        else favoritesVM.addFavorite(it)
                    }
                }
            }
            ReposScrollBar(state)
        }
    }
}

@Composable
internal fun FavoriteItem(
    item: GitHubTopic,
    onFavoriteClick: (Boolean) -> Unit,
) {
    TopicItem(
        item = item,
        savedTopics = emptyList(),
        currentTopics = emptyList(),
        onCardClick = LocalAppActions.current.onCardClick,
        onTopicClick = {},
        onFavoriteClick = onFavoriteClick,
        isFavorite = true
    )
}