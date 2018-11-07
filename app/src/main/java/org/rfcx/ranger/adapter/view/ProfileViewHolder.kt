package org.rfcx.ranger.adapter.view

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import org.rfcx.ranger.adapter.OnLocationTrackingChangeListener

/**
 * View holder for the header profile section (which shows the location tracking status)
 */

class ProfileViewHolder(itemView: View, private var onLocationTrackingChangeListener: OnLocationTrackingChangeListener):
        RecyclerView.ViewHolder(itemView) {

    fun bind() {

    }
}