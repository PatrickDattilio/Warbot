package com.warlabel

import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import work.socialhub.kbsky.BlueskyFactory
import work.socialhub.kbsky.api.entity.app.bsky.feed.FeedPostRequest
import work.socialhub.kbsky.api.entity.app.bsky.notification.NotificationListNotificationsRequest
import work.socialhub.kbsky.api.entity.app.bsky.notification.NotificationUpdateSeenRequest
import work.socialhub.kbsky.auth.BearerTokenAuthProvider
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
    private val labelRegex: Regex
    private val addRegex: Regex
    private val removeRegex: Regex
    private var lastCursor: String? = null
    private val labelUtils = LabelUtils()
    private val labelManager = LabelManager()

    init {
        attackRegex = Regex("@warlabel.bsky.social attack")
        helpRegex = Regex("@warlabel.bsky.social help")
        legionRegex = Regex("@warlabel.bsky.social legion (.*)")
        legionsRegex = Regex("@warlabel.bsky.social legions")
        statusRegex = Regex("@warlabel.bsky.social status")
        labelRegex = Regex("@warlabel.bsky.social label")
        addRegex = Regex("@warlabel.bsky.social add(.*)")
        removeRegex = Regex("@warlabel.bsky.social remove(.*)")
    }

    fun fetchAndProcess() {
        try {
            val notifs = BlueskyFactory
                .instance(Service.BSKY_SOCIAL.uri)
                .notification()
                .listNotifications(
                    NotificationListNotificationsRequest(BearerTokenAuthProvider(labelerTokenManager.getToken())).also {
                        it.cursor = lastCursor
                    }
                )
//            if (notifs.data.cursor != null) {
//                lastCursor = notifs.data.cursor
//            }
            notifs.data.notifications.filter { !it.isRead && it.reason == "mention" }
                .forEach { notif ->
                    val feedpost = notif.record.asFeedPost
                    val text = feedpost?.text?.lowercase()
                    println("${Clock.System.now()} ${notif.reason} ${notif.author.handle} ${feedpost?.text}")
                    if (text?.matches(helpRegex) == true) {
                        handleHelp(labelerTokenManager.getToken(), notif.uri, notif.cid)
                    } else if (text?.matches(attackRegex) == true) {
                        handleAttack(notif.author, notif.uri, notif.cid)
                    } else if (text?.matches(statusRegex) == true) {
                        handleStatus(notif.uri, notif.cid)
                    } else if (text?.matches(legionsRegex) == true) {
                        handleLegionEmpty(labelerTokenManager.getToken(), notif.uri, notif.cid)
                    } else if (text?.matches(legionRegex) == true) {
                        val legion = legionRegex.find(feedpost.text!!)?.groupValues?.last() ?: ""
                        if (legion.isBlank()) {
                            handleLegionEmpty(labelerTokenManager.getToken(), notif.uri, notif.cid)
                        } else {
                            handleLegion(labelerTokenManager.getToken(), notif.uri, notif.cid, legion)
                        }
                    } else if (text?.matches(labelRegex) == true) {
                        handleLabelEmpty(labelerTokenManager.getToken(), notif.uri, notif.cid, notif.author.did)

                    } else if (text?.matches(addRegex) == true) {
                        val label = addRegex.find(feedpost.text!!)?.groupValues?.last()?.trim() ?: ""
                        if (label.isBlank()) {
                            handleLabelEmpty(labelerTokenManager.getToken(), notif.uri, notif.cid, notif.author.did)
                        } else {
                            handleLabel(
                                labelerTokenManager.getToken(),
                                notif.uri,
                                notif.cid,
                                notif.author.did,
                                notif.author.handle,
                                label
                            )
                        }
                    } else if (text?.matches(removeRegex) == true) {
                        val label = labelRegex.find(feedpost.text!!)?.groupValues?.last()?.trim() ?: ""
                        if (label.isBlank()) {
                            handleLabelEmpty(labelerTokenManager.getToken(), notif.uri, notif.cid, notif.author.did)
                        } else {
                            handleRemoveLabel(
                                labelerTokenManager.getToken(),
                                notif.uri,
                                notif.cid,
                                notif.author.did,
                                notif.author.handle,
                                label
                            )
                        }
                    }

                    BlueskyFactory
                        .instance(Service.BSKY_SOCIAL.uri)
                        .notification()
                        .updateSeen(NotificationUpdateSeenRequest(BearerTokenAuthProvider(labelerTokenManager.getToken())).also {
                            it.seenAt = Clock.System.now().toString()
                        })
                }
        } catch (throwable: Throwable) {
            println(throwable.toString())
        }
    }

    private fun handleRemoveLabel(token: String, uri: String, cid: String, did: String, handle: String, label: String) {
        val validLabel = labelUtils.getLabel(label)
        if (validLabel != null) {
            val playerResult = transaction { Player.selectAll().where(Player.did.eq(did)).toList() }
            if (playerResult.isNotEmpty()) {
                playerResult.forEach {
                    val appliedLabel = it[Player.tag]
                    if (appliedLabel == label) {
                        labelManager.removeLabel(
                            did = did,
                            label = validLabel.label,
                            token = token
                        )
                        val id = it[Player.id]
                        transaction {
                            Player.deleteWhere { Player.id eq id }
                        }
                        println("${Clock.System.now()} $did removed $label")
                    }
                }
            }
        }
    }

    private fun handleLabel(token: String, uri: String, cid: String, did: String, handle: String, label: String) {
        val validLabel = labelUtils.getLabel(label)
        if (validLabel != null) {
            //We have a label a user wants
            //Check db for user
            // check all user labels
            val playerResult = transaction { Player.selectAll().where(Player.did.eq(did)).toList() }
            if (playerResult.isNotEmpty()) {
                var hasRequestedLabel = false
                playerResult.forEach {
                    val appliedLabel = it[Player.tag]
                    if (appliedLabel == label) {
                        hasRequestedLabel = true
                    }
                }
                if (!hasRequestedLabel) {
                    labelManager.applyLabel(
                        did = did,
                        label = validLabel.label,
                        token = token
                    )
                    transaction {
                        Player.insert {
                            it[Player.did] = did
                            it[name] = handle
                            it[tag] = label
                        }
                    }
                } else {
                    println("${Clock.System.now()} $did requested $label but is already labeled")
                }
            } else {
                labelManager.applyLabel(
                    did = did,
                    label = validLabel.label,
                    token = token
                )
                transaction {
                    Player.insert {
                        it[Player.did] = did
                        it[name] = handle
                        it[tag] = label
                    }
                }
                println("${Clock.System.now()} $did labeled $label")
            }
        } else {
            handleLabelEmpty(token, uri, cid, did)
        }
    }


    private fun handleLabelEmpty(token: String, uri: String, cid: String, did: String) {
        val validLabels = "Valid Labels:\n"
        var feedPostResponse = BlueskyFactory
            .instance(Service.BSKY_SOCIAL.uri)
            .feed().post(FeedPostRequest(BearerTokenAuthProvider(labelerTokenManager.getToken())).also {
                it.text = validLabels + labelUtils.availableLabels[0]
                it.reply = FeedPostReplyRef().also {
                    it.root = RepoStrongRef(uri, cid)
                    it.parent = RepoStrongRef(uri, cid)
                }
            })
        for (i in 1..labelUtils.availableLabels.size - 1) {
            val group = labelUtils.availableLabels[i]

            val newCid = feedPostResponse.data.cid
            val newUri = feedPostResponse.data.uri
            feedPostResponse = BlueskyFactory
                .instance(Service.BSKY_SOCIAL.uri)
                .feed().post(FeedPostRequest(BearerTokenAuthProvider(labelerTokenManager.getToken())).also {
                    it.text = group
                    it.reply = FeedPostReplyRef().also {
                        it.root = RepoStrongRef(newUri, newCid)
                        it.parent = RepoStrongRef(newUri, newCid)
                    }
                })
        }
    }

    private fun handleStatus(uri: String, cid: String) {


        val imperium = transaction {
            Attack.selectAll().where(Attack.isLoyalist.eq(true).and(Attack.damage.greaterEq(1))).toList()
        }
        val warmaster = transaction {
            Attack.selectAll().where(Attack.isLoyalist.eq(false).and(Attack.damage.greaterEq(1))).toList()
        }
        val response =
            "Fallen Astartes of the Imperium: ${warmaster.size}\nFallen Astartes of the Warmaster: ${imperium.size}"

        BlueskyFactory
            .instance(Service.BSKY_SOCIAL.uri)
            .feed().post(FeedPostRequest(BearerTokenAuthProvider(labelerTokenManager.getToken())).also {
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
            .feed().post(FeedPostRequest(BearerTokenAuthProvider(labelerTokenManager.getToken())).also {
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
            .feed().post(FeedPostRequest(BearerTokenAuthProvider(labelerTokenManager.getToken())).also {
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
            .feed().post(FeedPostRequest(BearerTokenAuthProvider(labelerTokenManager.getToken())).also {
                it.text =
                    "Commands:\n" +
                            "add label-name: adds label-name label to your account. label-name must be one of the valid labels \n" +
                            "label: list all valid labels for add/remove command\n" +
                            "remove labelname: removes labelname label from your account. labelname must be one of the valid labels\n" +
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
                .feed().post(FeedPostRequest(BearerTokenAuthProvider(labelerTokenManager.getToken())).also {
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