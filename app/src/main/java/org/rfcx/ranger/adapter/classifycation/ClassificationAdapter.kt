package org.rfcx.ranger.adapter.classifycation

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.event.Confidence

class ClassificationAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
	
	var lists: ArrayList<Classification> = arrayListOf()
	var onDetectionBoxClick: ((ClassificationBox) -> Unit)? = null
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
				val lastedBox = lists[lists.lastIndex]
				if (current.beginsAtOffset > pref.endsAtOffset) {
					lists.add(ClassificationEmptyBox(pref.endsAtOffset, current.beginsAtOffset))
					lists.add(ClassificationBox(current.beginsAtOffset, current.endsAtOffset))
				} else if (current.beginsAtOffset == pref.endsAtOffset) {
					// combine the box
					lastedBox.endAt = current.endsAtOffset
					
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
			VIEW_CLASSIFICATION ->
				ClassificationHolder(inflater.inflate(R.layout.item_classification, parent, false)
						, onDetectionBoxClick)
			else -> ClassificationEmptyHolder(inflater.inflate(R.layout.item_classification_empty, parent, false),
					onDetectionBoxClick)
		}
	}
	
	override fun getItemCount(): Int {
		return lists.count()
	}
	
	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		val box = lists[position]
		if (holder is ClassificationHolder && box is ClassificationBox) {
			holder.bind(box)
		} else if (holder is ClassificationEmptyHolder && box is ClassificationEmptyBox) {
			holder.bind(box)
		}
	}
	
	override fun getItemViewType(position: Int): Int {
		return when (lists[position]) {
			is ClassificationBox -> VIEW_CLASSIFICATION
			else -> VIEW_EMPTY
		}
	}
	
	class ClassificationHolder(itemView: View, private val onDetectionBoxClick: ((ClassificationBox) -> Unit)?) : RecyclerView.ViewHolder(itemView) {
		
		fun bind(classificationBox: ClassificationBox) {
			itemView.setOnClickListener {
				onDetectionBoxClick?.invoke(classificationBox)
			}
		}
	}
	
	class ClassificationEmptyHolder(itemView: View, private val onDetectionBoxClick: ((ClassificationBox) -> Unit)?) : RecyclerView.ViewHolder(itemView) {
		
		fun bind(classificationBox: ClassificationEmptyBox) {
			
			var lastTouchDownX = 0f
			itemView.setOnTouchListener { _, event ->
				
				if (event.actionMasked == MotionEvent.ACTION_DOWN) {
					lastTouchDownX = event.x
				}
				false
			}
			itemView.setOnClickListener {
				
				val boxWidth = it.width
				val durationOnClickPosition = classificationBox.durationSecond() * lastTouchDownX / boxWidth
				onDetectionBoxClick?.invoke(ClassificationBox((classificationBox.beginAt + (durationOnClickPosition * 1000)).toLong(), classificationBox.endAt))
			}
		}
	}
	
	companion object {
		const val VIEW_CLASSIFICATION = 1
		const val VIEW_EMPTY = 2
		const val MAX_SPAN_COUNT = 90 // max duration is 90 seconds
		const val MAX_DURATION = 90000L
	}
	
}

open class Classification(open var beginAt: Long, open var endAt: Long) {
	fun durationSecond(): Int = ((endAt - beginAt) / 1000).toInt()
}

data class ClassificationBox(override var beginAt: Long, override var endAt: Long) : Classification(beginAt, endAt)
data class ClassificationEmptyBox(override var beginAt: Long, override var endAt: Long) : Classification(beginAt, endAt)
