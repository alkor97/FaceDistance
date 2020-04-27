package info.alkor.facedistance

import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.util.Log
import java.util.concurrent.atomic.AtomicBoolean

data class CameraParameters(
    val hViewAngle: Float,
    val vViewAngle: Float,
    val focalLength: Float,
    val portrait: Boolean
) {
    fun width() = sizeOf(hViewAngle, focalLength)
    fun height() = sizeOf(vViewAngle, focalLength)

    private fun sizeOf(viewAngle: Float, focalLength: Float): Float =
        (Math.tan(Math.toRadians(viewAngle / 2.toDouble())) * 2 * focalLength).toFloat()
}

typealias CameraPictureCallback = (ByteArray, CameraParameters) -> Unit

class FrontCameraPictureProvider {

    private val tag = "fcp"
    private val busy = AtomicBoolean(false)

    fun takePicture(portrait: Boolean, pictureCallback: CameraPictureCallback): Boolean {
        val camera = getFrontCamera()
        if (camera != null) {
            if (busy.compareAndSet(false, true)) {
                val params: Camera.Parameters = camera.parameters

                val size = selectPictureSize(params)
                params.setPictureSize(size.width, size.height)
                camera.parameters = params

                // store preview in invisible surface
                val surfaceTexture = SurfaceTexture(10)
                camera.setPreviewTexture(surfaceTexture)

                camera.startPreview()
                Log.d(tag, "preview started")

                camera.takePicture(null,null,
                    { bytes, c -> onJpegPictureTaken(pictureCallback, bytes, c, portrait, surfaceTexture) }
                )
                Log.d(tag, "taking picture initiated")

                return true
            } else {
                Log.e(tag, "Camera is busy now!")
                return false
            }
        } else {
            return false
        }
    }

    private fun onJpegPictureTaken(
        pictureCallback: CameraPictureCallback,
        bytes: ByteArray?,
        camera: Camera,
        portrait: Boolean,
        surface: SurfaceTexture
    ) {
        if (bytes != null) {
            pictureCallback(bytes, getParameters(camera, portrait))
        }
        camera.stopPreview()
        surface.release()
        camera.release()
        busy.set(false)
    }

    private fun getParameters(camera: Camera, portrait: Boolean): CameraParameters {
        val p = camera.parameters
        return CameraParameters(
            p.horizontalViewAngle,
            p.verticalViewAngle,
            p.focalLength,
            portrait
        )
    }

    private fun getFrontCamera(): Camera? {
        val info = Camera.CameraInfo()
        for (i in 0..Camera.getNumberOfCameras()) {
            Camera.getCameraInfo(i, info)
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                Log.d(tag, "Front-facing camera found.")
                try {
                    return Camera.open(i)
                } catch (e: RuntimeException) {
                    Log.e(tag, "Cannot open camera")
                    return null
                }
            }
        }
        Log.e(tag, "No front-facing camera found!")
        return null
    }

    private fun selectPictureSize(params: Camera.Parameters): Camera.Size {
        val targetPixelSize = 1920 * 1080
        val sorted = params.supportedPictureSizes.sortedBy { Math.abs(it.width*it.height - targetPixelSize) }
        return sorted[0]
    }
}