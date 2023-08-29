package com.programmersbox.common

import io.github.reactivecircus.cache4k.Cache
import kotlin.time.Duration.Companion.minutes

internal data class CachedTopic(val topic: GitHubTopic, val repoContent: String)

internal object Cached {

    private val map = mutableMapOf<String, CachedTopic>()

    val cache = Cache.Builder<String, CachedTopic>()
        .expireAfterWrite(5.minutes)
        .maximumCacheSize(10)
        .build()

    operator fun get(key: String) = cache.get(key)
    operator fun set(key: String, topic: CachedTopic) = cache.put(key, topic)
}