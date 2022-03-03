package org.rfcx.incidents.view.login

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.R
import org.rfcx.incidents.data.preferences.Preferences
import org.rfcx.incidents.data.remote.common.success
import org.rfcx.incidents.databinding.FragmentLoginBinding
import org.rfcx.incidents.util.Analytics
import org.rfcx.incidents.util.Screen
import org.rfcx.incidents.util.isValidEmail
import org.rfcx.incidents.view.base.BaseFragment

class LoginFragment : BaseFragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    lateinit var listener: LoginListener
    private val loginViewModel: LoginViewModel by viewModel()

    private val analytics by lazy { context?.let { Analytics(it) } }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = (context as LoginListener)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        setupObserver()
    }

    override fun onResume() {
        super.onResume()
        loading(false)
        analytics?.trackScreen(Screen.LOGIN)
    }

    private fun initView() {
        binding.signInButton.setOnClickListener {
            analytics?.trackLoginEvent("email")
            val email = binding.loginEmailEditText.text.toString()
            val password = binding.loginPasswordEditText.text.toString()
            it.hideKeyboard()

            if (validateInput(email, password)) {
                loading()
                loginViewModel.login(email, password)
            }
        }

        binding.forgotYourPasswordTextView.setOnClickListener {
            alertDialogResetPassword()
        }
    }

    private fun alertDialogResetPassword() {

        val builder = context?.let { AlertDialog.Builder(it) }
        val inflater = LayoutInflater.from(activity)
        val view = inflater.inflate(R.layout.reset_password_dialog, null)
        val editText = view.findViewById(R.id.emailResetPasswordEditText) as EditText
        val errorEmailFormat = view.findViewById(R.id.errorEmailFormatTextView) as TextView

        if (builder != null) {
            builder.setTitle(getString(R.string.reset_password))
            builder.setMessage(R.string.enter_email)
            builder.setView(view)
            builder.setCancelable(false)

            builder.setPositiveButton(getString(R.string.reset)) { _, _ ->
                loading()
                val email = editText.text.toString()
                loginViewModel.resetPassword(email)
            }

            builder.setNegativeButton(getString(R.string.cancel)) { _, _ ->
                view.hideKeyboard()
            }

            val alertDialog = builder.create()
            alertDialog.show()

            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
            editText.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = s?.length != 0
                    if (s?.length != 0) {
                        if (s.toString().isValidEmail()) {
                            errorEmailFormat.visibility = View.INVISIBLE
                        } else {
                            errorEmailFormat.visibility = View.VISIBLE
                        }
                    } else {
                        errorEmailFormat.visibility = View.INVISIBLE
                    }
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        }
    }

    private fun loading(start: Boolean = true) {
        binding.loginGroupView.visibility = if (start) View.GONE else View.VISIBLE
        binding.loginProgressBar.visibility = if (start) View.VISIBLE else View.GONE
    }

    private fun setupObserver() {
        loginViewModel.userAuth.observe(
            viewLifecycleOwner,
            Observer {
                loading()
                it ?: return@Observer
                loginViewModel.checkUserDetail(it)
            }
        )

        loginViewModel.loginFailure.observe(
            viewLifecycleOwner,
            Observer { errorMessage ->
                if ((errorMessage != null) && errorMessage.isNotEmpty()) {
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                }
                loading(false)
            }
        )

        loginViewModel.statusUserTouch.observe(
            viewLifecycleOwner,
            Observer { status ->
                if (status !== null && status) {
                    loginViewModel.fetchProjects()
                } else {
                    loading(false)
                }
            }
        )

        loginViewModel.projects.observe(viewLifecycleOwner) { result ->
            result.success({ projects ->
                if (projects.size == 1) {
                    loginViewModel.subscribe(projects[0]) { status ->
                        if (status) {
                            val preferences = Preferences.getInstance(requireContext())
                            val projectId = projects[0].id
                            preferences.putString(Preferences.SELECTED_PROJECT, projectId)
                            saveSubscribedProject(arrayListOf(projects[0].id))

                            listener.openMain()
                        } else {
                            listener.handleOpenPage()
                        }
                    }
                } else {
                    listener.handleOpenPage()
                }
            }, {
                listener.handleOpenPage()
            }, {})
        }

        loginViewModel.resetPassword.observe(
            viewLifecycleOwner,
            Observer { str ->
                if (str == SUCCESS) {
                    loading(false)
                    Toast.makeText(context, getString(R.string.reset_link_send), Toast.LENGTH_LONG).show()
                } else {
                    loading(false)
                    Toast.makeText(context, str, Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    private fun validateInput(email: String?, password: String?): Boolean {
        if (email.isNullOrEmpty()) {
            binding.loginEmailEditText.error = getString(R.string.pls_fill_email)
            return false
        } else if (password.isNullOrEmpty()) {
            binding.loginPasswordEditText.error = getString(R.string.pls_fill_password)
            return false
        }
        return true
    }

    private fun View.hideKeyboard() = this.let {
        val inputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(windowToken, 0)
    }

    private fun saveSubscribedProject(subscribedProjects: ArrayList<String>) {
        val preferenceHelper = Preferences.getInstance(requireContext())
        preferenceHelper.remove(Preferences.SUBSCRIBED_PROJECTS)
        preferenceHelper.putArrayList(Preferences.SUBSCRIBED_PROJECTS, subscribedProjects)
    }

    companion object {
        const val SUCCESS = "SUCCESS"
    }
}
