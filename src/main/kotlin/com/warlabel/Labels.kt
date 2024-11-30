package com.warlabel

sealed class Label(val label: String) {

    //Xenos
    sealed class Xenos(label: String) : Label(label) {
        data object Orks : Xenos("orks")
        data object Tau : Xenos("tau")
        data object Drukhari : Xenos("drukhari")
        data object Aeldari : Xenos("aeldari")
        data object Necron: Xenos("necron")
        data object Tyranid: Xenos("tyranid")
        data object Kroot: Xenos("kroot")
        data object Voltann: Xenos("voltann")
    }

    //Imperium
    sealed class Imperium(label: String) : Label(label) {
        data object AdeptusCustodes : Imperium("adeptus-custodes")
        data object AdeptaSororitas : Imperium("adepta-sororitas")
        data object AlphaLegion : Imperium("alpha-legion")
        data object BloodAngels : Imperium("blood-angels")
        data object DarkAngels : Imperium("dark-angels")
        data object DeathGuard : Imperium("death-guard")
        data object EmperorsChildren : Imperium("emperors-children")
        data object ImperialFists : Imperium("imperial-fists")
        data object IronHands : Imperium("iron-hands")
        data object IronWarriors : Imperium("iron-warriors")
        data object NightLords : Imperium("night-lords")
        data object RavenGuard : Imperium("raven-guard")
        data object Salamanders : Imperium("salamanders")
        data object SonsOfHorus : Imperium("sons-of-horus")
        data object SpaceWolves : Imperium("space-wolves")
        data object ThousandSons : Imperium("thousand-sons")
        data object Ultramarines : Imperium("ultramarines")
        data object WhiteScars : Imperium("white-scars")
        data object WordBearers : Imperium("word-bearers")
        data object WorldEaters : Imperium("world-eaters")
    }

    sealed class Hobby(label: String): Label(label){
        data object MiniPainter: Hobby("mini-painter")
        data object MiniPrinter: Hobby("mini-printer")
        data object MiniSculptor: Hobby("mini-sculptor")
    }

    sealed class Game(label: String): Label(label){
        data object Warhammer40k: Game("wh40k")
        data object WarhammerAgeOfSigmar: Game("sigmar")
        data object WarhammerOldWorld: Game("old-world")
        data object WarhammerKillTeam: Game("kill-team")
        data object WarhammerNecromunda: Game("necromunda")
    }


}


