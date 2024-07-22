package com.warlabel

import work.socialhub.kbsky.BlueskyFactory
import work.socialhub.kbsky.api.entity.com.atproto.server.ServerCreateSessionRequest
import work.socialhub.kbsky.api.entity.share.AuthRequest
import work.socialhub.kbsky.domain.Service
import java.time.Instant
import kotlin.time.Duration.Companion.minutes

class TokenManager(val username: String, val password: String) {
    private var _token: String? = null
    private var _refresh: String? = null
    private var lastRefresh: Long = System.currentTimeMillis()

    fun getToken(): String {
        val now = System.currentTimeMillis()
        return if (_token == null) {
            fetchToken()
        } else if (now - lastRefresh >= 5.minutes.inWholeMilliseconds) {
            fetchRefresh(now)
        } else {
            _token!!
        }
    }

    fun fetchRefresh(now: Long): String {
        val refreshed = BlueskyFactory.instance(Service.BSKY_SOCIAL.uri).server().refreshSession(
            AuthRequest(_refresh!!)
        )
        _token = refreshed.data.accessJwt
        _refresh = refreshed.data.refreshJwt
        lastRefresh = now
        println(Instant.now().toString() + " Refreshed token")
        return _token!!
    }

    fun fetchToken(): String {
        val tokens =
            BlueskyFactory.instance(Service.BSKY_SOCIAL.uri).server().createSession(ServerCreateSessionRequest().also {
                it.identifier = username
                it.password = password
            })
        _token = tokens.data.accessJwt
        _refresh = tokens.data.refreshJwt
        return _token!!
    }
}
