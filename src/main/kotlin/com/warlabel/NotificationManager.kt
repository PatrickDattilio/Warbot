package com.warlabel

import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import work.socialhub.kbsky.BlueskyFactory
import work.socialhub.kbsky.api.entity.app.bsky.feed.FeedPostRequest
import work.socialhub.kbsky.api.entity.app.bsky.notification.NotificationListNotificationsRequest
import work.socialhub.kbsky.api.entity.app.bsky.notification.NotificationUpdateSeenRequest
import work.socialhub.kbsky.domain.Service
import work.socialhub.kbsky.model.app.bsky.actor.ActorDefsProfileView
import work.socialhub.kbsky.model.app.bsky.feed.FeedPostReplyRef
import work.socialhub.kbsky.model.com.atproto.repo.RepoStrongRef

class NotificationManager(val tokenManger: TokenManager, val labelerTokenManager: TokenManager) {
    private val legionsRegex: Regex
    private val legionRegex: Regex
    private val helpRegex: Regex
    private val attackRegex: Regex
    private val statusRegex: Regex
    private var lastCursor: String? = null

    init {
        attackRegex = Regex("@warlabel.bsky.social attack")
        helpRegex = Regex("@warlabel.bsky.social help")
        legionRegex = Regex("@warlabel.bsky.social legion (.*)")
        legionsRegex = Regex("@warlabel.bsky.social legions")
        statusRegex = Regex("@warlabel.bsky.social status")
    }

    fun fetchAndProcess() {
        try {
            val notifs = BlueskyFactory
                .instance(Service.BSKY_SOCIAL.uri)
                .notification()
                .listNotifications(
                    NotificationListNotificationsRequest(labelerTokenManager.getToken()).also {
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
                        handleHelp(labelerTokenManager.getToken(), notif.uri, notif.cid)
                    } else if (feedpost?.text?.matches(attackRegex) == true) {
                        handleAttack(notif.author, notif.uri, notif.cid)
                    } else if (feedpost?.text?.matches(statusRegex) == true) {
                        handleStatus(notif.uri, notif.cid)
                    } else if (feedpost?.text?.matches(legionsRegex) == true) {
                        handleLegionEmpty(labelerTokenManager.getToken(), notif.uri, notif.cid)
                    } else if (feedpost?.text?.matches(legionRegex) == true) {
                        val legion = legionRegex.find(feedpost.text!!)?.groupValues?.last() ?: ""
                        if (legion.isBlank()) {
                            handleLegionEmpty(labelerTokenManager.getToken(), notif.uri, notif.cid)
                        } else {
                            handleLegion(labelerTokenManager.getToken(), notif.uri, notif.cid, legion)
                        }
                    }

                    println("${notif.reason} ${notif.author.handle} ${feedpost?.text}")
                    BlueskyFactory
                        .instance(Service.BSKY_SOCIAL.uri)
                        .notification()
                        .updateSeen(NotificationUpdateSeenRequest(labelerTokenManager.getToken()).also {
                            it.seenAt = Clock.System.now().toString()
                        })
                }
        } catch (throwable: Throwable) {
            println(throwable.toString())
        }
    }

    private fun handleStatus(uri: String, cid: String) {


        val imperium = transaction {  Attack.selectAll().where(Attack.isLoyalist.eq(true).and(Attack.damage.greaterEq(1))).toList()}
        val warmaster =  transaction { Attack.selectAll().where(Attack.isLoyalist.eq(false).and(Attack.damage.greaterEq(1))).toList()}
        val response = "Fallen Astartes of the Imperium: ${warmaster.size}\nFallen Astartes of the Warmaster: ${imperium.size}"

        BlueskyFactory
            .instance(Service.BSKY_SOCIAL.uri)
            .feed().post(FeedPostRequest(labelerTokenManager.getToken()).also {
                it.text = response
                it.reply = FeedPostReplyRef().also {
                    it.root = RepoStrongRef(uri, cid)
                    it.parent = RepoStrongRef(uri, cid)
                }
            })
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
            .feed().post(FeedPostRequest(labelerTokenManager.getToken()).also {
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
            .feed().post(FeedPostRequest(labelerTokenManager.getToken()).also {
                it.text =
                    "Valid legions are:\nadeptus-custodes\ndark-angels\nemperors-children\niron-warriors\nwhite-scars\nspace-wolves\nimperial-fists\nnight-lords\nblood-angels\niron-hands\nworld-eaters\nultramarines\ndeath-guard\nthousand-sons\nsons-of-horus\nword-bearers\nsalamanders\nraven-guard\nalpha-legion"
                it.reply = FeedPostReplyRef().also {
                    it.root = RepoStrongRef(uri, cid)
                    it.parent = RepoStrongRef(uri, cid)
                }
            })
    }

    private fun handleHelp(token: String, uri: String, cid: String) {
        BlueskyFactory
            .instance(Service.BSKY_SOCIAL.uri)
            .feed().post(FeedPostRequest(labelerTokenManager.getToken()).also {
                it.text =
                    "Commands:\nhelp: this menu\nlegions: list all valid legion tags for legion command\n" +
                            "legion <tag>: list battle brothers of the given legion tag\n" +
                            "attack: find an enemy astartes and attack them!\n" +
                            "status: a summary of the war effort! For Glory!"
                it.reply = FeedPostReplyRef().also {
                    it.root = RepoStrongRef(uri, cid)
                    it.parent = RepoStrongRef(uri, cid)
                }
            })
    }

    fun handleAttack(author: ActorDefsProfileView, uri: String, cid: String) {
        //fetch person
        val playerResult = transaction { Player.selectAll().where(Player.did.eq(author.did)).firstOrNull() }
        if (playerResult != null) {
            val label = playerResult[Player.tag]
            val attacks = Attacks()
            val result = attacks.handleAttack(author.did, label)
            BlueskyFactory
                .instance(Service.BSKY_SOCIAL.uri)
                .feed().post(FeedPostRequest(labelerTokenManager.getToken()).also {
                    it.text = result
                    it.reply = FeedPostReplyRef().also {
                        it.root = RepoStrongRef(uri, cid)
                        it.parent = RepoStrongRef(uri, cid)
                    }
                })
        } else {
            // Join the fight! Like and subscribe to this labeler to be assigned a legion!
        }
        //run attack
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