package info.alkor.facedistance

import android.app.IntentService
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.util.Log

class FaceDistanceService : IntentService("Face Distance Service") {

    private val tag = "fds"

    override fun onHandleIntent(intent: Intent?) {
        if (isScreenNotLocked()) {
            appContext().startFaceDistanceAnalysis()
        } else {
            Log.d(tag, "Screen is locked, skipping")
        }
    }

    private fun isScreenNotLocked() = !(getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager).isDeviceLocked
    private fun appContext() = application as MyApplication
}