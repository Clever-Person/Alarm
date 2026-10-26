package com.fajralarm.missions.photo

import android.graphics.Bitmap
import com.fajralarm.missions.MissionRules
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions

/** Uses the on-device ML Kit base labeler; no image is uploaded or retained. */
class PhotoLabeler {
    private val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
    fun verify(bitmap: Bitmap, target: String, result: (Boolean, List<String>) -> Unit) {
        labeler.process(InputImage.fromBitmap(bitmap, 0))
            .addOnSuccessListener { labels ->
                val names = labels.filter { it.confidence >= 0.45f }.map { it.text }
                result(MissionRules.photoLabelsMatch(names, target), names)
            }
            .addOnFailureListener { result(false, emptyList()) }
    }
}
