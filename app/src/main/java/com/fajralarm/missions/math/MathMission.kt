package com.fajralarm.missions.math

import com.fajralarm.missions.MissionVerifier

class MathMission(target: Int) : MissionVerifier {
    val target = target.coerceAtLeast(1)
    var solved = 0
        private set
    fun submit(expected: Int, answer: String): Boolean {
        if (answer.trim().toIntOrNull() == expected) solved++
        return onProgress(solved)
    }
    override fun onProgress(value: Int) = value >= target
    override fun reset() { solved = 0 }
}
