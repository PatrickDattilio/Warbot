package com.warlabel

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
 class LabelUtilsTest {

@Test
 fun getAvailableLabelString() {
  val split = LabelUtils().availableLabelString.substring(0,200)
  println(LabelUtils().availableLabelString.substring(0,200))
 println("200")
 println(LabelUtils().availableLabelString.substring(200))

 }
}