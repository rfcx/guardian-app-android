package org.rfcx.ranger.adapter.location

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_location.view.*
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.location.RangerLocation
import org.rfcx.ranger.util.DateHelper
import org.rfcx.ranger.util.RealmHelper

class LocationAdapter : RecyclerView.Adapter<LocationAdapter.LocationViewHolder>() {
	
	private val locations = RealmHelper.getInstance().getLocations()
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder =
			LocationViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_location, parent, false))
	
	override fun getItemCount(): Int {
		Log.d("getItemCount", locations.count().toString())
		return locations.count()
	}
	
	override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
		holder.bind(locations[position])
	}
	
	
	inner class LocationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		fun bind(location: RangerLocation) {
			itemView.timeTextView.text = DateHelper.parse(location.time)
			itemView.latitudeTextView.text = location.latitude.toString()
			itemView.longitudeTextView.text = location.longitude.toString()
		}
	}
	
}


