package info.alkor.facedistance

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import java.text.DateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

class MainActivity : AppCompatActivity() {

    companion object {
        private const val RC_CAMERA_PERMISSIONS = 37
    }

    private val listenerRegisteredSwitch by lazy { findViewById<Switch>(R.id.switch_register_listener) }
    private val alarmEnabledSwitch by lazy { findViewById<Switch>(R.id.switch_enable_alarm) }
    private val faceDistanceText by lazy { findViewById<TextView>(R.id.text_face_distance) }
    private val measuringProgress by lazy { findViewById<ProgressBar>(R.id.progress_measuring) }
    private val distanceBetweenEyesEdit by lazy { findViewById<EditText>(R.id.edit_distance_between_eyes) }
    private val distanceThresholdEdit by lazy { findViewById<EditText>(R.id.edit_face_distance_threshold) }
    private val measurementPeriodEdit by lazy { findViewById<EditText>(R.id.edit_measurement_period) }
    private val lastCheckedEdit by lazy { findViewById<EditText>(R.id.edit_last_checked) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!isPermissionGranted()) {
            requestCameraPermissions()
        }

        installObserver(appContext().alarmEnabled) { alarmEnabledSwitch.isChecked = it }
        alarmEnabledSwitch.setOnCheckedChangeListener { _, isChecked -> enableAlarm(isChecked) }

        installObserver(appContext().userObserverEnabled) { listenerRegisteredSwitch.isChecked = it }
        installObserver(appContext().faceDistance) {
            faceDistanceText.text = resources.getString(R.string.face_distance_is, it)
            lastCheckedEdit.setText(DateFormat.getTimeInstance().format(Date()))
        }
        installObserver(appContext().isMeasuring) { measuringProgress.visibility = if (it) View.VISIBLE else View.INVISIBLE }
        appContext().isMeasuring.postValue(false)

        setupOnIntChanged(distanceBetweenEyesEdit) { appContext().distanceBetweenEyesMm = it }
        initIntEditor(distanceBetweenEyesEdit) { appContext().distanceBetweenEyesMm }

        setupOnIntChanged(distanceThresholdEdit) { appContext().distanceThresholdMm = it }
        initIntEditor(distanceThresholdEdit) { appContext().distanceThresholdMm }

        setupOnIntChanged(measurementPeriodEdit) { appContext().measuringPeriod = Timeout(it.toLong(), TimeUnit.SECONDS) }
        initIntEditor(measurementPeriodEdit) { appContext().measuringPeriod.toSeconds().toInt() }
    }

    private fun registerUserStatusReceiver(enable: Boolean) {
        if (enable) {
            appContext().registerUserStatusReceiver()
        } else {
            appContext().unregisterUserStatusReceiver()
        }
    }

    private fun enableAlarm(enable: Boolean) {
        if (enable) {
            appContext().scheduleAlarm()
            registerUserStatusReceiver(true)
        } else {
            appContext().cancelAlarm()
            registerUserStatusReceiver(false)
        }
    }
    private fun <E> installObserver(stream: LiveData<E>, handler: (E) -> Unit) = stream.observe(this, Observer { handler(it) })

    private fun setupOnIntChanged(editor: EditText, consume: (Int) -> Unit) = editor.addTextChangedListener(IntWatcher( consume ))
    private fun initIntEditor(editor: EditText, provide: () -> Int) = editor.setText(provide().toString())

    private class IntWatcher(private val handler: (Int) -> Unit) : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val value = parseInteger(s.toString())
            if (value != null) {
                handler(value)
            }
        }

        private fun parseInteger(text: String): Int? = try {
            Integer.parseInt(text)
        } catch (e: NumberFormatException) {
            null
        }
    }

    private fun isPermissionGranted() =
        PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)

    private fun requestCameraPermissions() {
        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), RC_CAMERA_PERMISSIONS)
            return
        }

        val thisActivity = this
        val listener = View.OnClickListener {
            ActivityCompat.requestPermissions(thisActivity, arrayOf(Manifest.permission.CAMERA), RC_CAMERA_PERMISSIONS)
        }

        Snackbar.make(findViewById(android.R.id.content), R.string.permission_camera_rationale, Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.ok, listener).show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        if (requestCode != RC_CAMERA_PERMISSIONS) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            return
        }
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            return
        }

        AlertDialog.Builder(this).setTitle("Face Tracker sample")
            .setMessage(R.string.no_camera_permission)
            .setPositiveButton(R.string.ok) { _, _ -> finish() }
            .show()
    }

    private fun appContext() = application as MyApplication
}
