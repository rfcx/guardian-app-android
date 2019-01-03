package org.rfcx.ranger.view

import android.animation.Animator
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_fragment_report_success.*
import org.rfcx.ranger.R

class ReportSuccessDialogFragment : DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_fragment_report_success, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        successAnimateView.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationEnd(p0: Animator?) {
                Handler().postDelayed({
                    dismissAllowingStateLoss()
                }, 200)
            }
            override fun onAnimationCancel(p0: Animator?) {}
            override fun onAnimationStart(p0: Animator?) {}
            override fun onAnimationRepeat(p0: Animator?) {}
        })
    }

    override fun onDestroy() {
        successAnimateView?.removeAllAnimatorListeners()
        super.onDestroy()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.window?.attributes?.windowAnimations = R.style.DialogAnim
    }
}
	
