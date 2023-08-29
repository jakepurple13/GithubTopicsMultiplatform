package com.programmersbox.common.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.programmersbox.common.Database
import com.programmersbox.common.GitHubTopic
import com.programmersbox.common.Network
import com.programmersbox.common.SettingInformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope

class TopicViewModel(
    s: Flow<SettingInformation>
) : ViewModel() {
    val items = mutableStateListOf<GitHubTopic>()
    var isLoading by mutableStateOf(true)
    var singleTopic by mutableStateOf(true)
    val currentTopics = mutableStateListOf<String>()
    val topicList = mutableStateListOf<String>()
    var page by mutableStateOf(1)

    val db: Database by lazy { Database() }

    init {
        s
            .map { it.topicList }
            .distinctUntilChanged()
            .onEach {
                topicList.clear()
                topicList.addAll(it)
            }
            .launchIn(viewModelScope)

        s
            .map { it.currentTopics }
            .distinctUntilChanged()
            .onEach {
                currentTopics.clear()
                currentTopics.addAll(it)
                if (it.isNotEmpty()) refresh()
            }
            .launchIn(viewModelScope)

        s
            .map { it.singleTopic }
            .distinctUntilChanged()
            .onEach { singleTopic = it }
            .launchIn(viewModelScope)
    }

    private suspend fun loadTopics() {
        isLoading = true
        withContext(Dispatchers.IO) {
            Network.getTopics(page, *currentTopics.toTypedArray()).fold(
                onSuccess = { items.addAll(it) },
                onFailure = { it.printStackTrace() }
            )
        }
        isLoading = false
    }

    suspend fun refresh() {
        items.clear()
        page = 1
        loadTopics()
    }

    suspend fun newPage() {
        page++
        loadTopics()
    }

    fun setTopic(topic: String) {
        viewModelScope.launch {
            if (singleTopic) {
                db.setCurrentTopic(topic)
            } else {
                if (topic !in currentTopics) {
                    db.addCurrentTopic(topic)
                } else {
                    db.removeCurrentTopic(topic)
                }
            }
        }
    }

    fun addTopic(topic: String) {
        viewModelScope.launch {
            if (topic !in topicList) {
                db.addTopic(topic)
            }
        }
    }

    fun removeTopic(topic: String) {
        viewModelScope.launch {
            db.removeTopic(topic)
        }
    }

    fun toggleSingleTopic() {
        viewModelScope.launch {
            db.singleTopicToggle(!singleTopic)
        }
    }
}