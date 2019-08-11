package org.rfcx.ranger.view.base

import com.google.android.material.bottomsheet.BottomSheetDialogFragment

abstract class BaseBottomSheetDialogFragment : BottomSheetDialogFragment() {
	
	private val loadingDialogTag = "LoadingDialog"
	
	protected fun showLoading() {
		val loading: LoadingFragment = childFragmentManager.findFragmentByTag(loadingDialogTag) as LoadingFragment?
				?: run {
					LoadingFragment()
				}
		loading.show(childFragmentManager, loadingDialogTag)
	}
	
	protected fun hideLoading() {
		val loading: LoadingFragment? = childFragmentManager.findFragmentByTag(loadingDialogTag) as LoadingFragment?
		loading?.dismissDialog()
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