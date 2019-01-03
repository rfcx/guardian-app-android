package org.rfcx.ranger.adapter.guardian

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.guardian.GuardianGroup


/**
 * Data provider for guardian group spinner in Settings
 */

class GuardianGroupsAdapter: ArrayAdapter<GuardianGroup> {

    val inflater: LayoutInflater

    private var values: List<GuardianGroup> = ArrayList()

    constructor(context: Activity):
            super(context, R.layout.settings_spinner, android.R.id.text1) {

        setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
        inflater = context.layoutInflater
    }

    fun setData(groups: List<GuardianGroup>) {
        values = groups
        clear()
        addAll(groups)
        notifyDataSetChanged()
    }

    override fun getItem(position: Int): GuardianGroup = values[position]

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = createViewFromResource(convertView, parent, R.layout.settings_spinner)

        return bindData(getItem(position), view)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = createViewFromResource(convertView, parent, android.R.layout.simple_spinner_dropdown_item)

        return bindData(getItem(position), view)
    }

    private fun createViewFromResource(convertView: View?, parent: ViewGroup, layoutResource: Int): TextView {
        val context = parent.context
        val view = convertView ?: LayoutInflater.from(context).inflate(layoutResource, parent, false)
        return view as TextView
    }

    private fun bindData(value: GuardianGroup, view: TextView): TextView {
        view.text = value.name
        return view
    }
}
