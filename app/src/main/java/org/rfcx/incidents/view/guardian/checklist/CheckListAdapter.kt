package org.rfcx.incidents.view.guardian.checklist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import org.rfcx.incidents.R
import org.rfcx.incidents.databinding.ItemChecklistHeaderBinding
import org.rfcx.incidents.databinding.ItemChecklistStepBinding

class CheckListAdapter(private val onCheckClickListener: (Int, String) -> Unit) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var headerBinding: ItemChecklistHeaderBinding
    private lateinit var stepBinding: ItemChecklistStepBinding
    private var listOfChecks = listOf<CheckListItem>()

    companion object {
        const val CHECK_ITEM = 1
        const val HEADER_ITEM = 2
    }

    fun setCheckList(checks: List<CheckListItem>) {
        listOfChecks = checks
        notifyDataSetChanged()
    }

    fun setCheckPassed(number: Int) {
        listOfChecks.filterIsInstance<CheckListItem.CheckItem>()
            .find { it.number == number }?.isPassed = true
        notifyDataSetChanged()
    }

    fun setCheckUnPassed(number: Int) {
        listOfChecks.filterIsInstance<CheckListItem.CheckItem>()
            .find { it.number == number }?.isPassed = false
        notifyDataSetChanged()
    }

    fun isEveryCheckListPassed(): Boolean {
        return listOfChecks.filterIsInstance<CheckListItem.CheckItem>().filter { it.isRequired }.all { it.isPassed }
    }

    override fun getItemViewType(position: Int): Int {
        return when (listOfChecks[position]) {
            is CheckListItem.CheckItem -> CHECK_ITEM
            else -> HEADER_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            CHECK_ITEM -> {
                stepBinding = ItemChecklistStepBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                CheckItemViewHolder(stepBinding)
            }
            else -> {
                headerBinding = ItemChecklistHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                HeaderItemViewHolder(headerBinding)
            }
        }
    }

    override fun getItemCount(): Int = listOfChecks.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            CHECK_ITEM -> (holder as CheckItemViewHolder).bind(listOfChecks[position] as CheckListItem.CheckItem)
            else -> (holder as HeaderItemViewHolder).bind(listOfChecks[position] as CheckListItem.Header)
        }
    }

    inner class CheckItemViewHolder(private val binding: ItemChecklistStepBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(check: CheckListItem.CheckItem) {
            binding.checkName.text = check.name

            binding.checkName.setOnClickListener {
                onCheckClickListener(check.number, check.name)
            }

            if (check.isPassed) {
                binding.checkName.setTextColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.colorPrimary
                    )
                )
                binding.checkName.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_checklist_passed,
                    0,
                    0,
                    0
                )
            } else {
                binding.checkName.setTextColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.text_primary
                    )
                )
                binding.checkName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_checklist, 0, 0, 0)
            }
        }
    }

    inner class HeaderItemViewHolder(private val binding: ItemChecklistHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(header: CheckListItem.Header) {
            binding.checkHeader.text = header.name
        }
    }
}

sealed class CheckListItem {
    data class CheckItem(val number: Int, val name: String, var isPassed: Boolean = false, var isRequired: Boolean = true) : CheckListItem()
    data class Header(val name: String) : CheckListItem()
}
