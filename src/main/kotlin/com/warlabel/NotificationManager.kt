package com.warlabel

import kotlinx.datetime.Clock
import work.socialhub.kbsky.BlueskyFactory
import work.socialhub.kbsky.api.entity.app.bsky.feed.FeedPostRequest
import work.socialhub.kbsky.api.entity.app.bsky.notification.NotificationListNotificationsRequest
import work.socialhub.kbsky.api.entity.app.bsky.notification.NotificationUpdateSeenRequest
import work.socialhub.kbsky.domain.Service
import work.socialhub.kbsky.model.app.bsky.feed.FeedPostReplyRef
import work.socialhub.kbsky.model.com.atproto.repo.RepoStrongRef

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
                    if (feedpost?.text?.matches(helpRegex) == true) {
                        handleHelp(token, notif.uri, notif.cid)
                    } else if (feedpost?.text?.matches(attackRegex) == true) {
                        handleAttack(token)
                    } else if (feedpost?.text?.matches(legionRegex) == true) {
                        val legion = legionRegex.find(feedpost.text!!)?.groupValues?.last() ?: ""
                        if (legion.isBlank()) {
                            handleLegionNotFound(token, notif.uri, notif.cid)
                        } else {
                            handleLegion(token, notif.uri, notif.cid)
                        }
                    }

                    println("${notif.reason} ${notif.author.handle} ${feedpost?.text}")

                }
            BlueskyFactory
                .instance(Service.BSKY_SOCIAL.uri)
                .notification()
                .updateSeen(NotificationUpdateSeenRequest(token).also {
                    it.seenAt = Clock.System.now().toString()
                })
        } catch (throwable: Throwable) {
            println(throwable.toString())
        }
    }

    private fun handleLegion(token: String, uri: String, cid: String) {
        TODO("Not yet implemented")
    }

    private fun handleLegionNotFound(token: String,uri: String, cid: String) {
        BlueskyFactory
            .instance(Service.BSKY_SOCIAL.uri)
            .feed().post(FeedPostRequest(token).also {
                it.text = "Sorry, I don't know that legion. Try using all lowercase and - for spaces."
                it.reply = FeedPostReplyRef().also {
                    it.root = RepoStrongRef(uri, cid)
                    it.parent = RepoStrongRef(uri, cid)
                }
            })
    }

    private fun handleHelp(token: String, uri: String, cid: String) {
        BlueskyFactory
            .instance(Service.BSKY_SOCIAL.uri)
            .feed().post(FeedPostRequest(token).also {
                it.text = "Commands:\nhelp: this menu"
                it.reply = FeedPostReplyRef().also {
                    it.root = RepoStrongRef(uri, cid)
                    it.parent = RepoStrongRef(uri, cid)
                }
            })
    }

    fun handleAttack(token: String) {
        //fetch person
        //run attack
        val attacks = Attacks()
        val result = attacks.handleAttack("test")
        //post result
//        BlueskyFactory
//            .instance(Service.BSKY_SOCIAL.uri)
//            .feed().post(FeedPostRequest(token).also {
//                it.text = result
//                it.reply = FeedPostReplyRef().also {
//                    it.root
//                }
//            })
    }

}