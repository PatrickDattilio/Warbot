package com.warlabel

import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import work.socialhub.kbsky.BlueskyFactory
import work.socialhub.kbsky.api.entity.app.bsky.feed.FeedPostRequest
import work.socialhub.kbsky.api.entity.app.bsky.notification.NotificationListNotificationsRequest
import work.socialhub.kbsky.api.entity.app.bsky.notification.NotificationUpdateSeenRequest
import work.socialhub.kbsky.domain.Service
import work.socialhub.kbsky.model.app.bsky.feed.FeedPostReplyRef
import work.socialhub.kbsky.model.com.atproto.repo.RepoStrongRef

class NotificationManager(val labelerTokenManger: TokenManager) {
    private var legionsRegex: Regex
    private var legionRegex: Regex
    private var helpRegex: Regex
    private var attackRegex: Regex
    private var lastCursor: String? = null

    init {
        attackRegex = Regex("@warlabel.bsky.social attack")
        helpRegex = Regex("@warlabel.bsky.social help")
        legionRegex = Regex("@warlabel.bsky.social legion (.*)")
        legionsRegex = Regex("@warlabel.bsky.social legions")
    }

    fun fetchAndProcess() {
        try {
            val notifs = BlueskyFactory
                .instance(Service.BSKY_SOCIAL.uri)
                .notification()
                .listNotifications(
                    NotificationListNotificationsRequest(labelerTokenManger.getToken()).also {
                        it.cursor = lastCursor
                    }
                )
//            if (notifs.data.cursor != null) {
//                lastCursor = notifs.data.cursor
//            }
            notifs.data.notifications.filter { !it.isRead && it.reason == "mention" }
                .forEach { notif ->
                    val feedpost = notif.record.asFeedPost
                    println("${notif.reason} ${notif.author.handle} ${feedpost?.text}")
                    if (feedpost?.text?.matches(helpRegex) == true) {
                        handleHelp(labelerTokenManger.getToken(), notif.uri, notif.cid)
                    } else if (feedpost?.text?.matches(attackRegex) == true) {
                        handleAttack(labelerTokenManger.getToken())
                    }
                    else if(feedpost?.text?.matches(legionsRegex)== true){
                        handleLegionEmpty(labelerTokenManger.getToken(), notif.uri, notif.cid)
                    } else if (feedpost?.text?.matches(legionRegex) == true) {
                        val legion = legionRegex.find(feedpost.text!!)?.groupValues?.last() ?: ""
                        if (legion.isBlank()) {
                            handleLegionEmpty(labelerTokenManger.getToken(), notif.uri, notif.cid)
                        } else {
                            handleLegion(labelerTokenManger.getToken(), notif.uri, notif.cid, legion)
                        }
                    }

                    println("${notif.reason} ${notif.author.handle} ${feedpost?.text}")
                    BlueskyFactory
                        .instance(Service.BSKY_SOCIAL.uri)
                        .notification()
                        .updateSeen(NotificationUpdateSeenRequest(labelerTokenManger.getToken()).also {
                            it.seenAt = Clock.System.now().toString()
                        })
                }
        } catch (throwable: Throwable) {
            println(throwable.toString())
        }
    }

    private fun handleLegion(token: String, uri: String, cid: String, legion: String) {
        //get users
        val playerRows = transaction { Player.selectAll().where(Player.tag.eq(legion)).toList() }

        var response = "Battle brothers:\n"
        playerRows.forEach { resultRow ->
            response += resultRow[Player.name] + "\n"
        }
        BlueskyFactory
            .instance(Service.BSKY_SOCIAL.uri)
            .feed().post(FeedPostRequest(labelerTokenManger.getToken()).also {
                it.text = response
                it.reply = FeedPostReplyRef().also {
                    it.root = RepoStrongRef(uri, cid)
                    it.parent = RepoStrongRef(uri, cid)
                }
            })

    }

    private fun handleLegionEmpty(token: String, uri: String, cid: String) {
        BlueskyFactory
            .instance(Service.BSKY_SOCIAL.uri)
            .feed().post(FeedPostRequest(labelerTokenManger.getToken()).also {
                it.text =
                    "Valid legions are  adeptus-custodes,dark-angels,emperors-children,iron-warriors,white-scars,space-wolves,imperial-fists,night-lords,blood-angels,iron-hands,world-eaters,ultramarines,death-guard,thousand-sons,sons-of-horus,word-bearers,salamanders,raven-guard,alpha-legion"
                it.reply = FeedPostReplyRef().also {
                    it.root = RepoStrongRef(uri, cid)
                    it.parent = RepoStrongRef(uri, cid)
                }
            })
    }

    private fun handleHelp(token: String, uri: String, cid: String) {
        BlueskyFactory
            .instance(Service.BSKY_SOCIAL.uri)
            .feed().post(FeedPostRequest(labelerTokenManger.getToken()).also {
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