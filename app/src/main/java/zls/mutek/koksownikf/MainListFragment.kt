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
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.ListFragment
import android.text.InputFilter
import android.text.Spanned
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ListView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.dialog_add.*

import java.io.File
import java.util.ArrayList
import java.util.HashMap

/**
 * Created by abara on 7/19/2017.
 */

class MainListFragment : ListFragment() {

    lateinit var activity: MainActivity
    lateinit var path: String

    var curPath: String = ""
    internal var adapterItems = ArrayList<String>()
    internal var dirs = ArrayList<String>()
    private var layoutView: View? = null

    private val TAG = "MF_TAG"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)

        path = arguments?.getString("path") ?: ""
        curPath = arguments?.getString("curPath") ?: ""
        arguments?.getSerializable("dirs")?.let {
            initializeAdapterList(it as ArrayList<String>)
        }

        activity.path_textView?.text = path.substring(path.indexOf("/") + 1)

        if (listAdapter == null && this.dirs.size != 0) {
            listAdapter = MainListAdapter(activity, R.layout.main_list_layout, adapterItems, this)
        }
    }

    @SuppressLint("ResourceType")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layoutView = super.onCreateView(inflater, container, savedInstanceState)
        if (layoutView != null) {
            val listContainer = layoutView!!.findViewById<View>(0x00ff0003)//ListFragment.INTERNAL_LIST_CONTAINER_ID);
            if (listContainer != null) {
                val scale = resources.displayMetrics.density
                val sizeInDp = 10.0f
                val dpAsPixels = (sizeInDp * scale + 0.5f).toInt()
                listContainer.setPadding(dpAsPixels, dpAsPixels, dpAsPixels, 0)

                /*
                val listView = listContainer.findViewById<View>(android.R.id.list) as ListView?
                listView?.onItemLongClickListener =
                    AdapterView.OnItemLongClickListener { parent, view, position, id ->
                        val builder = AlertDialog.Builder(activity)
                        builder.setTitle(R.string.remove).setMessage(R.string.remove_node)
                            .setPositiveButton(R.string.delete) { dialog, which ->
                                /*
                                    adapterItems.removeAt(position)
                                    (listAdapter as MainListAdapter).notifyDataSetChanged()
                                */
                            }.setNegativeButton(R.string.cancel) { dialog, which -> dialog.dismiss() }
                        builder.create().show()
                        true
                    }
               */
            }
        }
        return layoutView
    }

    override fun onResume() {
        super.onResume()
        activity.path_textView?.text = path.substring(path.indexOf("/") + 1)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        // Handle item selection
        when (item!!.itemId) {
            R.id.actionbar_add -> {

                val dialog = Dialog(context!!)
                dialog.setContentView(R.layout.dialog_add)
                dialog.setTitle(R.string.add)
                val et = dialog.dialog_add_editText
                et.filters = arrayOf(InputFilter { source, start, end, dest, dstart, dend ->
                    if (source.isNotEmpty()) {
                        val ch = source[0]
                        if (ch == ':') {// || ch == ' ') {
                            return@InputFilter "_"
                        }

                        if (dstart > 0 && !Utils.isNameChar(ch) && ch != ' ') {
                            return@InputFilter ""
                        }
                        if (dstart == 0 && !Utils.isNameStartChar(ch)) {
                            return@InputFilter ""
                        }

                    }
                    null
                })
                val checkBox = dialog.dialog_add_CheckBox
                val bt = dialog.dialog_add_Button
                bt.setOnClickListener {
                    var newNode: String? = et.text.toString()
                    if (newNode != null && !newNode.isEmpty()) {// && validTree()) {
                        if (!Utils.isValidXMLName(newNode)) { //any invalid characters?
                            newNode = Utils.fixXMLName(newNode) //fix invalid characters
                        }
                        var newPath = path.substring(0, (path.indexOf(curPath!!)) + (curPath?.length ?: 0))
                        newPath += "/$newNode/"
                        if (!checkBox.isChecked)
                            newPath += "*";
                        if (!dirs.contains(newPath)) {
                            dirs.add(newPath)
                            adapterItems.add(newPath)
                            var map = activity.updateMap["dirs"] as? HashMap<String, Any>
                            if (map == null)
                                map = HashMap<String, Any>()
                            map[path] = newPath
                            activity.updateMap["dirs"] =
                                map //przy zapisie zrobic jakies mergowanie sciezek np w thegym nie ma dira tricek i zostaly utworzone thegym/tricek/beton i thegym/tricek wiec nie potrzebujemy same thegym/tricek
                        }
                        //newPath.orEmpty()
                        if (listAdapter == null) {
                            listAdapter = MainListAdapter(
                                activity,
                                R.layout.main_list_layout,
                                adapterItems,
                                this@MainListFragment
                            )
                        }

                        (listAdapter as MainListAdapter).notifyDataSetChanged()
                    }/* else {
                            Utils.showAlertDialog(MainListFragment.this.getActivity(), R.string.error_occurred, R.string.internal_error);
                        }
                        */
                    dialog.dismiss()
                }
                dialog.show()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        activity = context as MainActivity
    }

    fun initializeAdapterList(dirs: ArrayList<String>) {
        if (this.dirs.size != 0) return
        this.dirs = dirs
        //if(curPath.isNullOrEmpty())
        //    adapterItems.addAll(dirs)
        //else {
        var level = -1
        var isRoot = 0
        if (!curPath.isNullOrEmpty()) {
            val splitted = path.split('/')
            for (i in splitted.indices) {
                if (splitted[i] == curPath) {
                    level = i
                    break
                }
            }
        } else {
            level = 1
            isRoot = 1
        }
        if (level != -1) {
            var helpArray = ArrayList<String>()
            for (dir in dirs) {
                val splitted = dir.split('/')
                if (level >= splitted.size) continue
                //trzeba dodac jakies sprawdzenie czy wczesniejsza czesc sciezki sie zgadza
                val checkIndex = if (isRoot != 0) level else level + 1
                if ((splitted[level] == curPath || curPath.isNullOrEmpty()) && //first should valid if that curpath matches so we wont get bound of array below
                    !helpArray.contains(splitted[checkIndex])
                ) {
                    if (!splitted[checkIndex].isEmpty()) {
                        adapterItems.add(dir)
                        helpArray.add(splitted[checkIndex])
                    }
                }
            }
        }
        //}
        if (listAdapter == null && context != null) {
            listAdapter = MainListAdapter(context!!, R.layout.main_list_layout, adapterItems, this)
        } else (listAdapter as MainListAdapter?)?.notifyDataSetChanged()
    }

    override fun onListItemClick(l: ListView?, v: View?, position: Int, id: Long) {
        var fr: ListFragment? = null
        val args = Bundle(2)

        path = adapterItems[position]
        args.putString("path", path)
        var nextPath = ""
        val splitted = path.split('/')
        var index = -1
        for (i in splitted.indices) {
            if (splitted[i] == curPath) {
                nextPath = splitted[i + 1]
                index = i
                break
            }
        }
        if (index != -1 && (index + 2) < splitted.size && splitted[index + 2] == "*") {
            fr = DetailsFragment.newInstance(path)
        } else {
            fr = MainListFragment.newInstance(path, nextPath, dirs)
        }

        /*
        fragmentManager!!.addOnBackStackChangedListener {
            if (fr is DetailsFragment)
                this.notes = fr.notes
        }
        */
        fragmentManager!!.beginTransaction().replace(R.id.fragment_container, fr).addToBackStack(null).commit()
    }


    companion object {

        fun newInstance(path: String, curPath: String?, dirs: ArrayList<*>?): MainListFragment {
            val myFragment = MainListFragment()

            val args = Bundle(1)
            args.putString("path", path)
            curPath?.let { args.putString("curPath", it) }
            dirs?.let { args.putSerializable("dirs", it) }
            myFragment.arguments = args

            return myFragment
        }
    }
}
