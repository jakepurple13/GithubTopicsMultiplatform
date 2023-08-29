package com.programmersbox.common.viewmodels

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.programmersbox.common.Database
import com.programmersbox.common.FavoritesDatabase
import com.programmersbox.common.GitHubTopic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope

public class FavoritesViewModel(database: Database) : ViewModel() {
    public val items: SnapshotStateList<GitHubTopic> = mutableStateListOf()
    public val db: FavoritesDatabase by lazy { FavoritesDatabase(database) }

    init {
        db.favoriteRepos()
            .distinctUntilChanged()
            .onEach {
                items.clear()
                items.addAll(it)
            }
            .launchIn(viewModelScope)
    }

    public fun addFavorite(repo: GitHubTopic) {
        viewModelScope.launch { db.addFavorite(repo) }
    }

    public fun removeFavorite(repo: GitHubTopic) {
        viewModelScope.launch { db.removeFavorite(repo) }
    }
}
