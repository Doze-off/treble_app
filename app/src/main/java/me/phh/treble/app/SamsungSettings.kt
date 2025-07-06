package me.phh.treble.app

import android.app.Fragment
import android.content.Context
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import android.util.Log
import android.view.View
import android.widget.ListView

object SamsungSettings : Settings {
    val highBrightess = "key_samsung_high_brightness"
    val gloveMode = "key_samsung_glove_mode"
    val audioStereoMode = "key_samsung_audio_stereo"
    val wirelessChargingTransmit = "key_samsung_wireless_charging_transmit"
    val doubleTapToWake = "key_samsung_double_tap_to_wake"
    val extraSensors = "key_samsung_extra_sensors"
    val colorspace = "key_samsung_colorspace"
    val brokenFingerprint = "key_samsung_broken_fingerprint"
    val backlightMultiplier = "key_samsung_backlight_multiplier"
    val cameraIds = "key_samsung_camera_ids"
    val fodSingleClick = "key_samsung_fod_single_click"
    val flashStrength = "key_samsung_flash_strength"
    val disableBackMic = "key_samsung_disable_back_mic"

    override fun enabled(context: Context): Boolean {
        val isSamsung = Tools.vendorFpLow.startsWith("samsung/") ||
        Tools.vendorFpLow.startsWith("kddi/scv41_")
        Log.d("PHH", "SamsungSettings enabled() called, isSamsung = $isSamsung")
        return isSamsung
    }
}

class SamsungSettingsFragment : PreferenceFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.pref_samsung)

        if (SamsungSettings.enabled(context)) {
            Log.d("PHH", "Loading Samsung fragment ${SamsungSettings.enabled(context)}")

            findPreference(SamsungSettings.flashStrength)?.let {
                SettingsActivity.bindPreferenceSummaryToValue(it)
            }

            findPreference(SamsungSettings.backlightMultiplier)?.let {
                SettingsActivity.bindPreferenceSummaryToValue(it)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Apply consistent visual settings
        val listView = view.findViewById<ListView>(android.R.id.list)
        listView?.apply {
            divider = null
            dividerHeight = 0
            clipToPadding = true
            setPadding(32, paddingTop, 32, paddingBottom)
        }
    }
}
