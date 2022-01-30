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
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.R
import org.rfcx.incidents.databinding.FragmentSetUserNameBinding
import org.rfcx.incidents.util.Analytics
import org.rfcx.incidents.util.Screen
import org.rfcx.incidents.view.base.BaseFragment

class SetUserNameFragment : BaseFragment() {
    private var _binding: FragmentSetUserNameBinding? = null
    private val binding get() = _binding!!

    lateinit var listener: LoginListener
    private val setUserNameViewModel: SetUserNameViewModel by viewModel()
    private val analytics by lazy { context?.let { Analytics(it) } }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = (context as LoginListener)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSetUserNameBinding.inflate(inflater, container, false)
        return binding.root
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
        binding.inputNameEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (p0 != null) {
                    if (p0.isEmpty()) {
                        binding.submitButton.isEnabled = false
                    }
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.submitButton.isEnabled = true
            }
        })

        binding.submitButton.setOnClickListener {
            it.hideKeyboard()
            binding.submitButton.isEnabled = false
            binding.setNameProgressBar.visibility = View.VISIBLE
            val name = binding.inputNameEditText.text.toString()
            setUserNameViewModel.sendName(name)

            setUserNameViewModel.status.observe(
                viewLifecycleOwner
            ) { status ->
                if (status) {
                    analytics?.trackSetUsernameEvent()
                    listener.handleOpenPage()
                } else {
                    Toast.makeText(context, R.string.something_is_wrong, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun View.hideKeyboard() = this.let {
        val inputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(windowToken, 0)
    }
}
