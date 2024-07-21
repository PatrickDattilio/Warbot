package com.warlabel

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import work.socialhub.kbsky.BlueskyFactory
import work.socialhub.kbsky.api.entity.com.atproto.server.ServerCreateSessionRequest
import work.socialhub.kbsky.api.entity.share.AuthRequest
import work.socialhub.kbsky.domain.Service
import java.io.IOException
import java.time.Instant
import kotlin.time.Duration.Companion.minutes


fun main() {
    val tokens = BlueskyFactory
        .instance(Service.BSKY_SOCIAL.uri)
        .server()
        .createSession(
            ServerCreateSessionRequest().also {
                it.identifier = "wlabelapi.bsky.social"
                it.password = "XQJ!hud-gup7kgn.mxe"
            }
        )
    var token = tokens.data.accessJwt
    var refresh = tokens.data.refreshJwt
    var lastRefresh = System.currentTimeMillis()
    val notificationManager = NotificationManager()
    val likeManager = LikeManager()

    while (true) {

        val now = System.currentTimeMillis()
        if (now - lastRefresh >= 5.minutes.inWholeMilliseconds) {
            //do refresh
            val refreshed = BlueskyFactory
                .instance(Service.BSKY_SOCIAL.uri)
                .server()
                .refreshSession(
                    AuthRequest(refresh)
                )
            token = refreshed.data.accessJwt
            refresh = refreshed.data.refreshJwt
            lastRefresh = now
            println(Instant.now().toString() + " Refreshed token")
        }
        try{
            notificationManager.fetchAndProcess(token)
            likeManager.fetchAndProcess(token)
        }catch (throwable:Throwable){
            println(throwable.toString())
        }

        Thread.sleep(5000)
    }
}

