package com.warlabel

import org.jetbrains.exposed.sql.Database


fun main() {
    val tokenManager = TokenManager("wlabelapi.bsky.social","XQJ!hud-gup7kgn.mxe")
    val labelerTokenManger = TokenManager("warlabel.bsky.social", "TVR6vhu!rnj5tnc@gut")
    val notificationManager = NotificationManager()
    val likeManager = LikeManager(tokenManager, labelerTokenManger)
    Database.connect("jdbc:h2:file:./warbot", driver = "org.h2.Driver", user = "root", password = "")
    while (true) {
        try{
            notificationManager.fetchAndProcess(tokenManager.getToken())
            likeManager.fetchAndProcess(tokenManager.getToken())
        }catch (throwable:Throwable){
            println(throwable.toString())
        }
        Thread.sleep(5000)
    }
}

