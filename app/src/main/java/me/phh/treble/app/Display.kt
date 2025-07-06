package me.phh.treble.app

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.hardware.display.DisplayManager
import android.os.Parcel
import android.os.ServiceManager
import android.os.SystemProperties
import android.util.Log
import androidx.preference.PreferenceManager
import java.lang.ref.WeakReference

@SuppressLint("StaticFieldLeak")
object Display : EntryStartup {
    val surfaceFlinger = ServiceManager.getService("SurfaceFlinger")
    fun forceFps(v: Int, c: Boolean) {
        val data = Parcel.obtain()
        try {
            data.writeInterfaceToken("android.ui.ISurfaceComposer")
            data.writeInt(v)
            surfaceFlinger.transact(1035, data, null, 0)
            Log.d("PHH", "Set surface flinger forced fps/mode to supportedModes[$v]")
            if (c) {
                Log.d("PHH", "Resolution changed, attempting to restart SystemUI")
                var cmds = listOf(
                    arrayOf("/sbin/su", "-c", "/system/bin/killall com.android.systemui"),
                    arrayOf("/system/xbin/su", "-c", "/system/bin/killall com.android.systemui"),
                    arrayOf("/system/xbin/phh-su", "-c", "/system/bin/killall com.android.systemui"),
                    arrayOf("/sbin/su", "0", "/system/bin/killall com.android.systemui"),
                    arrayOf("/system/xbin/su", "0", "/system/bin/killall com.android.systemui"),
                    arrayOf("/system/xbin/phh-su", "0", "/system/bin/killall com.android.systemui")
                )
                for (cmd in cmds) {
                    try {
                        Runtime.getRuntime().exec(cmd).waitFor()
                        break
                    } catch (t: Throwable) {
                        Log.d("PHH", "Failed to exec \"" + cmd.joinToString(separator = " ") + "\", skipping")
                    }
                }
            }
        } catch (r: Exception) {
            Log.d("PHH", "Failed setting surface flinger forced fps/mode to supportedModes[$v]")
        } finally {
            data.recycle()
        }
    }

    fun enableHwcOverlay(v: Boolean) {
        val data = Parcel.obtain()
        try {
            data.writeInterfaceToken("android.ui.ISurfaceComposer")
            data.writeInt(if(v) 0 else 1)
            surfaceFlinger.transact(1008, data, null, 0)
            Log.d("PHH", "Set surface flinger hwc overlay to $v")
        } catch (r: Exception) {
            Log.d("PHH", "Failed setting surface flinger hwc overlay to $v")
        } finally {
            data.recycle()
        }
    }

    lateinit var ctxt: WeakReference<Context>

    val spListener = SharedPreferences.OnSharedPreferenceChangeListener { sp, key ->
        val c = ctxt.get() ?: return@OnSharedPreferenceChangeListener
        val displayManager = c.getSystemService(DisplayManager::class.java)

        when (key) {
            // FPS com chave MiscSettings.displayFps
            DisplaySettings.displayFps -> {
                val thisModeIndex = sp.getString(key, "-1")?.toIntOrNull() ?: -1
                val displayInfo = displayManager.displays[0]
                if (thisModeIndex < 0 || thisModeIndex >= displayInfo.supportedModes.size) {
                    Log.d("PHH", "Trying to set impossible supportedModes[$thisModeIndex]")
                } else {
                    Log.d("PHH", "Trying to set supportedModes[$thisModeIndex]")
                    val lastMode = displayInfo.mode
                    val lastModeIndex = displayInfo.supportedModes.indexOf(lastMode)
                    val thisMode = displayInfo.supportedModes[thisModeIndex]
                    Log.d("PHH", "\tlastMode = supportedModes[$lastModeIndex] = $lastMode")
                    Log.d("PHH", "\tthisMode = supportedModes[$thisModeIndex] = $thisMode")
                    forceFps(
                        thisModeIndex,
                        thisMode.physicalWidth != lastMode.physicalWidth || thisMode.physicalHeight != lastMode.physicalHeight
                    )
                }
            }

            DisplaySettings.dynamicFps -> {
                val value = sp.getBoolean(key, false)
                SystemProperties.set("persist.sys.phh.dynamic_fps", if (value) "true" else "false")
            }

            DisplaySettings.noHwcomposer -> {
                val value = sp.getBoolean(key, false)
                enableHwcOverlay(!value)
            }

            DisplaySettings.aod -> {
                val value = sp.getBoolean(key, false)
                SystemProperties.set("persist.sys.overlay.aod", if (value) "true" else "false")
                OverlayPicker.setOverlayEnabled("me.phh.treble.overlay.misc.aod_systemui", true)
            }

            DisplaySettings.disableSfGlBackpressure -> {
                val value = sp.getBoolean(key, false)
                SystemProperties.set("persist.sys.phh.enable_sf_gl_backpressure", if (value) "0" else "1")
            }

            DisplaySettings.disableSfHwcBackpressure -> {
                val value = sp.getBoolean(key, false)
                SystemProperties.set("persist.sys.phh.enable_sf_hwc_backpressure", if (value) "0" else "1")
            }

            DisplaySettings.sfBlurAlgorithm -> {
                val value = sp.getString(key, "kawase")
                SystemProperties.set("persist.sys.phh.sf.background_blur", value)
            }

            DisplaySettings.sfRenderEngineBackend -> {
                val value = sp.getString(key, "")
                SystemProperties.set("debug.renderengine.backend", value)
            }
        }
    }

    override fun startup(ctxt: Context) {
        Log.d("PHH", "Loading Display fragment")

        val sp = PreferenceManager.getDefaultSharedPreferences(ctxt)
        sp.registerOnSharedPreferenceChangeListener(spListener)

        this.ctxt = WeakReference(ctxt.applicationContext)

        spListener.onSharedPreferenceChanged(sp, DisplaySettings.displayFps)
        spListener.onSharedPreferenceChanged(sp, DisplaySettings.noHwcomposer)
    }
}
