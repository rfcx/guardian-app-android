package org.rfcx.ranger.view.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_invitation_code.*
import org.rfcx.ranger.R
import org.rfcx.ranger.view.base.BaseFragment

class InvitationCodeFragment: BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_invitation_code, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        submitButton.setOnClickListener {
            val code = inputCodeEditText.text.toString()

            // TODO doSubmit
        }
    }
}