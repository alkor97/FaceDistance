package info.alkor.facedistance

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

abstract class SimpleStorage(private val context: Context, private val storageName: String) {

    private val tag = "sst"

    private val listeners = HashMap<String, (Any) -> Unit>()
    private val readers = HashMap<String, () -> Any>()

    private val valueChangedListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        val supplier = readers[key]
        if (supplier != null) {
            val consumer = listeners[key]
            if (consumer != null) {
                val value = supplier()
                consumer(value)
            }
        }
    }

    protected fun writeInt(key: String, value: Int) = write(key, value, (SimpleStorage)::doWriteInt)
    protected fun readInt(key: String, defaultValue: Int) = sharedPrefs().getInt(key, defaultValue)

    protected fun writeLong(key: String, value: Long) = write(key, value, (SimpleStorage)::doWriteLong)
    protected fun readLong(key: String, defaultValue: Long) = sharedPrefs().getLong(key, defaultValue)

    protected fun writeString(key: String, value: String) = write(key, value, (SimpleStorage)::doWriteString)
    protected fun readString(key: String, defaultValue: String) = sharedPrefs().getString(key, defaultValue)!!

    protected fun writeBoolean(key: String, value: Boolean) = write(key, value, (SimpleStorage)::doWriteBoolean)
    protected fun readBoolean(key: String, defaultValue: Boolean) = sharedPrefs().getBoolean(key, defaultValue)

    protected fun <E> registerListener(key: String, listener: (E) -> Unit, reader: () -> E) {
        if (listeners.isEmpty()) {
            sharedPrefs().registerOnSharedPreferenceChangeListener(valueChangedListener)
        }
        listeners[key] = listener as (Any) -> Unit
        readers[key] = reader as () -> Any
    }

    protected fun unregisterListener(key: String) {
        listeners.remove(key)
        readers.remove(key)
        if (listeners.isEmpty()) {
            sharedPrefs().unregisterOnSharedPreferenceChangeListener(valueChangedListener)
        }
    }

    private fun <E> write(key: String, value: E, writer: (SharedPreferences.Editor, String, E) -> Unit) = with(sharedPrefs().edit()) {
        writer(this, key, value)
        commit()
        Log.d(tag, "$key set to $value")
    }

    private fun sharedPrefs() = context.getSharedPreferences(
        context.packageName + "_" + storageName,
        Context.MODE_PRIVATE
    )

    companion object {
        private fun doWriteInt(spe: SharedPreferences.Editor, key: String, value: Int) {
            spe.putInt(key, value)
        }

        private fun doWriteLong(spe: SharedPreferences.Editor, key: String, value: Long) {
            spe.putLong(key, value)
        }

        private fun doWriteString(spe: SharedPreferences.Editor, key: String, value: String) {
            spe.putString(key, value)
        }

        private fun doWriteBoolean(spe: SharedPreferences.Editor, key: String, value: Boolean) {
            spe.putBoolean(key, value)
        }
    }

    fun logContent() {
        val sb = StringBuilder("Content of $storageName:\n")
        with(sharedPrefs()) {
            all.entries.stream().forEach {
                sb.append(it.key).append("=").append(it.value).append('\n')
            }
        }
        Log.d(tag, sb.toString())
    }
}