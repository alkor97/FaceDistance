package info.alkor.facedistance

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Vibrator
import android.util.Log
import java.util.concurrent.atomic.AtomicBoolean

class FaceDistanceManager(private val context: Context,
                          private val postFaceDistance: (Int) -> Unit,
                          private val postIsMeasuring: (Boolean) -> Unit) {

    private val tag = "fdm"

    private val faceDistanceAnalysisInProgress = AtomicBoolean(false)
    private val pictureProvider by lazy { FrontCameraPictureProvider() }
    private val faceDistanceAnalyzer by lazy { FaceDistanceAnalyzer(context) }

    fun startFaceDistanceAnalysis(distanceBetweenEyesMm: Float, faceToScreenDistanceThresholdMm: Float) {
        if (faceDistanceAnalysisInProgress.compareAndSet(false, true)) {
            postIsMeasuring(true)
            pictureProvider.takePicture(isPortrait()) { bytes, params -> onPictureTaken(bytes, params, distanceBetweenEyesMm, faceToScreenDistanceThresholdMm) }
        } else {
            Log.e(tag, "Face distance analysis already in progress!")
        }
    }

    private fun onPictureTaken(bytes: ByteArray, sensor: CameraParameters, distanceBetweenEyesMm: Float, faceToScreenDistanceThresholdMm: Float) {
        val faceDistanceMm = faceDistanceAnalyzer.computeFaceDistance(bytes, sensor, distanceBetweenEyesMm)
        if (faceDistanceMm != null) {
            Log.d(tag, "distance to face is $faceDistanceMm mm")
            postFaceDistance(faceDistanceMm.toInt())

            if (faceDistanceMm < faceToScreenDistanceThresholdMm) {
                Log.w(tag, "Your face is too close to screen! Move away!")
                vibrator.vibrate(longArrayOf(0, 250, 100, 250, 100, 250, 100, 250, 100, 250, 100), -1)
            }
        } else {
            Log.d(tag, "Unable to measure face-to-screen distance!")
        }
        faceDistanceAnalysisInProgress.set(false)
        postIsMeasuring(false)
    }

    fun destroy() {
        faceDistanceAnalyzer.destroy()
    }

    private fun isPortrait() = context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    private val vibrator by lazy { context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator }
}