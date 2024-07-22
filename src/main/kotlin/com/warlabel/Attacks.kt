package com.warlabel

import kotlinx.datetime.Clock
import work.socialhub.kbsky.BlueskyFactory
import work.socialhub.kbsky.api.entity.app.bsky.feed.FeedPostRequest
import work.socialhub.kbsky.api.entity.app.bsky.notification.NotificationListNotificationsRequest
import work.socialhub.kbsky.api.entity.app.bsky.notification.NotificationUpdateSeenRequest
import work.socialhub.kbsky.api.entity.com.atproto.server.ServerCreateSessionRequest
import work.socialhub.kbsky.domain.Service
import kotlin.random.Random

class Attacks {


    private fun d6(): Int {
        return Random.nextInt(1, 6)
    }

    private fun d3(): Int {
        return Random.nextInt(1, 3)
    }


    private fun hitRoll(roll: Int, toHit: Int): String {
        val difference = toHit - roll
        return when (difference) {
            -5 -> "Critical Hit."
            -4 -> "Brutal hit."
            -3 -> "Direct hit."
            -2 -> "Solid hit."
            -1 -> "You hit."
            0 -> "You just barely hit."
            1 -> "You just barely miss."
            2 -> "You miss wide."
            3 -> "Were you even aiming? Huge miss."
            4 -> "You almost end up hitting yourself. You miss."
            5 -> "Your bolter jams."
            else -> "Invalid attack"
        } + "\n"
    }

    fun isLoyalist(label: String): Boolean {
        return when (label) {
            "emperor-of-mankind",
            "adeptus-custodes",
            "dark-angels",
            "lion-el-jonson",
            "white-scars",
            "jaghatai-khan",
            "space-wolves",
            "leman-russ",
            "imperial-fists",
            "rogal-dorn",
            "blood-angels",
            "sanguinius",
            "iron-hands",
            "ferrus-manus",
            "ultramarines",
            "roboute-guilliman",
            "salamanders",
            "vulkan",
            "raven-guard",
            "corvus-corax",

                -> true

            "emperors-children",
            "fulgrim",
            "iron-warriors",
            "perturabo",
            "night-lords",
            "konrad-curze",
            "world-eaters",
            "angron",
            "death-guard",
            "mortarion",
            "thousand-sons",
            "magnus-the-red",
            "sons-of-horus",
            "horus",
            "word-bearers",
            "lorgar",
            "erberus",
            "alpha-legion",
            "alpharius-omegon",

                -> false

            else -> false

        }

    }

    fun handleAttack(attackerDid: String, label: String): String {
        val leader = if (isLoyalist(label)) {
            "Emperor"
        } else {
            "Warmaster"
        }
        val intro = "You see a traitor to the $leader attempting to sneak up on your squad.\n"
        val hit = d6()
        val toHit = d3()
        val attack = "You fire your bolter at the enemy!\n"
        val roll = "[$hit/$toHit] "
        val hitResult = hitRoll(hit, toHit)
        val woundRoll = d6()
        val toWound = d3()
        val wouldRollText = "[$hit/$toHit] "
        val wound = woundRoll(woundRoll, toWound)
        val savingThrowRoll = d6()
        val savingThrow = ""
        insertAttackToDb(attackerDid, label, hit, toHit, wound, toWound, savingThrowRoll)
        return intro + attack + roll + hitResult + wouldRollText + wound + savingThrow

    }

    private fun insertAttackToDb(
        attackerDid: String,
        label: String,
        hit: Int,
        toHit: Int,
        wound: String,
        toWound: Int,
        savingThrowRoll: Int
    ) {

    }

    private fun woundRoll(woundRoll: Int, toWound: Int): String {

        val difference = toWound - woundRoll
        return when (difference) {
            -5 -> "Critical wound!"
            -4 -> "Devastating wound!"
            -3 -> "Vicious wound!"
            -2 -> "Solid wound."
            -1 -> "You wound the enemy."
            0 -> "You just barely wound the enemy."
            1 -> "Your attack glances off their armor"
            2 -> "Your attack scratches their armor"
            3 -> "Were you even aiming? Huge miss."
            4 -> "You almost end up hitting yourself. You miss."
            5 -> "Your bolter jams."
            else -> "Invalid Wound"
        } + "\n"

    }

}
//
//fun main() {
//    val attacks = Attacks()
//    val response = BlueskyFactory
//        .instance(Service.BSKY_SOCIAL.uri)
//        .server()
//        .createSession(
//            ServerCreateSessionRequest().also {
//                it.identifier = "warlabel.bsky.social"
//                it.password = "TVR6vhu!rnj5tnc@gut"
//            }
//        )
//    while (true) {
//
//        val notifs = BlueskyFactory
//            .instance(Service.BSKY_SOCIAL.uri)
//            .notification()
//            .listNotifications(
//                NotificationListNotificationsRequest(response.data.accessJwt)
//            )
//
//        notifs.data.notifications.filter { !it.isRead && it.reason == "mention" }
//            .forEach { notif ->
//                val feedpost = notif.record.asFeedPost
//                if (feedpost?.text?.matches(Regex.fromLiteral("@warlabel.bsky.social attack")) == true) {
//                    //fetch person
//                    //run attack
//                    val result = attacks.handleAttack("test")
//                    //post result
//                    BlueskyFactory
//                        .instance(Service.BSKY_SOCIAL.uri)
//                        .feed().post(FeedPostRequest(response.data.accessJwt).also {
//                            it.text = result
//                            it.reply = feedpost.reply
//                        })
//                }
//                println("${notif.reason} ${notif.author.handle} ${feedpost?.text}")
//                BlueskyFactory
//                    .instance(Service.BSKY_SOCIAL.uri)
//                    .notification()
//                    .updateSeen(NotificationUpdateSeenRequest(response.data.accessJwt).also {
//                        it.seenAt = Clock.System.now().toString()
//                    })
//            }
//    }
//
//}