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
import android.content.Context
import android.content.Intent
import android.os.Build
import android.preference.Preference
import android.support.annotation.RequiresApi
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.Toast

import java.io.File

/**
 * Created by abara on 6/16/2017.
 */

class ButtonPreference : Preference {

    private var mRefreshThemeButton: Button? = null

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    internal constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    ) {
    }


    override fun onBindView(view: View) {
        super.onBindView(view)

        mRefreshThemeButton = view.findViewById<View>(R.id.button_refresh_theme) as Button?
        if (mRefreshThemeButton?.hasOnClickListeners() == false) {
            mRefreshThemeButton?.setOnClickListener {
                Utils.restartApplication(context)
            }
        }
    }
}
