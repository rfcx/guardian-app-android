package org.rfcx.ranger.view.base

import com.google.android.material.bottomsheet.BottomSheetDialogFragment

abstract class BaseBottomSheetDialogFragment : BottomSheetDialogFragment() {
	
	private val loadingDialogTag = "LoadingDialog"
	
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
	
	protected fun dismissDialog() {
		try {
			dismiss()
		} catch (e: Exception) {
			e.printStackTrace()
			dismissAllowingStateLoss()
		}
	}
	
}