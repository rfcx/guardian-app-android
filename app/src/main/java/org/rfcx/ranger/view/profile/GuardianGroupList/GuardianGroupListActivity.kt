package org.rfcx.ranger.view.profile.GuardianGroupList

import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import kotlinx.android.synthetic.main.activity_guardian_group_list.*
import kotlinx.android.synthetic.main.fragment_profile.*
import org.rfcx.ranger.R
import org.rfcx.ranger.view.MainActivityNew
import org.rfcx.ranger.view.profile.ProfileFragment
import android.content.Intent as Intent1


class GuardianGroupListActivity : AppCompatActivity() {
	
	val listItems = arrayOf("osa", "romania", "tambopata", "tembe")
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_guardian_group_list)
		
		val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listItems)
		guardianGroupListView.adapter = adapter
		
		guardianGroupListView.setOnItemClickListener { parent, view, position, id ->
			val guardianName: String = listItems[position]
			
			val profileFragment = ProfileFragment()
			Log.d("siteNameTextView", guardianName)
			
			val manager = supportFragmentManager
			val transaction = manager.beginTransaction()
			
			transaction.replace(R.id.guardianGroupListView,profileFragment)
			transaction.addToBackStack(null)

			// Finishing the transition
			transaction.commit()
			
			Toast.makeText(this, "Clicked item : $position",Toast.LENGTH_SHORT).show()
		}
		
	}
	
	
}
