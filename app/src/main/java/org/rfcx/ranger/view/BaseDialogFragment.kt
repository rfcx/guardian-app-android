package org.rfcx.ranger.view

import android.app.ProgressDialog
import org.rfcx.ranger.R
import androidx.fragment.app.DialogFragment

/**
 * Created by Jingjoeh on 12/18/2017 AD.
 */
open class BaseDialogFragment : DialogFragment() {

    private var progressDialog: ProgressDialog? = null

    protected fun showProgress() {
        if (progressDialog == null || !progressDialog!!.isShowing()) {
            progressDialog = ProgressDialog(context, R.style.ProgressDialogTheme)
            progressDialog!!.setCancelable(false)
            progressDialog!!.setProgressStyle(android.R.style.Widget_ProgressBar_Small)
            progressDialog!!.show()
        }
    }

    protected fun hideProgress() {
        if (progressDialog != null) {
            if (progressDialog!!.isShowing) {
                progressDialog!!.dismiss()
            }
        }
        progressDialog = null
    }
}