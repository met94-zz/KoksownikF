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

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Environment
import android.preference.PreferenceManager
import android.support.annotation.StringRes
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.telephony.TelephonyManager
import android.widget.Toast

import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.Arrays

/**
 * Created by abara on 8/4/2017.
 */

object Utils {
    internal val NEGATIVE_COLORFILTER = floatArrayOf(
        -1.0f, 0f, 0f, 0f, 255f, // red
        0f, -1.0f, 0f, 0f, 255f, // green
        0f, 0f, -1.0f, 0f, 255f, // blue
        0f, 0f, 0f, 1.0f, 0f  // alpha
    )
    val REQUEST_CODE_ASK_PERMISSIONS = 123

    /* Checks if external storage is available for read and write */
    val isExternalStorageWritable: Boolean
        get() {
            val state = Environment.getExternalStorageState()
            return if (Environment.MEDIA_MOUNTED == state) {
                true
            } else false
        }
    fun isNameStartChar(c: Char): Boolean {
        return (c == ':' || c in 'A'..'Z' || c == '_' || c in 'a'..'z' || c.toInt() in 0xC0..0xD6
                || c.toInt() in 0xD8..0xF6 || c.toInt() in 0xF8..0x2FF || c.toInt() in 0x370..0x37D
                || c.toInt() in 0x37F..0x1FFF || c.toInt() in 0x200C..0x200D
                || c.toInt() in 0x2070..0x218F || c.toInt() in 0x2C00..0x2FEF
                || c.toInt() in 0x3001..0xD7FF || c.toInt() in 0xF900..0xFDCF
                || c.toInt() in 0xFDF0..0xFFFD || c.toInt() in 0x10000..0xEFFFF)
    }

    fun isNameChar(c: Char): Boolean {
        return (isNameStartChar(c) || c == '-' || c == '.' || c in '0'..'9' || c.toInt() == 0xB7
                || c.toInt() in 0x0300..0x036F || c.toInt() in 0x203F..0x2040)
    }

    /**
     * Check to see if a string is a valid Name according to [5]
     * in the XML 1.0 Recommendation
     *
     * @param name string to check
     * @return true if name is a valid Name
     */
    fun isValidXMLName(name: String): Boolean {
        if (name.length == 0)
            return false
        var ch = name[0]
        if (!isNameStartChar(ch) || ch == ':' || ch == ';')
            return false
        for (i in 1 until name.length) {
            ch = name[i]
            if (!isNameChar(ch) || ch == ':' || ch == ';' || ch == ' ') {
                return false
            }
        }
        return true
    }

    fun fixXMLName(name: String): String {
        val sb = StringBuilder()
        var ch: Char
        for (i in 0 until name.length) {
            ch = name[i]
            if (!isNameChar(ch) || ch == ':' || ch == ';' || ch == ' ') {
                sb.append("_")
            } else
                sb.append(ch)
        }
        return sb.toString()
    }

    fun validXMLName(fragment: DetailsFragment, name: String): String {
        var result = name
        val ch = result[0]
        Character.UPPERCASE_LETTER
        if(Character.getType(ch) != Character.UPPERCASE_LETTER.toInt() && Character.getType(ch) != Character.LOWERCASE_LETTER.toInt()) { //first char must be a letter
            result = "L$result"
        }
        if(!isValidXMLName(result)) { //any invalid characters?
            result = fixXMLName(result); //fix invalid characters
        }
        while(fragment.adapterItems.any { it["title"] == result }) { //check for duplicates
            if(result[result.length-1] == '9' || !Character.isDigit(result[result.length-1])) {
                result += '1'
            } else {
                var c = result[result.length-1]
                result = result.substring(0, result.length-1) + (++c)
            }
        }
        return result
    }

    fun showAlertDialog(context: Context, @StringRes title: Int, @StringRes message: Int) {
        showAlertDialog(context, context.getString(title), context.getString(message))
    }

    fun showAlertDialog(
        context: Context, @StringRes title: Int, @StringRes message: Int,
        listener: DialogInterface.OnClickListener
    ) {
        showAlertDialog(context, context.getString(title), context.getString(message), listener)
    }

    @JvmOverloads
    fun showAlertDialog(
        context: Context,
        title: String,
        message: String?,
        listener: DialogInterface.OnClickListener = DialogInterface.OnClickListener { dialog, id -> dialog.dismiss() }
    ) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        if (message != null) {
            builder.setMessage(message)
        }
        builder.setPositiveButton(R.string.ok, listener)
        builder.create().show()
    }

    /**********************************
     *
     * askPermissions
     * asks for missing permissions
     *
     */
    fun askPermissions(activity: Activity, perms: Array<String>): Boolean {
        var i = 0
        for (j in perms.indices) {
            if (ContextCompat.checkSelfPermission(activity, perms[j]) == PackageManager.PERMISSION_DENIED) {
                perms[i++] = perms[j]
            }
        }

        val permsToAsk = Arrays.copyOf(perms, i)

        if (permsToAsk.size == 0) {
            return true
        }

        ActivityCompat.requestPermissions(activity, permsToAsk, REQUEST_CODE_ASK_PERMISSIONS)
        return false
    }

    @Throws(IOException::class)
    fun copyFile(`in`: InputStream, out: OutputStream?) {
        try {
            val buf = ByteArray(1024)
            var len: Int = `in`.read(buf)
            while (len > 0) {
                out!!.write(buf, 0, len)
                len = `in`.read(buf)
            }
            `in`.close()
            out!!.close()
        } finally {
            `in`?.close()
            out?.close()
        }
    }

    fun restartApplication(context: Context) {
        val i = context.packageManager
            .getLaunchIntentForPackage(context.packageName)
        i!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        context.startActivity(i)
    }


    fun saveLogcatToFile(context: Activity) {
        val dir = if (Utils.isExternalStorageWritable) context.externalCacheDir else context.cacheDir
        val fileName = "logcat_" + System.currentTimeMillis() + ".txt"
        val outputFile = File(dir, fileName)
        try {
            val process = Runtime.getRuntime().exec("logcat -df " + outputFile.absolutePath)
            Toast.makeText(context, outputFile.absolutePath, Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
        }

    }


    internal fun checkPreferencesSecurityKey(activity: MainActivity): Boolean {
        val KEY_SECURITYKEY = activity.getString(R.string.generalPreferences_securityKeyKey)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
        val securityKey = sharedPreferences.getString(KEY_SECURITYKEY, null)
        /*
        if(securityKey == null || securityKey.length() < 12) {
            return false;
        } else {
            String newSecurityKey = getKeyHash(getEncryptedIMEI(activity));
            if(!newSecurityKey.contains(securityKey)) {
                sharedPreferences.edit().putString(KEY_SECURITYKEY, null).apply();
                return false;
            }
        }
        return true;
        */
        return securityKey != null //edit to enable Key verification
    }

    internal fun getKeyHash(imei: String): String {
        try {
            val digest = MessageDigest.getInstance("SHA-256")
            val byteHash = digest.digest(imei.toByteArray())
            var keyHash = ""
            //for(byte b : byteHash) {
            for (i in 0..19) {
                val b = byteHash[i]
                keyHash += String.format("%1$02X", b)
            }
            return keyHash
        } catch (e: NoSuchAlgorithmException) {
            return ""
        }

    }

    @SuppressLint("MissingPermission")
    internal fun getEncryptedIMEI(context: Context): String {
        var result = ""
        val xorVal = 0x55
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        var key: String? = telephonyManager.deviceId
        if (key == null || key.length < 5) {
            key = "K0Tl3T"
        }
        var i = 0
        while (i < key.length && key[i] != '\u0000') {
            result += String.format("%1$02X", key[i].toInt() xor xorVal)
            i++
        }
        return result
    }
}
