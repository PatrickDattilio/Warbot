package com.warlabel

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction


object Player : Table() {
    val id: Column<Int> = integer("id").autoIncrement()
    val name: Column<String> = varchar("name", length = 50)
    val chapterId: Column<Int?> = (integer("chapter_id") references Chapter.id).nullable()
    val chapterTag: Column<String> = varchar("chapter_tag", length = 50) references Chapter.tag
    val did: Column<String> = varchar("did", length = 50)
    val isLoyalist: Column<Boolean> = bool("chapter_loyalist") references Chapter.loyalist
}

object Chapter : Table() {
    val id: Column<Int> = integer("id").autoIncrement()
    val name: Column<String> = varchar("name", 50)
    val tag: Column<String> = varchar("tag", 50)
    val loyalist: Column<Boolean> = bool("loyalist")
}

fun main() {
    Database.connect("jdbc:h2:mem:test", driver = "org.h2.Driver", user = "root", password = "")

    transaction {
        addLogger(StdOutSqlLogger)

        SchemaUtils.create(Chapter, Player)

        Chapter.insert {
            it[id] = 0
            it[name] = "Sons of Horus"
            it[tag] = "sons-of-horus"
            it[loyalist] = false
        }

        Player.insert {
            it[id] = 0
            it[name] = "whomever.bsky.social"
            it[chapterTag] = "sons-of-horus"
            it[chapterId] = 0
            it[did] = "did:whaterver"
            it[isLoyalist] = false
        }

        val players = Player.selectAll()
        val what = Player.selectAll().execute(this)
        println("Players: ${Player.selectAll()}")
    }
}

