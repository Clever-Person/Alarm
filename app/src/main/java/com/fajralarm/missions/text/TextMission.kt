package com.fajralarm.missions.text

import com.fajralarm.missions.MissionVerifier
import com.fajralarm.missions.MissionRules

class TextMission(private val sentence: String) : MissionVerifier {
    override fun onProgress(value: Int) = value > 0
    fun verify(input: String) = MissionRules.answerMatches(sentence, input)
    override fun reset() = Unit
}
