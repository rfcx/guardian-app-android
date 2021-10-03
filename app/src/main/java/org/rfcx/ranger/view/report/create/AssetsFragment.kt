package org.rfcx.ranger.view.report.create

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_assets.*
import org.rfcx.ranger.R
import org.rfcx.ranger.view.report.create.image.BaseImageFragment

class AssetsFragment : BaseImageFragment() {
	
	companion object {
		@JvmStatic
		fun newInstance() = AssetsFragment()
	}
	
	lateinit var listener: CreateReportListener
	
	override fun onAttach(context: Context) {
		super.onAttach(context)
		listener = (context as CreateReportListener)
	}
	
	override fun didAddImages(imagePaths: List<String>) {}
	
	override fun didRemoveImage(imagePath: String) {}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
	                          savedInstanceState: Bundle?): View? {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_assets, container, false)
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		setupImageRecycler()
		view.viewTreeObserver.addOnGlobalLayoutListener { setOnFocusEditText() }
		
		saveDraftButton.setOnClickListener {
			saveAssets()
			listener.onSaveDraftButtonClick()
		}
		
		submitButton.setOnClickListener {
			saveAssets()
			listener.onSubmitButtonClick()
		}
		setupAssets()
	}
	
	private fun setupAssets() {
		val response = listener.getResponse()
		response?.note?.let { note -> noteEditText.setText(note) }
	}
	
	private fun saveAssets() {
		noteEditText.text?.let {
			listener.setNotes(it.toString())
		}
		listener.setImages(reportImageAdapter.getNewAttachImage())
	}
	
	private fun setupImageRecycler() {
		attachImageRecycler.apply {
			adapter = reportImageAdapter
			layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
			setHasFixedSize(true)
		}
		reportImageAdapter.setImages(arrayListOf())
	}
	
	private fun setOnFocusEditText() {
		val screenHeight: Int = view?.rootView?.height ?: 0
		val r = Rect()
		view?.getWindowVisibleDisplayFrame(r)
		val keypadHeight: Int = screenHeight - r.bottom
		if (keypadHeight > screenHeight * 0.15) {
			saveDraftButton.visibility = View.GONE
			submitButton.visibility = View.GONE
		} else {
			if (saveDraftButton != null) {
				saveDraftButton.visibility = View.VISIBLE
			}
			if (submitButton != null) {
				submitButton.visibility = View.VISIBLE
			}
		}
	}
}
