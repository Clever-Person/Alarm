package com.fajralarm.missions.pushups

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.acos
import kotlin.math.abs
import kotlin.math.sqrt

/** Counts a push-up when shoulder/elbow/hip angles move down then return to an extended position. */
class PoseRepCounter {
    var reps = 0
        private set
    private var lowered = false
    fun onPose(pose: Pose): Int {
        val shoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        val elbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
        val wrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)
        val hip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
        if (shoulder == null || elbow == null || wrist == null || hip == null) return reps
        val ax = shoulder.position.x - elbow.position.x
        val ay = shoulder.position.y - elbow.position.y
        val bx = wrist.position.x - elbow.position.x
        val by = wrist.position.y - elbow.position.y
        val denominator = sqrt(ax * ax + ay * ay) * sqrt(bx * bx + by * by)
        if (denominator == 0f || shoulder.inFrameLikelihood < 0.5f || elbow.inFrameLikelihood < 0.5f || wrist.inFrameLikelihood < 0.5f || hip.inFrameLikelihood < 0.5f) return reps
        val angle = Math.toDegrees(acos(((ax * bx + ay * by) / denominator).coerceIn(-1f, 1f).toDouble())).toFloat()
        if (angle < 100f) lowered = true
        else if (lowered && angle > 155f) { reps++; lowered = false }
        return reps
    }
    fun reset() { reps = 0; lowered = false }
}
