package com.fajralarm.missions.shake

import com.fajralarm.missions.MissionVerifier
import kotlin.math.sqrt

/** Accelerometer threshold with a short refractory window so one jolt is not counted several times. */
class ShakeMission(private val target: Int, private val threshold: Float = 2.8f) : MissionVerifier {
    var count: Int = 0
        private set
    private var lastShakeMs = 0L
    fun onAcceleration(x: Float, y: Float, z: Float, nowMs: Long): Boolean {
        val magnitude = kotlin.math.abs(sqrt(x*x + y*y + z*z) - 9.81f)
        if (magnitude > threshold && nowMs - lastShakeMs > 350) { count++; lastShakeMs = nowMs }
        return onProgress(count)
    }
    override fun onProgress(value: Int) = value >= target.coerceAtLeast(1)
    override fun reset() { count = 0; lastShakeMs = 0 }
}
