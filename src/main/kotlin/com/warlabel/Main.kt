package com.warlabel

import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.Database


fun main() {
    val apiSecret= System.getenv("WLABELAPI")
    val warlabelSecret = System.getenv("WARLABEL")
    println("Secrets Set Successfully")
    val tokenManager = TokenManager("wlabelapi.bsky.social", apiSecret)
    val labelerTokenManger = TokenManager("warlabel.bsky.social", warlabelSecret)
    val notificationManager = NotificationManager(tokenManager, labelerTokenManger)
    val likeManager = LikeManager(tokenManager, labelerTokenManger)
    Database.connect("jdbc:h2:file:./warbot;AUTO_SERVER=TRUE", driver = "org.h2.Driver", user = "root", password = "")
    while (true) {
        try {
            notificationManager.fetchAndProcess()
            likeManager.fetchAndProcess(tokenManager.getToken())
        } catch (throwable: Throwable) {
            println(Clock.System.now().toString() + throwable.toString())
        }
        Thread.sleep(5000)
    }
}

