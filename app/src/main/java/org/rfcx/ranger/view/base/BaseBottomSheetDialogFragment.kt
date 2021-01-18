package org.rfcx.ranger.view.base

import android.app.Dialog
import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

abstract class BaseBottomSheetDialogFragment : BottomSheetDialogFragment() {
	
	private val loadingDialogTag = "LoadingDialog"
	private lateinit var dialog : BottomSheetDialog
	
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
		return dialog
	}
	
	//set the behavior here
	fun setFullScreen(){
		dialog.behavior.state = STATE_EXPANDED
	}
	
	protected fun showLoading() {
		val loadingDialog: LoadingDialogFragment = childFragmentManager.findFragmentByTag(loadingDialogTag) as LoadingDialogFragment?
				?: run {
					LoadingDialogFragment()
				}
		loadingDialog.show(childFragmentManager, loadingDialogTag)
	}
	
	protected fun hideLoading() {
		val loadingDialog: LoadingDialogFragment? = childFragmentManager.findFragmentByTag(loadingDialogTag) as LoadingDialogFragment?
		loadingDialog?.dismissDialog()
	}
	
	fun dismissDialog() {
		try {
			dismiss()
		} catch (e: Exception) {
			e.printStackTrace()
			dismissAllowingStateLoss()
		}
	}
	
}
