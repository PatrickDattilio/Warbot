package com.warlabel

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class LabelManager {

    fun applyLabel(did: String, label: String, token: String) {
        val MEDIA_TYPE = "application/json".toMediaType()

        val client = OkHttpClient().newBuilder()
            .build()
        val requestBody =
            "{\"subject\":{\"\$type\":\"com.atproto.admin.defs#repoRef\",\"did\":\"$did\"},\"createdBy\":\"did:plc:mpogduvvraozdcbp6w2lafqg\",\"subjectBlobCids\":[],\"event\":{\"\$type\":\"tools.ozone.moderation.defs#modEventLabel\",\"createLabelVals\":[$label],\"negateLabelVals\":[]}}"

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
}