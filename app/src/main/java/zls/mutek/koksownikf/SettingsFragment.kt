/* 
 * Copyright (C) 2017 Alan Bara, alanbarasoft@gmail.com
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package zls.mutek.koksownikf

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceFragment
import android.preference.PreferenceManager
import android.preference.SwitchPreference
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * Created by abara on 5/9/2017.
 * settings fragment class
 */

class SettingsFragment : PreferenceFragment() {

    internal var mSettingsListener: SharedPreferences.OnSharedPreferenceChangeListener = onSharedPreferenceChange

    /**********************************
     *
     * On Preference Changed
     *
     */

    private val onSharedPreferenceChange: SharedPreferences.OnSharedPreferenceChangeListener
        get() = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == SettingsActivity.KEY_THEME) {
                if (isAdded) {
                    val switchPreference =
                        findPreference(getString(R.string.generalPreferences_themeKey)) as SwitchPreference
                    if (switchPreference != null) {
                        if (sharedPreferences.getBoolean(key, true)) {
                            switchPreference.setTitle(R.string.generalPreferences_themeDark)
                        } else {
                            switchPreference.setTitle(R.string.generalPreferences_themeLight)
                        }
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences)

        PreferenceManager.getDefaultSharedPreferences(activity)
            .registerOnSharedPreferenceChangeListener(mSettingsListener)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mSettingsListener.onSharedPreferenceChanged(
            PreferenceManager.getDefaultSharedPreferences(activity),
            SettingsActivity.KEY_THEME
        ) //set switch text
    }
}