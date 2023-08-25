package org.rfcx.incidents.view.events

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import org.rfcx.incidents.databinding.ItemClusterBinding

class MapMarkerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding by lazy { ItemClusterBinding.inflate(LayoutInflater.from(context)) }

    init {
        addView(binding.root)
    }

    fun setContent(number: String, idDrawable: Int) {
        binding.numberText.text = number
        binding.bgView.setImageDrawable(ContextCompat.getDrawable(context, idDrawable))
    }
}
