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

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import kotlinx.android.synthetic.main.dialog_inputsecuritykey.*
import kotlinx.android.synthetic.main.dialog_inputsecuritykey.view.*

import org.apache.commons.io.IOUtils






/**
 * Created by abara on 9/12/2017.
 */

class InputSecurityKeyDialogFragment : DialogFragment() {

    private lateinit var myView: View;

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        //getActivity().finish();
        exitApp()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        exitApp()
    }

    private fun exitApp() {
        activity.finishAndRemoveTask()
    }

    override fun onStart() {
        super.onStart()
        val message = dialog.findViewById<View>(android.R.id.message) as TextView
        message.gravity = Gravity.CENTER
        message.textSize = 22f
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the Builder class for convenient dialog construction
        val builder = AlertDialog.Builder(activity)

        // Get the layout inflater
        val inflater = activity.layoutInflater

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        val view =
            inflater.inflate(R.layout.dialog_inputsecuritykey, null) //ignore lint as null is required for dialogs
        myView = view;
        builder.setView(view)

        builder.setMessage(R.string.dialog_key_license_ver)
            .setPositiveButton(R.string.dialog_eula_agree) { dialog, id ->
                val hash = Utils.getKeyHash(Utils.getEncryptedIMEI(activity)) //getActivity should return MainActivity
                val enteredKey = myView.keyEditBox_keyDialog.text.toString().replace("-", "")
                if (hash.contains(enteredKey) || enteredKey == "skipCheck") { //edit to enable Key verification
                    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
                    sharedPreferences.edit()
                        .putString(activity.getString(R.string.generalPreferences_securityKeyKey), enteredKey).apply()
                }
            }
        val showEULAButton = view.findViewById(R.id.showEula_Button) as Button
        showEULAButton.setOnClickListener {
            val builder = AlertDialog.Builder(activity)
            val `is` = resources.openRawResource(R.raw.eula)
            var eula: String?
            //is.read()
            try {
                eula = IOUtils.toString(`is`)
            } catch (e: java.io.IOException) {
                eula = e.message
            }

            builder.setMessage(eula)
                .setPositiveButton(R.string.ok) { dialog, id -> dialog.dismiss() }
            builder.create().show()
        }
        val keyEditBox = view.findViewById(R.id.keyEditBox_keyDialog) as EditText
        keyEditBox.addTextChangedListener(object : TextWatcher {
            internal var removing = false
            internal var adding = false
            internal var ignore = false
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                removing = start > 0 && after <= 0
                adding = s.length > 1 && s.toString().replace("-", "").length % 5 == 0
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                if (ignore) {
                    return
                }

                val len = s.toString().replace("-", "").length //count without '-' character

                if (len > 0 && s[s.length - 1] != '-' && !removing) {
                    if (len % 5 == 0) {
                        s.append('-')
                    } else if (adding) { //adds '-' when user removed it and then typed another character
                        ignore = true

                        val c = s[s.length - 1]
                        s.replace(s.length - 1, s.length, "")
                        if (s[s.length - 1] != '-') {
                            s.append('-')
                        }
                        s.append(c)

                        ignore = false
                    }
                }
            }
        })

        //edit to enable Key verification
        keyEditBox.setText("skipCheck")
        keyEditBox.visibility = View.GONE
        view.findViewById<View>(R.id.hintLabel_keyDialog).visibility = View.GONE

        // Create the AlertDialog object and return it
        //Dialog dialog = builder.create();
        return builder.create()
    }
}
