package org.rfcx.ranger.view.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_guardian_group.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.ranger.R
import org.rfcx.ranger.data.remote.success
import org.rfcx.ranger.entity.guardian.GuardianGroup
import org.rfcx.ranger.util.CloudMessaging
import org.rfcx.ranger.util.Preferences
import org.rfcx.ranger.util.Preferences.Companion.SELECTED_GUARDIAN_GROUP
import org.rfcx.ranger.util.handleError
import org.rfcx.ranger.view.base.BaseActivity


class GuardianGroupActivity : BaseActivity() {
	private val viewModel: GuardianGroupViewModel by viewModel()
	private val guardianGroupAdapter by lazy { GuardianGroupAdapter() }
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_guardian_group)
		
		setupToolbar()
		
		// setup list
		guardianGroupRecycler.apply {
			layoutManager = LinearLayoutManager(this@GuardianGroupActivity)
			adapter = guardianGroupAdapter
		}
		
		viewModel.items.observe(this, Observer { it ->
			it.success({
				// Success block
				loadingProgress.visibility = View.INVISIBLE
				guardianGroupAdapter.items = it
				
			}, {
				loadingProgress.visibility = View.INVISIBLE
				this@GuardianGroupActivity.handleError(it)
			}, {
				// Loading block
				loadingProgress.visibility = View.VISIBLE
			})
		})
		
		guardianGroupAdapter.mOnItemClickListener = object : OnItemClickListener {
			override fun onItemClick(guardianGroup: GuardianGroup) {
				val preferenceHelper = Preferences.getInstance(this@GuardianGroupActivity)
				if (preferenceHelper.getString(SELECTED_GUARDIAN_GROUP) != guardianGroup.shortname) {
					CloudMessaging.unsubscribe(this@GuardianGroupActivity)
					preferenceHelper.putString(SELECTED_GUARDIAN_GROUP, guardianGroup.shortname)
					CloudMessaging.subscribeIfRequired(this@GuardianGroupActivity)
				}
				finish()
			}
		}
	}
	
	private fun setupToolbar() {
		setSupportActionBar(toolbar)
		supportActionBar?.apply {
			setDisplayHomeAsUpEnabled(true)
			setDisplayShowHomeEnabled(true)
			elevation = 0f
			title = getString(R.string.guardian_group_list)
		}
	}
	
	override fun onSupportNavigateUp(): Boolean {
		onBackPressed()
		return true
	}
	
	companion object {
		fun startActivity(context: Context) {
			val intent = Intent(context, GuardianGroupActivity::class.java)
			context.startActivity(intent)
		}
	}
}

interface OnItemClickListener {
	fun onItemClick(guardianGroup: GuardianGroup)
}