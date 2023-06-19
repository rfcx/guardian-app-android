package org.rfcx.incidents.view.profile.guardian

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.rfcx.incidents.databinding.ItemRegistrationBinding
import org.rfcx.incidents.entity.guardian.registration.GuardianRegistration
import org.rfcx.incidents.entity.response.SyncState

class UnsentGuardianRegistrationAdapter(private val listener: RegisterButtonListener) :
    RecyclerView.Adapter<UnsentGuardianRegistrationAdapter.RegistrationViewHolder>() {

    private lateinit var binding: ItemRegistrationBinding

    var registrations: List<GuardianRegistration> = listOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): RegistrationViewHolder {
        binding = ItemRegistrationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RegistrationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RegistrationViewHolder, position: Int) {
        holder.bind(registrations[position])
    }

    override fun getItemCount(): Int = registrations.size

    inner class RegistrationViewHolder(binding: ItemRegistrationBinding) : RecyclerView.ViewHolder(binding.root) {
        private val guid = binding.guidTextView
        val registerButton = binding.registerButton
        private val registeredText = binding.registeredText
        private val loading = binding.registerLoading

        fun bind(registration: GuardianRegistration) {
            guid.text = registration.guid

            when(registration.syncState) {
                SyncState.UNSENT.value -> {
                    loading.visibility = View.GONE
                    registerButton.visibility = View.VISIBLE
                    registeredText.visibility = View.GONE
                }
                SyncState.SENDING.value -> {
                    loading.visibility = View.VISIBLE
                    registerButton.visibility = View.INVISIBLE
                    registeredText.visibility = View.GONE
                }
                SyncState.SENT.value -> {
                    loading.visibility = View.GONE
                    registerButton.visibility = View.INVISIBLE
                    registeredText.visibility = View.VISIBLE
                }
            }

            registerButton.setOnClickListener {
                listener.onClicked(registration)
            }
        }
    }
}

interface RegisterButtonListener {
    fun onClicked(registration: GuardianRegistration)
}
