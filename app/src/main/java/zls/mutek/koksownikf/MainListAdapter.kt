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
import android.content.Context
import android.content.DialogInterface
import android.support.annotation.LayoutRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

import java.util.HashMap

/**
 * Created by abara on 7/19/2017.
 */

class MainListAdapter(
    private val mContext: Context, @param:LayoutRes @field:LayoutRes private val mResourceId: Int, // declaring our ArrayList of items
    private val mObjects: List<String>, private val mFragment: MainListFragment
) : ArrayAdapter<String>(mContext, mResourceId, mObjects) {

    internal var listedDirs: ArrayList<String> = ArrayList<String>()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var v = convertView
        val map: String = mObjects[position]

        if (v == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            v = inflater.inflate(mResourceId, null)
        }

        val textView = v?.findViewById<View>(R.id.main_list_TextView1) as TextView?
        if (textView != null) {
            val splitted = map.split('/')
            for(i in splitted!!.indices) {
                if(splitted[i] == mFragment.curPath) {
                    //if(listedDirs.contains(splitted[i+1])) {
                   //     break;
                    //}
                    //listedDirs.add(splitted[i+1])
                    textView.text = splitted[i+1]
                    break
                }
            }
            /*
            var startIndex = map.indexOf(mFragment.curPath!!)
            startIndex = if(startIndex != -1) startIndex+mFragment.curPath!!.length else 0
            val endIndex = map.indexOf('/', startIndex+1)
            textView.text = map.substring(startIndex, endIndex)//map["title"]!!.replace('_', ' ')
            */
        }

        // the view must be returned to our activity
        return v

    }

}
