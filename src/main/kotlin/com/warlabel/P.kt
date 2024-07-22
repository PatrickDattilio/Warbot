package com.warlabel

import com.warlabel.Player.autoIncrement
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction


object Player : Table() {
    val id: Column<Int> = integer("id").autoIncrement()
    val name: Column<String> = varchar("name", length = 50)
//    val chapterId: Column<Int?> = (integer("chapter_id") references Chapter.id).nullable()
    val tag: Column<String> = varchar("tag", length = 50)
    val did: Column<String> = varchar("did", length = 50)
//    val isLoyalist: Column<Boolean> = bool("chapter_loyalist") references Chapter.loyalist
}

object Attack: Table(){
    val id: Column<Int> = integer("id").autoIncrement()
    val attackerDid:Column<String> = varchar("attacker_did", length = 50)
    val defenderDid:Column<String> = varchar("defender_did", length = 50)
    val label:Column<String> = varchar("tag", length = 50)
    val hit: Column<Int> = integer("hit")
    val toHit: Column<Int> = integer("to_hit")
    val wound: Column<Int> = integer("wound")
    val toWound: Column<Int> = integer("to_wound")
    val savingThrowRoll: Column<Int> = integer("saving_throw")
    val damage: Column<Int> = integer("damage")
}

object Chapter : Table() {
    val id: Column<Int> = integer("id").autoIncrement()
    val name: Column<String> = varchar("name", 50)
    val tag: Column<String> = varchar("tag", 50)
    val loyalist: Column<Boolean> = bool("loyalist")
}

fun main() {
    Database.connect("jdbc:h2:file:./warbot;AUTO_SERVER=TRUE", driver = "org.h2.Driver", user = "root", password = "")

    transaction {
        addLogger(StdOutSqlLogger)

        SchemaUtils.create( Attack)
//
//        Chapter.insert {
//            it[id] = 0
//            it[name] = "Sons of Horus"
//            it[tag] = "sons-of-horus"
//            it[loyalist] = false
//        }
//
//        Player.insert {
//            it[id] = 0
//            it[name] = "whomever.bsky.social"
//            it[tag] = "sons-of-horus"
////            it[chapterId] = 0
//            it[did] = "did:whaterver"
////            it[isLoyalist] = false
//        }

        val players = Player.selectAll()
        val what = Player.selectAll().execute(this)
        println("Players: ${Player.selectAll()}")
    }
}

