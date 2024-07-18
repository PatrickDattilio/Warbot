package com.warlabel

import kotlinx.datetime.Clock
import work.socialhub.kbsky.BlueskyFactory
import work.socialhub.kbsky.api.entity.app.bsky.feed.FeedPostRequest
import work.socialhub.kbsky.api.entity.app.bsky.notification.NotificationListNotificationsRequest
import work.socialhub.kbsky.api.entity.app.bsky.notification.NotificationUpdateSeenRequest
import work.socialhub.kbsky.domain.Service

class NotificationManager {
    private var legionRegex: Regex
    private var helpRegex: Regex
    private var attackRegex: Regex

    init {
        attackRegex = Regex("@warlabel.bsky.social attack")
        helpRegex = Regex("@warlabel.bsky.social help")
        legionRegex = Regex("@warlabel.bsky.social legion (.*)")
    }

    fun fetchAndProcess(token: String) {
        try {
            val notifs = BlueskyFactory
                .instance(Service.BSKY_SOCIAL.uri)
                .notification()
                .listNotifications(
                    NotificationListNotificationsRequest(token)
                )
            notifs.data.notifications.filter { !it.isRead && it.reason == "mention" }
                .forEach { notif ->
                    val feedpost = notif.record.asFeedPost
                    if (feedpost?.text?.matches() == true) {
                        //fetch person
                        //run attack
                        val result = attacks.handleAttack("test")
                        //post result
                        BlueskyFactory
                            .instance(Service.BSKY_SOCIAL.uri)
                            .feed().post(FeedPostRequest(response.data.accessJwt).also { it.text = result
                                it.reply = feedpost.reply
                            })
                    }
                    println("${notif.reason} ${notif.author.handle} ${feedpost?.text}")
                    BlueskyFactory
                        .instance(Service.BSKY_SOCIAL.uri)
                        .notification()
                        .updateSeen(NotificationUpdateSeenRequest(response.data.accessJwt).also {
                            it.seenAt = Clock.System.now().toString()
                        })
                }
        }catch (throwable:Throwable){
            println(throwable.toString())
        }
    }

}