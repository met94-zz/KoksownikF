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

import android.content.Context
import android.support.annotation.LayoutRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView

import java.text.SimpleDateFormat
import java.util.Date
import java.util.HashMap
import java.util.Locale
import java.util.Random

/**
 * Created by abara on 7/25/2017.
 */

class DetailsListAdapter(
    context: Context, @param:LayoutRes @field:LayoutRes
    private val mResourceId: Int, @param:LayoutRes internal var mSeparatorId: Int, // declaring our ArrayList of items
    private val mObjects: List<HashMap<String, Any>>, internal var fragment: DetailsFragment
) : ArrayAdapter<HashMap<String, Any>>(context, mResourceId, mObjects), View.OnFocusChangeListener {

    override fun getItemViewType(position: Int): Int {
        val map = mObjects[position]
        val isSeparator = map.containsKey("separator")
        return if (isSeparator) {
            0
        } else 1
    }

    override fun getViewTypeCount(): Int {
        return 2
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var v = convertView
        val map = mObjects[position]
        val isSeparator = map.containsKey("separator")

        if (v == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            if (isSeparator) {
                v = inflater.inflate(mSeparatorId, null)
                val textView = v!!.findViewById<View>(R.id.details_list_TextView) as TextView
                val created = map["created"].toString()
                textView.tag = created
                textView.text = convertTimestampToDate(Date(Date.parse(created)))
                return v
            } else {
                v = inflater.inflate(mResourceId, null)
            }
        }

        if (!isSeparator) {
            val textView = v!!.findViewById<View>(R.id.details_list_TextView1) as TextView
            if (textView != null) {
                textView.text = (map["title"] as String).replace('_', ' ')
                textView.tag = position
                textView.setOnLongClickListener(fragment)
            }

            val editText = v.findViewById<View>(R.id.details_list_EditText1) as EditText
            if (editText != null) {
                editText.setText(map["data"] as String)
                //editText.setTag(position);
                //map["position"] = position
                editText.tag = map
                editText.onFocusChangeListener = this
            }
        } else {
            val textView = v!!.findViewById<View>(R.id.details_list_TextView) as TextView
            val created = map["created"] as Date//.toString()
            //if(!((String)(textView.getTag())).equals(timestamp)) {
            if (textView.tag != created) {
                textView.text = convertTimestampToDate(created)//Date(Date.parse(created)))
            }
        }

        // the view must be returned to our activity
        return v

    }


    override fun onFocusChange(v: View, hasFocus: Boolean) {
        val id = v.id
        when (id) {
            R.id.details_list_EditText1 -> if (!hasFocus) {
                val map = v.tag as? HashMap<String, Any>
                if (map != null && map.containsKey("path") && map.containsKey("created")) {
                    val path = map["path"] as String
                    val created = map["created"] as? Date
                    if (!path.isEmpty() && created != null) {
                        val newData = (v as EditText).text.toString()
                        fragment.adapterItems.filter {
                            it["created"] == created && it["title"] == map["title"]
                        }?.get(0)?.let {
                            it["data"] = newData
                        }

                        fragment.notes.filter {
                            it["created"] == created
                        }.get(0).let {
                            (it as? HashMap<String, HashMap<String, String>>)?.get("data")?.let {
                                it[map["title"] as String] = newData
                            }
                        }

                        var updateAllDetailsMap = fragment.activity.updateMap["details"] as? HashMap<String, Any> ?: HashMap()

                        var updateMap: HashMap<Date, Any> = updateAllDetailsMap[path] as? HashMap<Date, Any> ?: HashMap()

                        if (!updateMap.containsKey(created))
                            updateMap[created] = HashMap<String, String>()


                        var updateDataMap = updateMap[created] as HashMap<String, String>
                        updateDataMap[map["title"] as String] = newData
                        updateMap[created] = updateDataMap
                        updateMap[Date(0)] = fragment.notes

                        updateAllDetailsMap[path] = updateMap

                        fragment.activity.updateMap["details"] = updateAllDetailsMap
                    }
                }
            }
            else -> {
            }
        }
    }

    private fun convertTimestampToDate(date: Date/*String?*/): String {
        //val format = SimpleDateFormat("dd/MM/yyyy", Locale("pl", "PL"))//new SimpleDateFormat("MM/dd/yyyy HH:mm");
        return SimpleDateFormat("dd/MM/yyyy", Locale("pl", "PL")).format(date)
    }

}
