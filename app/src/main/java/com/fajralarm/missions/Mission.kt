package com.fajralarm.missions

enum class MissionType { SHAKE, MATH, TEXT, STEPS, PHOTO, PUSH_UPS }

data class MissionSpec(val type: MissionType, val target: Int, val prompt: String = "")

/** Mission implementations report progress; the alarm is stopped only by a successful result. */
interface MissionVerifier {
    fun onProgress(value: Int): Boolean
    fun reset()
}

object MissionRules {
    fun shakeReached(count: Int, target: Int) = count >= target.coerceAtLeast(1)
    fun answerMatches(expected: String, actual: String) = expected.trim().equals(actual.trim(), ignoreCase = true)
    fun photoLabelsMatch(labels: Collection<String>, target: String): Boolean {
        val words = target.lowercase().split(Regex("[^a-z0-9]+" )).filter { it.length > 2 }
        return words.isNotEmpty() && labels.any { label -> words.any { it in label.lowercase() } }
    }
    fun isValidStepDelta(start: Int, current: Int, target: Int) = current - start >= target.coerceAtLeast(1)
}
