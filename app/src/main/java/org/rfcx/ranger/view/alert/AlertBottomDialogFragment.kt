package org.rfcx.ranger.view.alert

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.exoplayer2.Player
import kotlinx.android.synthetic.main.fragment_dialog_alert.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.ranger.R
import org.rfcx.ranger.adapter.classifycation.ClassificationAdapter
import org.rfcx.ranger.data.remote.success
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.util.GlideApp
import org.rfcx.ranger.util.getIconRes
import org.rfcx.ranger.view.base.BaseBottomSheetDialogFragment

class AlertBottomDialogFragment : BaseBottomSheetDialogFragment() {
	
	private val alertViewModel: AlertBottomDialogViewModel by viewModel()
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		
		val event: Event? = arguments?.getParcelable(BUNDLE_EVENT)
		if (event == null) {
			dismissDialog()
		} else {
			alertViewModel.setEvent(event)
		}
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_dialog_alert, container, false)
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		
		setupView()
		observeEventView()
		observePlayer()
		observeClassifiedCation()
		observeReveiewEvent()
	}
	
	private fun setupView() {
		soundProgressSeekBar.isEnabled = false
		soundProgressSeekBar.max = maxProgress
		
		replayButton.setOnClickListener {
			alertViewModel.replaySound()
		}
		
		negativeButton.setOnClickListener {
			if (alertViewModel.eventState.value == EventState.NONE) {
				alertViewModel.reviewEvent(false)
			} else {
				// Navigate to google map
			}
		}
		
		positiveButton.setOnClickListener {
			if (alertViewModel.eventState.value == EventState.NONE) {
				alertViewModel.reviewEvent(true)
			} else {
				dismissDialog()
			}
		}
	}
	
	@SuppressLint("SetTextI18n")
	private fun observeEventView() {
		alertViewModel.event.observe(this, Observer {
			eventIconImageView.setImageResource(it.getIconRes())
			eventNameTextView.text = "${it.value?.capitalize()} ?"
		})
		
		alertViewModel.spectrogramImage.observe(this, Observer {
			GlideApp.with(spectrogramImageView)
					.load(it)
					.placeholder(R.drawable.spectrogram_place_holder)
					.error(R.drawable.spectrogram_place_holder)
					.into(spectrogramImageView)
		})
		
		alertViewModel.eventState.observe(this, Observer {
			when (it!!) {
				EventState.NONE -> {
					negativeButton.text = getString(R.string.common_no)
					positiveButton.text = getString(R.string.common_yes)
				}
				EventState.REVIEWED -> {
				
				}
			}
		})
	}
	
	private fun observePlayer() {
		
		alertViewModel.playerError.observe(this, Observer {
			Toast.makeText(context, R.string.can_not_play_audio, Toast.LENGTH_SHORT).show()
			loadingSoundProgressBar.visibility = View.INVISIBLE
			replayButton.visibility = View.VISIBLE
		})
		
		alertViewModel.playerState.observe(this, Observer {
			when (it) {
				Player.STATE_BUFFERING -> {
					replayButton.visibility = View.INVISIBLE
					loadingSoundProgressBar.visibility = View.VISIBLE
				}
				Player.STATE_READY -> {
					replayButton.visibility = View.INVISIBLE
					loadingSoundProgressBar.visibility = View.INVISIBLE
				}
				Player.STATE_IDLE -> {
					loadingSoundProgressBar.visibility = View.INVISIBLE
				}
				Player.STATE_ENDED -> {
					replayButton.visibility = View.VISIBLE
				}
			}
		})
		
		alertViewModel.playerProgress.observe(this, Observer {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
				soundProgressSeekBar?.setProgress(it, true)
			} else {
				soundProgressSeekBar?.progress = it
			}
		})
	}
	
	private fun observeClassifiedCation() {
		alertViewModel.classifiedCation.observe(this, Observer { it ->
			it.success({ confidence ->
				val classificationAdapter = ClassificationAdapter()
				classificationAdapter.onDetectionBoxClick = {
					alertViewModel.seekPlayerTo(it.beginAt)
				}
				classificationAdapter.setClassification(confidence)
				val gridLayoutManager = GridLayoutManager(context, ClassificationAdapter.MAX_SPAN_COUNT)
				gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
					override fun getSpanSize(position: Int): Int {
						return classificationAdapter.lists[position].durationSecond()
					}
				}
				
				classificationRecyclerView.apply {
					setHasFixedSize(false)
					layoutManager = gridLayoutManager
					adapter = classificationAdapter
				}
			}, {
				// Handle error if need
			})
			
		})
	}
	
	private fun observeReveiewEvent() {
		alertViewModel.reviewEvent.observe(this, Observer {
			it.success(
					{
						hideLoading()
					},
					{
						hideLoading()
						// TODO error handle
					},
					{
						showLoading()
					})
		})
	}
	
	companion object {
		const val tag = "AlertBottomDialogFragment"
		private const val BUNDLE_EVENT = "BUNDLE_EVENT"
		
		fun newInstance(event: Event): AlertBottomDialogFragment {
			return AlertBottomDialogFragment().apply {
				arguments = Bundle().apply {
					putParcelable(BUNDLE_EVENT, event)
				}
			}
		}
		
		private const val maxProgress = 100_000
	}
}