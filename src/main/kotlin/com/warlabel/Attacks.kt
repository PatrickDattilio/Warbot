package com.warlabel

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.random.Random

class Attacks{

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
        val defenderDid = "enemy"
        val isLoyalist = isLoyalist(label)
        val leader = if (isLoyalist) {
            "Emperor"
        } else {
            "Warmaster"
        }
        val intro = getIntro(leader)
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
        val savingThrow = savingThrowRoll(savingThrowRoll)
        val damage = if(savingThrowRoll==6) 0 else 1
        insertAttackToDb(attackerDid, defenderDid,label, hit, toHit, woundRoll, toWound, savingThrowRoll, damage, isLoyalist)
        return intro + attack + roll + hitResult + wouldRollText + wound + savingThrow

    }

    private fun getIntro(leader:String): String {
         val intros:List<String> = listOf(
            "You see a traitor to the $leader attempting to sneak up on your squad.\n",
             "You spy a lone scout out on patrol. You begin firing screaming 'For the $leader!\n"
        )
        return intros[Random.nextInt(0,intros.lastIndex)]

    }

    private fun savingThrowRoll(savingThrowRoll: Int): String {
        return if(savingThrowRoll == 6){
            "By some miracle the blow delivers no damage!\n"
        }else{
            "You apply one wound.\n"
        }
    }

    private fun insertAttackToDb(
        attackerDid: String,
        defenderDid: String,
        label: String,
        hit: Int,
        toHit: Int,
        wound: Int,
        toWound: Int,
        savingThrowRoll: Int,
        damage: Int,
        isLoyalist: Boolean
    ) {
        transaction {
            Attack.insert {
                it[Attack.attackerDid] = attackerDid
                it[Attack.defenderDid] = defenderDid
                it[Attack.label] = label
                it[Attack.hit] = hit
                it[Attack.toHit] = toHit
                it[Attack.wound] = wound
                it[Attack.toWound] = toWound
                it[Attack.savingThrowRoll] = savingThrowRoll
                it[Attack.damage] = damage
                it[Attack.isLoyalist] = isLoyalist
            }
        }

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