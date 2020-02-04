package org.rfcx.ranger.view.alert

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.exoplayer2.Player
import kotlinx.android.synthetic.main.fragment_dialog_alert.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.ranger.R
import org.rfcx.ranger.adapter.classifycation.ClassificationAdapter
import org.rfcx.ranger.data.remote.success
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.entity.event.ReviewEventFactory
import org.rfcx.ranger.util.*
import org.rfcx.ranger.view.base.BaseBottomSheetDialogFragment


class AlertBottomDialogFragment : BaseBottomSheetDialogFragment() {
	
	private val alertViewModel: AlertBottomDialogViewModel by viewModel()
	private val analytics by lazy { context?.let { Analytics(it) } }
	
	private var alertListener: AlertListener? = null
	
	override fun onAttach(context: Context) {
		super.onAttach(context)
		if (parentFragment is AlertListener) {
			alertListener = parentFragment as AlertListener
		} else {
			/*throw IllegalStateException("Parent Fragment ${(parentFragment as Fragment).javaClass.simpleName} " +
					"not implemented @AlertListener")*/
		}
	}
	
	private var event: Event? = null
	
	override fun onDetach() {
		super.onDetach()
		alertListener = null
	}
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		
		val eventGuId: String? = arguments?.getString(BUNDLE_EVENT)
		if (eventGuId == null) {
			dismissDialog()
		} else {
			alertViewModel.setEventGuid(eventGuId)
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
		observeReviewEvent()
	}
	
	private fun setupView() {
		soundProgressSeekBar.isEnabled = false
		soundProgressSeekBar.max = maxProgress
		
		replayButton.setOnClickListener {
			alertViewModel.replaySound()
		}
		
	}
	
	private fun initReviewButtonClick() {
		negativeButton.setOnClickListener {
			if (alertViewModel.eventState.value == EventState.NONE) {
				alertViewModel.reviewEvent(false)
				alertViewModel.eventResult?.let { it1 -> analytics?.trackReviewAlertEvent(it1.id, it1.value.toString(), "0") }
			} else {
				alertViewModel.eventResult?.let { it1 -> analytics?.trackFollowAlertEvent(it1.id, it1.value.toString()) }
				dismissDialog()
			}
		}
		
		positiveButton.setOnClickListener {
			if (alertViewModel.eventState.value == EventState.NONE) {
				alertViewModel.reviewEvent(true)
				alertViewModel.eventResult?.let { it1 -> analytics?.trackReviewAlertEvent(it1.id, it1.value.toString(), "1") }
			} else {
				alertViewModel.eventResult?.let { event ->
					val gmmIntentUri = Uri.parse("geo:<${event.latitude}>,<${event.longitude}>" +
							"?q=<${event.latitude}>,<${event.longitude}>")
					val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
					mapIntent.setPackage("com.google.android.apps.maps")
					context?.let {
						if (mapIntent.resolveActivity(it.packageManager) != null) {
							startActivity(mapIntent)
						}
					}
				}
			}
		}
	}
	
	@SuppressLint("SetTextI18n", "DefaultLocale")
	private fun observeEventView() {
		alertViewModel.event.observe(this, Observer { it ->
			it.success({
				event = it
				val state: String = arguments?.getString(STATE_EVENT) ?: "NONE"
				
				eventIconImageView.setImageResource(it.getIconRes())
				guardianNameTextView.text = it.guardianName.capitalize()
				timeTextView.text = "  ${context?.let { it1 -> it.beginsAt.toTimeSinceStringAlternativeTimeAgo(it1) }}"
				reviewedTextView.text = context?.getString(if (it.firstNameReviewer.isNotBlank()) R.string.last_reviewed_by else R.string.not_have_review)
						?: ""
				nameReviewerTextView.text = it.firstNameReviewer
				nameReviewerTextView.visibility = if (it.firstNameReviewer.isNotBlank()) View.VISIBLE else View.INVISIBLE
				linearLayout.visibility = View.INVISIBLE
				agreeTextView.text = it.confirmedCount.toString()
				rejectTextView.text = it.rejectedCount.toString()
				
				if (state == "CONFIRM") {
					linearLayout.visibility = View.VISIBLE
					
					if (context !== null) {
						agreeImageView.background = context!!.getImage(R.drawable.bg_circle_red)
						agreeImageView.setImageDrawable(context!!.getImage(R.drawable.ic_confirm_event_white))
					}
				} else if (state == "REJECT") {
					linearLayout.visibility = View.VISIBLE
					
					if (context !== null) {
						rejectImageView.background = context!!.getImage(R.drawable.bg_circle_grey)
						rejectImageView.setImageDrawable(context!!.getImage(R.drawable.ic_reject_event_white))
					}
				}
				
				initReviewButtonClick()
			}, {
				context?.handleError(it)
				dismissDialog()
			}, {
				loadingSoundProgressBar.visibility = View.VISIBLE
			})
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
					negativeButton.apply {
						text = getString(R.string.reject_text)
						setCompoundDrawablesWithIntrinsicBounds(
								R.drawable.ic_reject_event_white, 0, 0, 0)
					}
					
					positiveButton.apply {
						text = getString(R.string.confirm_text)
						setCompoundDrawablesWithIntrinsicBounds(
								R.drawable.ic_confirm_event_white, 0, 0, 0)
					}
				}
				EventState.REVIEWED -> {
					negativeButton.apply {
						setPadding(0, 0, 0, 0)
						text = getString(R.string.follow_up_later_button)
						setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
					}
					
					positiveButton.apply {
						text = getString(R.string.open_map_button)
						setCompoundDrawablesWithIntrinsicBounds(
								R.drawable.ic_directions_white_24dp, 0, 0, 0)
					}
					
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
	
	private fun observeReviewEvent() {
		alertViewModel.reviewEvent.observe(this, Observer { it ->
			it.success(
					{
						hideLoading()
						event?.let { it1 -> alertListener?.onReviewed(it.reviewConfirm, it1) }
						
						// is rejectEvent?
						if (it.reviewConfirm == ReviewEventFactory.rejectEvent) {
							dismiss()
						}
						showCountReviewer(it)
					},
					{
						hideLoading()
						Toast.makeText(context, R.string.error_common, Toast.LENGTH_SHORT).show()
						
					},
					{
						showLoading()
					})
		})
	}
	
	private fun showCountReviewer(reviewEventFactory: ReviewEventFactory) {
		linearLayout.visibility = View.VISIBLE
		nameReviewerTextView.visibility = View.VISIBLE
		
		reviewedTextView.text = context?.getString(R.string.last_reviewed_by)
		nameReviewerTextView.text = context.getNameEmail()
		
		if (reviewEventFactory.reviewConfirm == ReviewEventFactory.confirmEvent) {
			agreeImageView.background = context?.getImage(R.drawable.bg_circle_red)
			agreeImageView.setImageDrawable(context?.getImage(R.drawable.ic_confirm_event_white))
			
			rejectImageView.setImageDrawable(context?.getImage(R.drawable.ic_reject_event_gray))
			context?.getBackgroundColor(R.color.transparent)?.let { rejectImageView.setBackgroundColor(it) }
			
		} else if (reviewEventFactory.reviewConfirm == ReviewEventFactory.rejectEvent) {
			rejectImageView.background = context!!.getImage(R.drawable.bg_circle_grey)
			rejectImageView.setImageDrawable(context!!.getImage(R.drawable.ic_reject_event_white))
			
			agreeImageView.setImageDrawable(context?.getImage(R.drawable.ic_confirm_event_gray))
			context?.getBackgroundColor(R.color.transparent)?.let { agreeImageView.setBackgroundColor(it) }
			
		}
		
		if (event !== null) {
			if (event!!.firstNameReviewer == context.getNameEmail()) {
				if (event!!.reviewConfirmed!!) {
					if (reviewEventFactory.reviewConfirm == ReviewEventFactory.rejectEvent) {
						rejectedCount()
					}
				} else {
					if (reviewEventFactory.reviewConfirm == ReviewEventFactory.confirmEvent) {
						confirmedCount()
					}
				}
			} else {
				if (reviewEventFactory.reviewConfirm == ReviewEventFactory.confirmEvent) {
					confirmedCount()
					
				} else if (reviewEventFactory.reviewConfirm == ReviewEventFactory.rejectEvent) {
					rejectedCount()
					
				}
			}
		}
	}
	
	private fun confirmedCount() {
		agreeTextView.text = (event?.confirmedCount?.plus(1)).toString()
		
		if (event?.firstNameReviewer == context.getNameEmail()) {
			rejectTextView.text = event?.rejectedCount?.minus(1).toString()
		} else {
			rejectTextView.text = event?.rejectedCount.toString()
		}
	}
	
	private fun rejectedCount() {
		rejectTextView.text = (event?.rejectedCount?.plus(1)).toString()
		
		if (event?.firstNameReviewer == context.getNameEmail()) {
			agreeTextView.text = event?.confirmedCount?.minus(1).toString()
		} else {
			agreeTextView.text = event?.confirmedCount.toString()
		}
	}
	
	private fun Context.getImage(res: Int): Drawable? {
		return ContextCompat.getDrawable(this, res)
	}
	
	private fun Context.getBackgroundColor(res: Int): Int {
		return ContextCompat.getColor(this, res)
	}
	
	companion object {
		const val tag = "AlertBottomDialogFragment"
		private const val BUNDLE_EVENT = "BUNDLE_EVENT"
		private const val STATE_EVENT = "STATE_EVENT"
		
		fun newInstance(eventGuId: String, state: EventItem.State): AlertBottomDialogFragment {
			return AlertBottomDialogFragment().apply {
				arguments = Bundle().apply {
					putString(BUNDLE_EVENT, eventGuId)
					putString(STATE_EVENT, state.toString())
				}
			}
		}
		
		private const val maxProgress = 100_000
	}
}