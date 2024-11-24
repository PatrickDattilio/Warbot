package com.warlabel

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.datetime.Clock
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import work.socialhub.kbsky.api.entity.app.bsky.feed.FeedGetLikesResponse
import work.socialhub.kbsky.model.app.bsky.feed.FeedGetLikesLike
import java.io.IOException
import kotlin.random.Random

class LikeManager(val tokenManager: TokenManager, val labelerTokenManger: TokenManager) {


    fun applyLabel(legion: Int, did: String, token: String): String {

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
        println(Clock.System.now().toString() +"Applying label: $firstbornLabel $did")

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
        return label
    }

    fun fetchAndProcess(token: String) {
        try {
            val client = OkHttpClient().newBuilder()
                .build()
            var cursor: String? = ""
            do {
                val request = Request.Builder()
                    .url("https://maitake.us-west.host.bsky.network/xrpc/app.bsky.feed.getLikes?uri=at://did:plc:mpogduvvraozdcbp6w2lafqg/app.bsky.labeler.service/self$cursor")
                    .get()
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer $token")
                    .header("atproto-accept-labelers", "did:plc:mpogduvvraozdcbp6w2lafqg")
                    .build()
                val response = client.newCall(request).execute()
                if (!response.isSuccessful) throw IOException("Unexpected code $response")
                val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
                val adapter = moshi.adapter(FeedGetLikesResponse::class.java)
                val likeResponse = adapter.fromJson(response.body!!.source())
                response.close()
                processLikes(likeResponse!!.likes)
                cursor = likeResponse.cursor
                cursor = if (cursor == null) {
                    ""
                } else {
                    "&cursor=${cursor}"
                }
            } while (cursor?.isNotBlank() == true)

        } catch (throwable: Throwable) {
            println(Clock.System.now().toString() +throwable)
        }
    }

    private fun processLikes(likes: List<FeedGetLikesLike>) {
        likes.forEach { like ->
            val user = like.actor
            val userDid = like.actor.did
            val dbUser = transaction { Player.selectAll().where(Player.did.eq(userDid)).firstOrNull() }

            if (dbUser != null) {
                // We good
            } else {
                var hasLabel = false
                var label = ""
                user.labels?.forEach { serverLabel ->
                    if (serverLabel.`val` != "firstborn" && serverLabel.src == "did:plc:mpogduvvraozdcbp6w2lafqg") {
                        label = serverLabel.`val`!!
                        hasLabel = true
                    }
                }
                if (!hasLabel) {
                    label = applyLabel(Random.nextInt(0, 18), userDid, labelerTokenManger.getToken())
                }
                transaction {
                    Player.insert {
                        it[did] = userDid
                        it[name] = user.handle
                        it[tag] = label
                    }
                }


            }
        }

    }

}
