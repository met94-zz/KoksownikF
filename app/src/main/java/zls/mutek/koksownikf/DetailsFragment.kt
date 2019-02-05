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
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ListFragment
import android.support.v4.content.ContextCompat
import android.text.InputFilter
import android.text.Spanned
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ListView
import android.widget.TextView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_main.*


import org.xmlpull.v1.XmlPullParserException

import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * Created by abara on 7/20/2017.
 */

class DetailsFragment : ListFragment(), View.OnLongClickListener {
    internal var path: String? = null
    internal var id: String? = null
    lateinit var activity: MainActivity
    var adapterItems = ArrayList<HashMap<String, Any>>()
        private set(value) {
            field = value
        }
    var notes = ArrayList<HashMap<String, *>>()
    /*
        private set(value) {
            field = value
        }
    */
    internal var newFileTimestamp: String = ""
    internal var limitLoad = 0x00000002

    private val TAG = "DF_TAG"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    fun onDeleteItem(position: Int): Boolean {
        val map = adapterItems[position]
        if (map.containsKey("separator")) {
            return true
        }
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.remove).setMessage(R.string.remove_node)
            .setPositiveButton(R.string.delete) { dialog, which ->
                val childName = map["title"]
                val path = map["path"]
                /*
                if (childName != null && !childName.isEmpty()) {
                    //val child = tree!!.getChildTreeByPath(path!!)
                    //child?.setDeleteXmlEntry(true)
                    //child?.getChildren().clear()
                    adapterItems.removeAt(position)
                    (listAdapter as DetailsListAdapter).notifyDataSetChanged()
                }
                */
            }.setNegativeButton(R.string.cancel) { dialog, which -> dialog.dismiss() }
        builder.create().show()
        return true
    }

    override fun onLongClick(view: View): Boolean {
        val position = view.tag as Int
        return onDeleteItem(position)
    }

    @SuppressLint("ResourceType")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = super.onCreateView(inflater, container, savedInstanceState)
        if (v != null) {
            val listContainer =
                v.findViewById<View>(0x00ff0003) as FrameLayout//ListFragment.INTERNAL_LIST_CONTAINER_ID);
            if (listContainer != null) {
                val scale = resources.displayMetrics.density
                //int dpAsPixels = (int) (sizeInDp*scale + 0.5f);
                listContainer.setPadding(
                    (10 * scale + 0.5f).toInt(),
                    (5 * scale + 0.5f).toInt(),
                    (10 * scale + 0.5f).toInt(),
                    0
                )


                val listView = listContainer.findViewById<View>(android.R.id.list) as ListView
                if (listView != null) {
                    listView.onItemLongClickListener =
                            AdapterView.OnItemLongClickListener { parent, view, position, id -> onDeleteItem(position) }
                }

                val moreButton = Button(context)
                val _width: Int
                val _height: Int
                moreButton.text = null
                _width = ContextCompat.getDrawable(context!!, R.drawable.btn_rounded_material)!!.intrinsicWidth
                _height = ContextCompat.getDrawable(context!!, R.drawable.btn_rounded_material)!!.intrinsicHeight
                moreButton.setBackgroundResource(R.drawable.btn_rounded_material)

                val lp = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                )
                lp.width = _width
                lp.height = _height
                lp.gravity = Gravity.BOTTOM or Gravity.RIGHT
                moreButton.layoutParams = lp
                moreButton.setOnClickListener{
                        limitLoad += 2 and 0xFFFF
                        val filename = id!! + ".xml"
                        val file = context!!.getFileStreamPath(filename)
                        if (file.exists()) {
                            try {
                                /*
                                val `in` = context!!.openFileInput(filename)
                                val oldSize = tree!!.getChildren()!!.size
                                //tree = XMLUtils(activity as MainActivity)
                                //    .parseXmlToTree(tree, `in`, limitLoad, true)
                                if (tree!!.hasChildren()) {
                                    val children = tree!!.getChildren()
                                    //sort by time
                                    Collections.sort(children, Comparator { o1, o2 ->
                                        try {
                                            return@Comparator (java.lang.Long.parseLong(o2.nodeName!!.substring(1)) - java.lang.Long.parseLong(
                                                o1.nodeName?.substring(
                                                    1
                                                )!!
                                            )).toInt()
                                        } catch (e: Exception) {
                                            return@Comparator 1
                                        }
                                    })
                                    val toLoad = children!!.size - oldSize
                                    var map: HashMap<String, String>
                                    //for (int i=children.size()-1; i>(children.size()-1-toLoad); i--) {
                                    for (i in 0 until toLoad) {
                                        val child = children!![oldSize + i]
                                        if (child.hasChildren() && !child.allChildrenDeleteValidXmlEntry) {
                                            val timestamp = child.nodeName!!.substring(1) //Skip first letter 'T'
                                            map = HashMap()
                                            map["separator"] = "true"
                                            map["timestamp"] = timestamp
                                            adapterItems.add(map)
                                            for (childChild in child.getChildren()!!) {
                                                if (childChild.deleteValidXmlEntry)
                                                    continue
                                                map = HashMap()
                                                map["title"] = childChild.nodeName!!
                                                map["data"] = childChild.data!!
                                                map["path"] = "root/" + child.nodeName + "/" + childChild.nodeName
                                                adapterItems.add(map)
                                            }
                                        }
                                        (listAdapter as DetailsListAdapter).notifyDataSetChanged()
                                    }
                                }
                                */
                            } catch (e: FileNotFoundException) {
                                Utils.showAlertDialog(activity as Context, R.string.error_occurred, R.string.file_no_exists)
                                Log.w(TAG, getString(R.string.file_no_exists))
                                Log.w(TAG, e.toString())
                            } catch (e: XmlPullParserException) {
                                Utils.showAlertDialog(activity as Context, R.string.error_occurred, R.string.file_corrupted)
                                Log.w(TAG, getString(R.string.file_corrupted))
                                Log.w(TAG, e.toString())
                            } catch (e: IOException) {
                                Utils.showAlertDialog(activity as Context, R.string.error_occurred, R.string.file_unknown_error)
                                Log.w(TAG, getString(R.string.file_unknown_error))
                                Log.w(TAG, e.toString())
                            }

                        }
                    }
                listContainer.addView(moreButton)
            }
        }
        return v
    }

    override fun onResume() {
        super.onResume()
        if (path != null) {
            activity?.path_textView?.text = path!!.substring(path!!.indexOf("/") + 1)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        // Handle item selection
        when (item!!.itemId) {
            R.id.actionbar_add -> {

                val dialog = Dialog(context!!)
                dialog.setContentView(R.layout.dialog_add_details)
                dialog.setTitle(R.string.add)
                val bt = dialog.findViewById<View>(R.id.dialog_add_Button) as Button
                val et = dialog.findViewById<View>(R.id.dialog_add_editText2) as EditText
                et.filters = arrayOf(InputFilter { source, start, end, dest, dstart, dend ->
                    if (source.length > 0) {
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
                bt.setOnClickListener {
                    var newNode = et.text.toString()
                    if (newNode.isEmpty()) {
                        newNode = et.hint.toString()
                    }
                    if (!newNode.isEmpty()) {
                        val data = (dialog.findViewById<View>(R.id.dialog_add_editText3) as EditText).text.toString()


                        val calendar = Calendar.getInstance()
                        calendar.set(Calendar.HOUR_OF_DAY, 0)
                        calendar.set(Calendar.MINUTE, 0)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)
                        newFileTimestamp = calendar.timeInMillis.toString()

                        newNode = Utils.validXMLName(this, newNode)

                        val date = Date(calendar.timeInMillis)
                        val map = HashMap<String, Any>()
                        map["title"] = newNode
                        map["data"] = data
                        //map["date"] = date.toString()
                        map["path"] = path!!
                                //map["path"] = "root/" + child.nodeName + "/" + childChild.nodeName
                        adapterItems.add(0, map)

                        var updateAllDetailsMap = activity.updateMap["newdetails"] as? HashMap<String, Any>
                        if(updateAllDetailsMap == null)
                            updateAllDetailsMap = HashMap<String, Any>()

                        var updateMap: HashMap<Date, Any>?
                        updateMap = updateAllDetailsMap[path!!] as? HashMap<Date, Any>
                        if(updateMap == null)
                            updateMap = HashMap()

                        if(!updateMap.containsKey(date))
                            updateMap[date] = HashMap<String, String>()

                        var updateDataMap = updateMap[date] as HashMap<String, String>
                        updateDataMap[newNode] = data
                        updateMap[date] = updateDataMap

                        updateAllDetailsMap[path!!] = updateMap
                        updateAllDetailsMap["notes"] = notes

                        activity.updateMap["newdetails"] = updateAllDetailsMap

                        if (listAdapter == null) {
                            listAdapter = DetailsListAdapter(
                                activity!!,
                                R.layout.details_list_layout,
                                R.layout.details_list_separator,
                                adapterItems,
                                this@DetailsFragment
                            )
                        }
                        (listAdapter as DetailsListAdapter).notifyDataSetChanged()
                    }
                    dialog?.dismiss()
                }
                dialog.show()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun validTree(): Boolean {
        if (id == null) {
            id = if (arguments != null) arguments!!.getString("id") else ""
        }

        if (path == null) {
            path = if (arguments != null) arguments!!.getString("path") else ""
            activity?.path_textView?.text = path!!.substring(path!!.indexOf("/") + 1)
        }

        if (activity == null) {
            return false
        }

        return true
    }

    private fun downloadData() {
        if(notes.size != 0)
        {
            if(context != null && adapterItems.size == 0) {
                initializeAdapterList();
            }
            return
        }

        // Access a Cloud Firestore instance from your Activity
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("users").document("CycxoH93888zgq31fry6")
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    Log.d(TAG, "DocumentSnapshot data: " + document.data)
                    val dirsRef = docRef.collection("dirs")
                    dirsRef.get()
                        .addOnSuccessListener { result ->
                            for (docDirRef in result) {
                                Log.d(TAG, docDirRef.id + " => " + docDirRef.data)
                                if(docDirRef.data["path"] == path) {
                                    val notesRef = dirsRef.document(docDirRef.id).collection("notes")
                                    notesRef.orderBy("created", Query.Direction.DESCENDING).get()
                                        .addOnSuccessListener { result ->
                                            for (docNoteRef in result) {
                                                Log.d(TAG, docNoteRef.id + " => " + docNoteRef.data)
                                                val noteData = docNoteRef.data as? HashMap<String, String>
                                                if(noteData != null)
                                                    notes.add(noteData)
                                            }
                                            initializeAdapterList();
                                        }
                                        .addOnFailureListener { exception ->
                                            Log.d(TAG, "Error getting documents: ", exception)
                                        }
                                }
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.d(TAG, "Error getting documents: ", exception)
                        }
                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
    }

    private fun initializeAdapterList() {
        var map: HashMap<String, Any>
        var mapNote: HashMap<String, String>

        for(note in notes) {
            map = HashMap()
            map["separator"] = "true"
            map["created"] = note["created"]!!
            adapterItems.add(map)
            mapNote = note["data"] as HashMap<String, String>
            for ((key, value) in mapNote) {
                map = HashMap()
                map["title"] = key
                map["data"] = value
                map["path"] = path!!
                map["created"] = note["created"]!!
                //map["path"] = "root/" + child.nodeName + "/" + childChild.nodeName
                adapterItems.add(map)
            }
        }
        //
        if(listAdapter == null)
            listAdapter = DetailsListAdapter(
                context!!,
                R.layout.details_list_layout,
                R.layout.details_list_separator,
                adapterItems,
                this
            )
        else
            (listAdapter as DetailsListAdapter).notifyDataSetChanged()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        activity = context as MainActivity

        if (validTree()) {
            /*
            val children = tree!!.getChildren()
            Collections.sort(children, Comparator { o1, o2 ->
                try {
                    return@Comparator (java.lang.Long.parseLong(o2.nodeName!!.substring(1)) - java.lang.Long.parseLong(
                        o1.nodeName!!.substring(
                            1
                        )
                    )).toInt()
                } catch (e: Exception) {
                    return@Comparator 1
                }
            })

            */
            downloadData()
        }
    }

    companion object {

        private val TAG = "DETAILS_TAG"
    }
}
