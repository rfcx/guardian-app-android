package org.rfcx.ranger.adapter.classifycation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.event.Confidence

class ClassificationAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
	
	var lists: ArrayList<Classification> = arrayListOf()
	
	fun setClassification(list: List<Confidence>) {
		list.sortedBy { it.beginsAtOffset }
		
		for (i in 0 until list.count()) {
			
			val current = list[i]
			if (lists.isEmpty()) {
				if (current.beginsAtOffset != 0L) {
					lists.add(ClassificationEmptyBox(0L, current.beginsAtOffset))
					lists.add(ClassificationBox(current.beginsAtOffset, current.endsAtOffset))
				} else {
					lists.add(ClassificationBox(current.beginsAtOffset, current.endsAtOffset))
				}
				
			} else {
				val pref = list[i - 1]
				if (current.beginsAtOffset > pref.endsAtOffset) {
					lists.add(ClassificationEmptyBox(pref.endsAtOffset, current.beginsAtOffset))
					lists.add(ClassificationBox(current.beginsAtOffset, current.endsAtOffset))
				} else {
					lists.add(ClassificationBox(current.beginsAtOffset, current.endsAtOffset))
				}
			}
			
			if (i == list.count() - 1) {
				if (current.endsAtOffset != MAX_DURATION) {
					lists.add(ClassificationEmptyBox(current.endsAtOffset, MAX_DURATION))
				}
			}
		}
	}
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
		val inflater = LayoutInflater.from(parent.context)
		return when (viewType) {
			VIEW_CLASSIFICATION -> ClassificationHolder(inflater.inflate(R.layout.item_classification, parent, false))
			else -> ClassificationEmptyHolder(inflater.inflate(R.layout.item_classification_empty, parent, false))
		}
	}
	
	override fun getItemCount(): Int {
		return lists.count()
	}
	
	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
	}
	
	override fun getItemViewType(position: Int): Int {
		return when (lists[position]) {
			is ClassificationBox -> VIEW_CLASSIFICATION
			else -> VIEW_EMPTY
		}
	}
	
	class ClassificationHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
	class ClassificationEmptyHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
	
	companion object {
		const val VIEW_CLASSIFICATION = 1
		const val VIEW_EMPTY = 2
		const val MAX_SPAN_COUNT = 90 // max duration is 90 seconds
		const val MAX_DURATION = 90000L
	}
	
}

interface Classification {
	fun durationSecond(): Int
}

data class ClassificationBox(val beginAt: Long, val endAt: Long, val duration: Long = endAt - beginAt) : Classification {
	override fun durationSecond(): Int = (duration / 1000).toInt()
}

data class ClassificationEmptyBox(val beginAt: Long, val endAt: Long, val duration: Long = endAt - beginAt) : Classification {
	override fun durationSecond(): Int = (duration / 1000).toInt()
}