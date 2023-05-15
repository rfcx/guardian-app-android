package org.rfcx.incidents.view.guardian.checklist.site

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.R
import org.rfcx.incidents.databinding.FragmentGuardianSiteSelectBinding
import org.rfcx.incidents.entity.stream.Stream
import org.rfcx.incidents.view.guardian.GuardianDeploymentEventListener

class GuardianSiteSelectFragment :
    Fragment(),
    (Stream, Boolean) -> Unit {

    private lateinit var binding: FragmentGuardianSiteSelectBinding
    private val viewModel: GuardianSiteSelectViewModel by viewModel()

    private var mainEvent: GuardianDeploymentEventListener? = null

    // Adapter
    private val existedSiteAdapter by lazy { SiteAdapter(this) }
    private var sitesAdapter = listOf<SiteWithDistanceItem>()

    private var searchItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainEvent = context as GuardianDeploymentEventListener
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_guardian_site_select, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainEvent?.let {
            it.showToolbar()
            it.hideThreeDots()
            it.setToolbarTitle(getString(R.string.guardian_site_title))
        }

        setEditText()
        setupAdapter()
        collectState()
        setEditText()
    }

    private fun showKeyboard() {
        binding.layoutSearchView.searchLayoutSearchEditText.requestFocus()
        val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(binding.layoutSearchView.searchLayoutSearchEditText, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard() {
        val inputManager =
            context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(binding.layoutSearchView.searchLayoutSearchEditText.windowToken, 0)
    }

    private fun setEditText() {
        showKeyboard()
        binding.layoutSearchView.searchViewActionRightButton.visibility = View.VISIBLE
        binding.layoutSearchView.searchLayoutSearchEditText.hint = getString(R.string.search_or_create_box_hint)

        binding.layoutSearchView.searchViewActionRightButton.setOnClickListener {
            if (binding.layoutSearchView.searchLayoutSearchEditText.text.isNullOrBlank()) {
                hideKeyboard()
            } else {
                binding.layoutSearchView.searchLayoutSearchEditText.text = null
            }
        }

        binding.layoutSearchView.searchLayoutSearchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                searchItem?.isVisible = s?.length == 0
                if (s?.length == 0) {
                    existedSiteAdapter.isNewSite = false
                    existedSiteAdapter.items = sitesAdapter
                } else {
                    val text = s.toString().lowercase()
                    binding.noResultFound.visibility = View.GONE
                    val createNew = arrayListOf(
                        SiteWithDistanceItem(
                            Stream(
                                id = -1,
                                name = s.toString(),
                                latitude = 0.0,
                                longitude = 0.0
                            ),
                            0F
                        )
                    )
                    val filteredNameSite = sitesAdapter.filter {
                        it.stream.name.lowercase().contains(text)
                    }
                    existedSiteAdapter.setFilter(ArrayList(createNew + filteredNameSite))
                    existedSiteAdapter.isNewSite = true
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupAdapter() {
        binding.existedRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = existedSiteAdapter
        }
        existedSiteAdapter.isNewSite = false
        existedSiteAdapter.items = listOf()
    }

    private fun collectState() {
        lifecycleScope.launch {
            viewModel.streams.collectLatest {
                sitesAdapter = it
                existedSiteAdapter.items = it
            }
        }
    }

    override fun invoke(site: Stream, isNewSite: Boolean) {
        mainEvent?.goToSiteSetScreen(site,isNewSite)
    }

    companion object {
        fun newInstance() = GuardianSiteSelectFragment()
    }
}
