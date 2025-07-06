package me.phh.treble.app

import android.app.Fragment
import android.content.Context
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import android.util.Log
import android.view.View
import android.widget.ListView

object OppoSettings : Settings {
    val dt2w = "key_oppo_double_tap_to_wake"
    val gamingMode = "key_oppo_ts_game_mode"
    val usbOtg = "key_oppo_usb_otg"
    val dcDiming = "key_oppo_dc_diming"

    override fun enabled(context: Context): Boolean {
        val isOppo = Tools.deviceId.startsWith("RMX")
        Log.d("PHH", "OppoSettings enabled() called, isOppo = $isOppo")
        return isOppo
    }
}

class OppoSettingsFragment : PreferenceFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.pref_oppo)

        if (OppoSettings.enabled(context)) {
            Log.d("PHH", "Loading Oppo fragment ${OppoSettings.enabled(context)}")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Apply same visual settings as AudioEffectsFragment
        val listView = view.findViewById<ListView>(android.R.id.list)
        listView?.apply {
            divider = null
            dividerHeight = 0
            clipToPadding = true
            setPadding(32, paddingTop, 32, paddingBottom)
        }
    }
}
