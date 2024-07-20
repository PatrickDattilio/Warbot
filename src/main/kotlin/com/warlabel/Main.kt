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
                it.identifier = "warlabel.bsky.social"
                it.password = "TVR6vhu!rnj5tnc@gut"
            }
        )
    var token = tokens.data.accessJwt
    var refresh = tokens.data.refreshJwt
    var lastRefresh = System.currentTimeMillis()
    val notificationManager = NotificationManager()

//    println("Logged in: ${response.data.accessJwt}")
//    var cursor: String? = null
//    val likes = File("users.txt").useLines { it.toMutableList() }

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

        }catch (throwable:Throwable){
            println(throwable.toString())
        }

        Thread.sleep(5000)
    }
//        try {
//
//            val likesResponse = BlueskyFactory
//                .instance(Service.BSKY_SOCIAL.uri)
//                .feed()
//                .getLikes(FeedGetLikesRequest(response.data.accessJwt).also {
//                    it.uri = "at://did:plc:mpogduvvraozdcbp6w2lafqg/app.bsky.labeler.service/self"
//                    it.limit = 50
//                    it.cursor = cursor
//                }
//                )
//
//            cursor = likesResponse.data.cursor
//            likesResponse.data.likes.forEach { like: FeedGetLikesLike ->
//                val did = like.actor.did
//                if (!likes.contains(did)) {
//                    //add to list
//                    likes.add(did)
//                    // determine labels
//                    val legion = Random.nextInt(0, 18)
//                    //Apply labels
//                    applyLabel(legion, did, response.data.accessJwt)
//                    File("users.txt").appendText(did + "\r\n")
//                }
//            }
//            println("Num Likes: ${likes.size}")
//        } catch (throwable: Throwable) {
//            println(throwable)
//        }
//        Thread.sleep(5000)
//    }
}

fun applyLabel(legion: Int, did: String, token: String) {

    val MEDIA_TYPE = "application/json".toMediaType()

    val label = when (legion) {
        0 -> "adeptus-custodes"
        1 -> "dark-angels"
        2 -> "emperors-children"
        3 -> "iron-warriors"
        4 -> "white-scars"
        5 -> "space-wolves"
        6 -> "imperial-fists"
        7 -> "night-lords"
        8 -> "blood-angels"
        9 -> "iron-hands"
        10 -> "world-eaters"
        11 -> "ultramarines"
        12 -> "death-guard"
        13 -> "thousand-sons"
        14 -> "sons-of-horus"
        15 -> "word-bearers"
        16 -> "salamanders"
        17 -> "raven-guard"
        18 -> "alpha-legion"
        else -> throw Throwable("Bad legion value: $legion")
    }
    val firstbornLabel = "\"firstborn\",\"$label\""
    println("Applying label: $firstbornLabel $did")

    val client = OkHttpClient().newBuilder()
        .build()
    val requestBody =
        "{\"subject\":{\"\$type\":\"com.atproto.admin.defs#repoRef\",\"did\":\"$did\"},\"createdBy\":\"did:plc:mpogduvvraozdcbp6w2lafqg\",\"subjectBlobCids\":[],\"event\":{\"\$type\":\"tools.ozone.moderation.defs#modEventLabel\",\"createLabelVals\":[$firstbornLabel],\"negateLabelVals\":[]}}"

    val request = Request.Builder()
        .url("https://verpa.us-west.host.bsky.network/xrpc/tools.ozone.moderation.emitEvent")
        .post(requestBody.toRequestBody(MEDIA_TYPE))
        .header("Content-Type", "application/json")
        .header("Accept", "application/json")
        .header("Authorization", "Bearer $token")
        .header("atproto-proxy", "did:plc:mpogduvvraozdcbp6w2lafqg#atproto_labeler")
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) throw IOException("Unexpected code $response")
        response.body!!.string()
    }
}
