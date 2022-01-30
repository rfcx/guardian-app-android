package org.rfcx.incidents.view.login

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_set_user_name.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.R
import org.rfcx.incidents.util.Analytics
import org.rfcx.incidents.util.Screen
import org.rfcx.incidents.view.base.BaseFragment

class SetUserNameFragment : BaseFragment() {
    
    lateinit var listener: LoginListener
    private val setUserNameViewModel: SetUserNameViewModel by viewModel()
    private val analytics by lazy { context?.let { Analytics(it) } }
    
    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = (context as LoginListener)
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_set_user_name, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }
    
    override fun onResume() {
        super.onResume()
        analytics?.trackScreen(Screen.USERNAME)
    }
    
    private fun initView() {
        inputNameEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (p0 != null) {
                    if (p0.isEmpty()) {
                        submitButton.isEnabled = false
                    }
                }
            }
            
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                submitButton.isEnabled = true
            }
        })
        
        submitButton.setOnClickListener {
            it.hideKeyboard()
            submitButton.isEnabled = false
            setNameProgressBar.visibility = View.VISIBLE
            val name = inputNameEditText.text.toString()
            setUserNameViewModel.sendName(name)
            
            setUserNameViewModel.status.observe(this, Observer { status ->
                if (status) {
                    analytics?.trackSetUsernameEvent()
                    listener.handleOpenPage()
                } else {
                    Toast.makeText(context, R.string.something_is_wrong, Toast.LENGTH_LONG).show()
                }
            })
        }
    }
    
    private fun View.hideKeyboard() = this.let {
        val inputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(windowToken, 0)
    }
}
