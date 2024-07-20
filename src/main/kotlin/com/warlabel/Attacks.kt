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



    fun d6(): Int {
        return Random.nextInt(1, 6)
    }

    fun d3(): Int {
        return Random.nextInt(1, 3)
    }


    fun hitRoll(roll: Int, toHit: Int):String {
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
        }
    }

    fun isLoyalist(label:String):Boolean{
        return when(label){
            "dark-angels",
            "white-scars",
            "space-wolves",
            "imperial-fists",

            -> true

            "emperors-children",
            "iron-warriors"
            ->false
            else -> false

        }

    }

    fun handleAttack(postDid: String):String {
        val leader = if (true) {
            "Emperor"
        } else {
            "Warmaster"
        }
        val intro = "You see a traitor to the $leader attempting to sneak up on your squad.\n"
        val hit = d6()
        val toHit = d3()
        val attack = "You fire your bolter at the enemy!\n"

        val hitResult = hitRoll(hit, toHit)
        val wound = ""
        val savingThrow = ""
        return intro+attack+hitResult+wound+savingThrow

    }

}
fun main(){
    val attacks = Attacks()
    val response = BlueskyFactory
        .instance(Service.BSKY_SOCIAL.uri)
        .server()
        .createSession(
            ServerCreateSessionRequest().also {
                it.identifier = "warlabel.bsky.social"
                it.password = "TVR6vhu!rnj5tnc@gut"
            }
        )
    while(true) {

        val notifs = BlueskyFactory
            .instance(Service.BSKY_SOCIAL.uri)
            .notification()
            .listNotifications(
                NotificationListNotificationsRequest(response.data.accessJwt)
            )

        notifs.data.notifications.filter { !it.isRead && it.reason == "mention" }
            .forEach { notif ->
                val feedpost = notif.record.asFeedPost
                if (feedpost?.text?.matches(Regex.fromLiteral("@warlabel.bsky.social attack")) == true) {
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
    }

}