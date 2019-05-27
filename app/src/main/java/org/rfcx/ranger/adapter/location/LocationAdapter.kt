package org.rfcx.ranger.adapter.location

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_location.view.*
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.location.CheckIn
import org.rfcx.ranger.localdb.LocationDb
import org.rfcx.ranger.util.DateHelper

class LocationAdapter : RecyclerView.Adapter<LocationAdapter.LocationViewHolder>() {
	
	private val locations = LocationDb().allForDisplay()
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder =
			LocationViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_location, parent, false))
	
	override fun getItemCount(): Int {
		return locations.count()
	}
	
	override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
		locations[position]?.let { holder.bind(it) }
	}
	
	inner class LocationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		fun bind(checkin: CheckIn) {
			itemView.timeTextView.text = DateHelper.parse(checkin.time)
			itemView.latitudeTextView.text = checkin.latitude.toString()
			itemView.longitudeTextView.text = checkin.longitude.toString()
			itemView.syncedTextView.setTextColor(ContextCompat.getColor(
					itemView.context, (if (checkin.synced) android.R.color.holo_green_light
			else android.R.color.holo_orange_light)))
		}
	}
}


