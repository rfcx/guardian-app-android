package org.rfcx.incidents.view.report.create

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import org.rfcx.incidents.R
import org.rfcx.incidents.databinding.FragmentDamageBinding
import org.rfcx.incidents.entity.response.DamageScale
import org.rfcx.incidents.util.Analytics
import org.rfcx.incidents.util.Screen

class DamageFragment : Fragment() {
    private var _binding: FragmentDamageBinding? = null
    private val binding get() = _binding!!
    lateinit var listener: CreateReportListener
    var selected: Int? = null
    private val analytics by lazy { context?.let { Analytics(it) } }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = (context as CreateReportListener)
    }

    override fun onResume() {
        super.onResume()
        analytics?.trackScreen(Screen.DAMAGE)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDamageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.nextStepButton.setOnClickListener {
            selected?.let { value ->
                listener.setDamage(value)
                listener.handleCheckClicked(StepCreateReport.ACTION.step)
            }
        }
        binding.largeAreaImageView.setOnClickListener {
            selected = DamageScale.LARGE.value
            setOnSelect(it)
        }
        binding.mediumTreesImageView.setOnClickListener {
            selected = DamageScale.MEDIUM.value
            setOnSelect(it)
        }
        binding.smallNumberImageView.setOnClickListener {
            selected = DamageScale.SMALL.value
            setOnSelect(it)
        }
        binding.noVisibleImageView.setOnClickListener {
            selected = DamageScale.NO_VISIBLE.value
            setOnSelect(it)
        }
        setupDamageScale()
    }

    private fun setupDamageScale() {
        val response = listener.getResponse()
        response?.let { res ->
            selected = res.damageScale
            binding.nextStepButton.isEnabled = res.damageScale != DamageScale.DEFAULT.value

            if (selected == DamageScale.LARGE.value) binding.largeAreaImageView.setBackgroundSelected() else binding.largeAreaImageView.setBackgroundNoSelect()
            if (selected == DamageScale.MEDIUM.value) binding.mediumTreesImageView.setBackgroundSelected() else binding.mediumTreesImageView.setBackgroundNoSelect()
            if (selected == DamageScale.SMALL.value) binding.smallNumberImageView.setBackgroundSelected() else binding.smallNumberImageView.setBackgroundNoSelect()
            if (selected == DamageScale.NO_VISIBLE.value) binding.noVisibleImageView.setBackgroundSelected() else binding.noVisibleImageView.setBackgroundNoSelect()
        }
    }

    private fun setOnSelect(selected: View) {
        binding.nextStepButton.isEnabled = true

        if (selected == binding.largeAreaImageView) binding.largeAreaImageView.setBackgroundSelected() else binding.largeAreaImageView.setBackgroundNoSelect()
        if (selected == binding.mediumTreesImageView) binding.mediumTreesImageView.setBackgroundSelected() else binding.mediumTreesImageView.setBackgroundNoSelect()
        if (selected == binding.smallNumberImageView) binding.smallNumberImageView.setBackgroundSelected() else binding.smallNumberImageView.setBackgroundNoSelect()
        if (selected == binding.noVisibleImageView) binding.noVisibleImageView.setBackgroundSelected() else binding.noVisibleImageView.setBackgroundNoSelect()
    }

    private fun ImageView.setBackgroundSelected() {
        this.setBackgroundResource(R.drawable.bg_selected)
    }

    private fun ImageView.setBackgroundNoSelect() {
        this.setBackgroundResource(R.drawable.bg_circle_white)
    }

    companion object {
        @JvmStatic
        fun newInstance() = DamageFragment()
    }
}
