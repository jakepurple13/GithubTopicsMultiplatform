package com.programmersbox.common

import io.realm.kotlin.MutableRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.asFlow
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.migration.AutomaticSchemaMigration
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SettingInformation : RealmObject {
    var currentTopics: RealmList<String> = realmListOf()
    var topicList: RealmList<String> = realmListOf()
    var isDarkMode: Boolean = true
    var singleTopic: Boolean = true
    var closeOnExit: Boolean = false
}

class Database {
    val realm = Realm.open(
        RealmConfiguration.Builder(
            schema = setOf(
                SettingInformation::class,
                Favorites::class
            )
        )
            .schemaVersion(1)
            .migration(AutomaticSchemaMigration { })
            .build()
    )

    val settingInformation = realm
        .initDbBlocking { SettingInformation() }
        .asFlow()
        .mapNotNull { it.obj }

    suspend fun setCurrentTopic(topic: String) {
        if (topic.isNotEmpty()) {
            updateInfo {
                it?.currentTopics?.clear()
                it?.currentTopics?.add(topic)
            }
        }
    }

    suspend fun addCurrentTopic(topic: String) {
        if (topic.isNotEmpty()) {
            updateInfo { it?.currentTopics?.add(topic) }
        }
    }

    suspend fun removeCurrentTopic(topic: String) {
        updateInfo { it?.currentTopics?.remove(topic) }
    }

    suspend fun addTopic(topic: String) {
        if (topic.isNotEmpty()) {
            updateInfo { it?.topicList?.add(topic) }
        }
    }

    suspend fun removeTopic(topic: String) {
        updateInfo { it?.topicList?.remove(topic) }
    }

    suspend fun changeMode(isDarkMode: Boolean) {
        updateInfo { it?.isDarkMode = isDarkMode }
    }

    suspend fun changeCloseOnExit(closeExit: Boolean) {
        updateInfo { it?.closeOnExit = closeExit }
    }

    private suspend fun updateInfo(block: MutableRealm.(SettingInformation?) -> Unit) {
        realm.query(SettingInformation::class).first().find()?.also { info ->
            realm.write { block(findLatest(info)) }
        }
    }

    suspend fun singleTopicToggle(toggle: Boolean) {
        updateInfo { it?.singleTopic = toggle }
    }
}

internal class Favorites : RealmObject {
    var favoriteRepos = realmListOf<String>()
}

class FavoritesDatabase(private val database: Database) {
    private val realm by lazy { database.realm }

    private val json = Json

    private fun flow() = realm.initDbBlocking { Favorites() }
        .asFlow()
        .mapNotNull { it.obj }
        .distinctUntilChanged()

    fun favoriteRepos() = flow()
        .map { fav -> fav.favoriteRepos.map { json.decodeFromString<GitHubTopic>(it) } }

    private suspend fun initialDb(): Favorites {
        val f = realm.query(Favorites::class).first().find()
        return f ?: realm.write { copyToRealm(Favorites()) }
    }

    suspend fun addFavorite(repo: GitHubTopic) {
        realm.updateInfo<Favorites> { it?.favoriteRepos?.add(json.encodeToString(repo)) }
    }

    suspend fun removeFavorite(repo: GitHubTopic) {
        realm.updateInfo<Favorites> {
            it?.favoriteRepos?.removeAll { t -> repo.htmlUrl == json.decodeFromString<GitHubTopic>(t).htmlUrl }
        }
    }
}

private suspend inline fun <reified T : RealmObject> Realm.updateInfo(crossinline block: MutableRealm.(T?) -> Unit) {
    query(T::class).first().find()?.also { info ->
        write { block(findLatest(info)) }
    }
}

private inline fun <reified T : RealmObject> Realm.initDbBlocking(crossinline default: () -> T): T {
    val f = query(T::class).first().find()
    return f ?: writeBlocking { copyToRealm(default()) }
}