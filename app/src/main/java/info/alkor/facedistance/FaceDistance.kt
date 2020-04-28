package info.alkor.facedistance

enum class Error(val key: String) {
    ALREADY_IN_PROGRESS("inProgress"),
    CAMERA_BUSY("cameraBusy"),
    NO_FACES_DETECTED("noFacesDetected");

    fun keyCount() = "${key}Count"
}

interface FaceDistance
data class Failure(val error: Error) : FaceDistance
data class Success(val value: Int) : FaceDistance
