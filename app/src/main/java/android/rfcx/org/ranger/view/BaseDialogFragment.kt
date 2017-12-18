package android.rfcx.org.ranger.view

import android.app.ProgressDialog
import android.rfcx.org.ranger.R
import android.support.v4.app.DialogFragment

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