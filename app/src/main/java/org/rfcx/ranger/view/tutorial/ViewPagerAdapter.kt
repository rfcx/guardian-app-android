package org.rfcx.ranger.view.tutorial

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_page.view.*
import org.rfcx.ranger.R
import org.rfcx.ranger.util.getImage

class ViewPagerAdapter(private var images: List<Int>) : RecyclerView.Adapter<ViewPagerAdapter.ViewPagerViewHolder>() {
	
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewPagerViewHolder {
		val view = LayoutInflater.from(parent.context).inflate(R.layout.item_page, parent, false)
		return ViewPagerViewHolder(view)
	}
	
	override fun getItemCount(): Int = images.size
	
	override fun onBindViewHolder(holder: ViewPagerViewHolder, position: Int) {
		holder.bind(images[position])
	}
	
	inner class ViewPagerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		private val imageView = itemView.imageView
		
		fun bind(image: Int) {
			imageView.setImageDrawable(itemView.context?.getImage(image))
		}
	}
}
