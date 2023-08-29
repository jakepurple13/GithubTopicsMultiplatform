package com.programmersbox.common.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.programmersbox.common.*
import com.programmersbox.common.Cached
import com.programmersbox.common.CachedTopic
import com.programmersbox.common.Network
import com.programmersbox.common.ReadMeResponse
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope

public class RepoViewModel(
    topic: String
) : ViewModel() {
    public val item: GitHubTopic by lazy { NetworkRepository.holder.first { it.htmlUrl == topic } }
    public var repoContent: ReadMeResponse by mutableStateOf<ReadMeResponse>(ReadMeResponse.Loading)
    public var error: Boolean by mutableStateOf(false)
    public var showWebView: Boolean by mutableStateOf(false)

    init {
        viewModelScope.launch { load() }
    }

    public suspend fun load() {
        val cached = Cached[item.htmlUrl]
        if (cached != null) {
            println("Loading from cache")
            repoContent = ReadMeResponse.Success(cached.repoContent)
        } else {
            println("Loading from url")
            Network.getReadMe(item.fullName).fold(
                onSuccess = {
                    repoContent = it
                    if (it is ReadMeResponse.Success) Cached[item.htmlUrl] = CachedTopic(item, it.content)
                },
                onFailure = {
                    it.printStackTrace()
                    error = true
                }
            )
        }
    }
}