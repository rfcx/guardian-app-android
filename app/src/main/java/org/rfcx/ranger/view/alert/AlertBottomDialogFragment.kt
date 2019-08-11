package org.rfcx.ranger.view.alert

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_dialog_alert.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.ranger.R
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
		bindEventView()
	}
	
	private fun setupView() {
		soundProgressSeekBar.isEnabled = false
		soundProgressSeekBar.max = maxProgress
	}
	
	@SuppressLint("SetTextI18n")
	private fun bindEventView() {
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
		
		private const val delayTime = 100L
		private const val maxProgress = 100_000
	}
}