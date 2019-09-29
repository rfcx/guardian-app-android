package org.rfcx.ranger.view.base

import androidx.fragment.app.Fragment


abstract class BaseFragment : Fragment() {
	protected fun isSafe(): Boolean {
		return !(this.isRemoving || this.activity == null || this.isDetached || !this.isAdded || this.view == null)
	}
}