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


import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.provider.DocumentFile
import android.util.Log
import android.widget.Toast

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

/**
 * Created by abara on 5/9/2017.
 * settings activity class
 */

class SettingsActivity : Activity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        if (MainActivity.activeTheme === MainActivity.Themes.Light) {
            setTheme(R.style.AppThemeLight_NoActionBar)
        }
        super.onCreate(savedInstanceState)
        // Display the fragment as the main content.
        fragmentManager.beginTransaction()
            .replace(android.R.id.content, SettingsFragment(), getString(R.string.preferencesFragmentTag))
            .commit()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        var result = true
        if (requestCode == ButtonPreference.PICK_DIR_CREATE_BACKUP_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                val uri = resultData.data
                var df = DocumentFile.fromTreeUri(this, uri!!)
                if (df == null && resultData.hasExtra("file")) {
                    df = DocumentFile.fromFile(resultData.getSerializableExtra("file") as File)
                }
                if (df != null) {
                    for (file in filesDir.listFiles()) {
                        try {
                            if (file.isFile) {
                                var fileName = file.name
                                val idx = fileName.lastIndexOf(".xml")
                                if (idx != -1) {
                                    fileName = fileName.substring(0, idx) //strip extension
                                }
                                val newFile = df.createFile("text/xml", fileName)
                                if (newFile != null) {
                                    Utils.copyFile(FileInputStream(file), contentResolver.openOutputStream(newFile.uri))
                                }
                            }
                        } catch (e: IOException) {
                            Log.w(TAG, e.toString())
                            Toast.makeText(this, R.string.backup_fail.toString() + file.name, Toast.LENGTH_SHORT).show()
                            result = false
                        }

                    }
                } else {
                    Toast.makeText(this, R.string.error_occurred, Toast.LENGTH_SHORT).show()
                    result = false
                }
                if (result) {
                    Toast.makeText(this, R.string.backup_success, Toast.LENGTH_SHORT).show()
                }
            }
        }
        if (requestCode == ButtonPreference.PICK_DIR_IMPORT_BACKUP_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                val uri = resultData.data
                var df = DocumentFile.fromTreeUri(this, uri!!) //copy from
                if (df == null && resultData.hasExtra("file")) {
                    df = DocumentFile.fromFile(resultData.getSerializableExtra("file") as File)
                }
                if (df != null) {
                    for (file in df.listFiles()) {
                        try {
                            val newFile = File(filesDir, file.name!!)
                            Utils.copyFile(contentResolver.openInputStream(file.uri)!!, FileOutputStream(newFile))
                        } catch (e: IOException) {
                            Log.w(TAG, e.toString())
                            Toast.makeText(this, R.string.backup_fail.toString() + file.name!!, Toast.LENGTH_SHORT)
                                .show()
                            result = false
                        }

                    }
                    Utils.restartApplication(this)
                } else {
                    Toast.makeText(this, R.string.error_occurred, Toast.LENGTH_SHORT).show()
                    result = false
                }
                if (result) {
                    Toast.makeText(this, R.string.import_backup_success, Toast.LENGTH_SHORT).show()
                }
            }
        }
        //super.onActivityResult(requestCode, resultCode, resultData);
    }

    companion object {
        var KEY_SAVE_LOGS: String = ""
        var KEY_THEME: String = ""

        internal val TAG = "SA_TAG"


        fun initializeResources(activity: MainActivity) {
            KEY_THEME = activity.getString(R.string.generalPreferences_themeKey)
            KEY_SAVE_LOGS = activity.getString(R.string.generalPreferences_createLogsKey)

            //activity.MsgHeader =  PreferenceManager.getDefaultSharedPreferences(activity).getString(SettingsActivity.KEY_MSG_HEADER, SettingsActivity.MSG_HEADER_DEF);
            //activity.useDefaultApp =  PreferenceManager.getDefaultSharedPreferences(activity).getBoolean(SettingsActivity.KEY_DEFAULTAPP, false);
        }
    }
}
