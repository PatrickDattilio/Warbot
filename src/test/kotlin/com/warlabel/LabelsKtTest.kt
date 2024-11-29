package com.warlabel

import org.junit.jupiter.api.Assertions.*
import kotlin.test.Test

class LabelsKtTest{


  @Test
  fun testOrk() {
   assertEquals(Label.Xenos.Orks, LabelUtils().getLabel("orks"))
  }
}