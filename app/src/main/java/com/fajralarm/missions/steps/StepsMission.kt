package com.fajralarm.missions.steps

import com.fajralarm.missions.MissionVerifier
import com.fajralarm.missions.MissionRules

class StepsMission(private val startCount: Int, private val target: Int) : MissionVerifier {
    override fun onProgress(value: Int) = MissionRules.isValidStepDelta(startCount, value, target)
    override fun reset() = Unit
}
