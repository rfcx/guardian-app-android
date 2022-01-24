package org.rfcx.incidents.widget

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.rfcx.incidents.R

@SuppressLint("ClickableViewAccessibility")
class SoundRecordProgressView @JvmOverloads constructor(
		context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
	
	var state: SoundRecordState = SoundRecordState.NONE
		set(value) {
			field = value
			onStateChange()
		}
	var isDisableEdit = false
	var onStateChangeListener: OnStateChangeListener? = null
	
	private var soundWaveViewAdapter = SoundWaveViewAdapter()
	private val animateHandler = Handler()
	private lateinit var _recyclerView: NoneTouchableRecycler
	private var cancelButton: ImageButton
	private var desTextView: TextView
	private var actionButton: ImageButton
	
	private val onScroll = object : RecyclerView.OnScrollListener() {
		override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
			super.onScrolled(recyclerView, dx, dy)
			
			val totalItemCount = (recyclerView.layoutManager as LinearLayoutManager).itemCount
			val lastVisibleItem = (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
			if (lastVisibleItem + 2 > totalItemCount) {
				soundWaveViewAdapter.addMoreView()
			}
		}
	}
	
	private val runnable = object : Runnable {
		override fun run() {
			_recyclerView.smoothScrollBy(4, 0)
			animateHandler.postDelayed(this, 100)
		}
	}
	
	init {
		View.inflate(context, R.layout.widget_sound_record_progress, this)
		_recyclerView = findViewById(R.id.recyclerView)
		_recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
		_recyclerView.adapter = soundWaveViewAdapter
		_recyclerView.addOnScrollListener(onScroll)
		
		cancelButton = findViewById(R.id.cancelButton)
		actionButton = findViewById(R.id.actionButton)
		desTextView = findViewById(R.id.tabToRecText)
		
		actionButton.setOnTouchListener(OnTouchListener { _, p1 ->
			
			if (p1?.action == MotionEvent.ACTION_DOWN && state == SoundRecordState.NONE) {
				state = SoundRecordState.RECORDING
				return@OnTouchListener true
				
			} else if (p1?.action == MotionEvent.ACTION_UP && state == SoundRecordState.RECORDING) {
				state = SoundRecordState.STOPPED_RECORD
				return@OnTouchListener true
			}
			false
		})
		
		actionButton.setOnClickListener {
			when (state) {
				SoundRecordState.STOPPED_RECORD -> {
					resetAnimate()
					state = SoundRecordState.PLAYING
				}
				SoundRecordState.PLAYING -> state = SoundRecordState.STOP_PLAYING
				SoundRecordState.STOP_PLAYING -> state = SoundRecordState.PLAYING
				else -> {}
			}
		}
		
		cancelButton.setOnClickListener {
			state = SoundRecordState.NONE
		}
		
	}
	
	private fun startAnimate() {
		animateHandler.postDelayed(runnable, 100)
	}
	
	private fun stopAnimate() {
		animateHandler.removeCallbacks(runnable)
	}
	
	private fun resetAnimate() {
		animateHandler.removeCallbacks(runnable)
		soundWaveViewAdapter.reset()
		(_recyclerView.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(0, 0)
	}
	
	override fun onDetachedFromWindow() {
		animateHandler.removeCallbacks(runnable)
		super.onDetachedFromWindow()
	}
	
	private fun onStateChange() {
		Log.d("onStateChange", state.name)
		when (state) {
			SoundRecordState.NONE -> {
				if (isDisableEdit) return
				stopAnimate()
				desTextView.visibility = View.VISIBLE
				cancelButton.visibility = View.GONE
				actionButton.setImageResource(R.drawable.ic_record)
				resetAnimate()
			}
			SoundRecordState.RECORDING -> {
				if (isDisableEdit) return
				desTextView.visibility = View.GONE
				cancelButton.visibility = View.GONE
				actionButton.setImageResource(R.drawable.ic_record_stop)
				startAnimate()
			}
			SoundRecordState.STOPPED_RECORD -> {
				desTextView.visibility = View.GONE
				cancelButton.visibility = View.VISIBLE
				actionButton.setImageResource(R.drawable.ic_record_play)
				stopAnimate()
			}
			SoundRecordState.PLAYING -> {
				desTextView.visibility = View.GONE
				cancelButton.visibility = View.GONE
				actionButton.setImageResource(R.drawable.ic_record_stop)
				resetAnimate()
				startAnimate()
			}
			
			SoundRecordState.STOP_PLAYING -> {
				desTextView.visibility = View.GONE
				if (!isDisableEdit) {
					cancelButton.visibility = View.VISIBLE
				}else{
					cancelButton.visibility = View.GONE
				}
				actionButton.setImageResource(R.drawable.ic_record_play)
				stopAnimate()
			}
		}
		
		onStateChangeListener?.invoke(state)
	}
	
	fun disableEdit() {
		isDisableEdit = true
	}
	
}

enum class SoundRecordState {
	NONE, RECORDING, STOPPED_RECORD, PLAYING, STOP_PLAYING
}

class SoundWaveViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
	
	private val sources = ArrayList<Any?>()
	
	init {
		addMoreView()
	}
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
		
		val inflater = LayoutInflater.from(parent.context)
		return when (viewType) {
			VIEW_TYPE_START -> {
				StartView(inflater.inflate(R.layout.item_widget_sound_progress_start_view, parent, false))
			}
			else -> SoundWaveView(inflater.inflate(R.layout.item_widget_sound_progress_visualizer_view, parent, false))
		}
	}
	
	override fun getItemCount(): Int = sources.count()
	
	
	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
	
	}
	
	override fun getItemViewType(position: Int): Int {
		return if (position == 0) VIEW_TYPE_START else VIEW_TYPE_SOUND
	}
	
	fun addMoreView() {
		var i = 0
		while (i < 20) {
			sources.add(null)
			i++
		}
		notifyDataSetChanged()
	}
	
	fun reset() {
		sources.clear()
		addMoreView()
		notifyDataSetChanged()
	}
	
	private inner class SoundWaveView(itemView: View) : RecyclerView.ViewHolder(itemView)
	
	private inner class StartView(itemView: View) : RecyclerView.ViewHolder(itemView)
	
	companion object {
		private const val VIEW_TYPE_START = 1
		private const val VIEW_TYPE_SOUND = 2
	}
}


typealias OnStateChangeListener = (SoundRecordState) -> Unit
