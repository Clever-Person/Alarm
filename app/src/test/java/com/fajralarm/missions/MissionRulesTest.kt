package com.fajralarm.missions

import org.junit.Assert.*
import org.junit.Test

class MissionRulesTest {
    @Test fun shakeRequiresTarget() { assertFalse(MissionRules.shakeReached(9, 10)); assertTrue(MissionRules.shakeReached(10, 10)) }
    @Test fun textMustMatchIgnoringOuterWhitespaceAndCase() { assertTrue(MissionRules.answerMatches("Rise with purpose", "  rise with purpose ")); assertFalse(MissionRules.answerMatches("Rise with purpose", "Rise purpose")) }
    @Test fun stepProgressUsesDelta() { assertFalse(MissionRules.isValidStepDelta(100, 109, 10)); assertTrue(MissionRules.isValidStepDelta(100, 110, 10)) }
    @Test fun photoLabelsMustContainTargetWord() { assertTrue(MissionRules.photoLabelsMatch(listOf("Blue sky", "Cloud"), "sky")); assertFalse(MissionRules.photoLabelsMatch(listOf("Bedroom"), "sky")) }
}
