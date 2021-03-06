package info.alkor.facedistance

import android.content.Context
import android.content.res.Configuration
import android.os.Vibrator
import android.util.Log
import java.util.concurrent.atomic.AtomicBoolean

class FaceDistanceManager(private val context: Context,
                          private val postFaceDistance: (FaceDistance) -> Unit,
                          private val postIsMeasuring: (Boolean) -> Unit,
                          private val statistics: Statistics
) {

    private val tag = "fdm"

    private val faceDistanceAnalysisInProgress = AtomicBoolean(false)
    private val pictureProvider by lazy { FrontCameraPictureProvider() }
    private val faceDistanceAnalyzer by lazy { FaceDistanceAnalyzer(context) }

    fun startFaceDistanceAnalysis(distanceBetweenEyesMm: Float, faceToScreenDistanceThresholdMm: Float) {
        if (tryMarkProcessingStarted()) {
            val pictureHandler = preparePictureHandler(
                distanceBetweenEyesMm,
                faceToScreenDistanceThresholdMm
            )
            if (!pictureProvider.takePicture(isPortrait(), pictureHandler)) {
                // taking picture failed for some reason (most probably camera is used by another application)
                markProcessingCompleted()
                postFaceDistance(Failure(Error.CAMERA_BUSY))
            }
        } else {
            Log.e(tag, "Face distance analysis already in progress!")
            postFaceDistance(Failure(Error.ALREADY_IN_PROGRESS))
        }
    }

    private fun preparePictureHandler(distanceBetweenEyesMm: Float, faceToScreenDistanceThresholdMm: Float): CameraPictureCallback =
        { bytes, params -> onPictureTaken(bytes, params, distanceBetweenEyesMm, faceToScreenDistanceThresholdMm) }

    private fun onPictureTaken(bytes: ByteArray, sensor: CameraParameters, distanceBetweenEyesMm: Float, faceToScreenDistanceThresholdMm: Float) {
        val faceDistanceMm = faceDistanceAnalyzer.computeFaceDistance(bytes, sensor, distanceBetweenEyesMm)
        if (faceDistanceMm != null) {
            Log.d(tag, "distance to face is $faceDistanceMm mm")
            postFaceDistance(Success(faceDistanceMm.toInt()))

            if (faceDistanceMm < faceToScreenDistanceThresholdMm) {
                statistics.measurementTaken(true)
                Log.w(tag, "Your face is too close to screen! Move away!")
                vibrator.vibrate(longArrayOf(0, 250, 100, 250, 100, 250, 100, 250, 100, 250, 100), -1)
            } else {
                statistics.measurementTaken(false)
            }
        } else {
            Log.d(tag, "Unable to measure face-to-screen distance!")
            postFaceDistance(Failure(Error.NO_FACES_DETECTED))
        }
        markProcessingCompleted()
    }

    fun destroy() {
        faceDistanceAnalyzer.destroy()
    }

    private fun tryMarkProcessingStarted(): Boolean {
        if (faceDistanceAnalysisInProgress.compareAndSet(false, true)) {
            postIsMeasuring(true)
            return true
        }
        return false
    }

    private fun markProcessingCompleted() {
        faceDistanceAnalysisInProgress.set(false)
        postIsMeasuring(false)
    }

    private fun isPortrait() = context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    private val vibrator by lazy { context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator }
}