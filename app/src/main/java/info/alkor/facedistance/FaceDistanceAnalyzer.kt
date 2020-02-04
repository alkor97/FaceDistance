package info.alkor.facedistance

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.face.FaceDetector
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class FaceDistanceAnalyzer(context: Context) {

    private val tag = "fda"
    private val detector = Holder(context)

    fun computeFaceDistance(bytes: ByteArray, sensor: CameraParameters, distanceBetweenEyesMm: Float): Float? {
        if (!detector().isOperational) {
            Log.e(tag, "Face detector is not yet operational!")
            return null
        }

        // convert JPEG picture to bitmap
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        Log.d(tag, "converted to bitmap ${bitmap.width} x ${bitmap.height}")
        Log.d(tag, "sensor=$sensor")

        val imageWidthPx = bitmap.width
        val imageHeightPx = bitmap.height

        val frame = Frame.Builder()
            .setRotation(if (sensor.portrait) Frame.ROTATION_270 else Frame.ROTATION_0)
            .setBitmap(bitmap)
            .build()

        val faces = detector().detect(frame)
        Log.d(tag, "${faces.size()} faces detected")

        var minDistance: Float = Int.MAX_VALUE.toFloat()
        var updated = false

        for (i in 0 until faces.size()) {
            val face = faces[i]
            if (face == null) {
                Log.e(tag, "face #$i is null")
                continue
            }

            val rightEye = face.landmarks[0].position
            val leftEye = face.landmarks[1].position

            val hDistPx = leftEye.x - rightEye.x
            val vDistPx = leftEye.y - rightEye.y
            val distancePx = Math.sqrt((hDistPx * hDistPx + vDistPx * vDistPx).toDouble()).toFloat()

            val sensorWidthMm =
                if (imageWidthPx > imageHeightPx) sensor.width() else sensor.height()
            val distanceMm =
                sensor.focalLength * distanceBetweenEyesMm * imageWidthPx / (distancePx * sensorWidthMm)

            Log.d(tag, "face #$i distance: $distanceMm mm")

            minDistance = Math.min(minDistance, distanceMm)
            updated = true
        }

        return if (updated) minDistance else null
    }

    private class Holder(private val context: Context) {

        private val tag = "fda"

        private var detector: FaceDetector? = null
        private val lock = ReentrantLock()

        operator fun invoke(): FaceDetector {
            lock.withLock {
                return detector ?: createFaceDetector()
            }
        }

        fun destroy() {
            lock.withLock {
                detector?.release()
                detector = null
            }
        }

        private fun createFaceDetector(): FaceDetector {
            try {
                val d = FaceDetector.Builder(context)
                    .setTrackingEnabled(false)
                    .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                    .build()
                detector = d
                return d
            } finally {
                Log.d(tag, "face detector instantiated")
            }
        }
    }

    fun destroy() {
        detector.destroy()
    }
}