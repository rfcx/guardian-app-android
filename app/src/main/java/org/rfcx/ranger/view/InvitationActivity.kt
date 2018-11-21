package org.rfcx.ranger.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.rfcx.ranger.R

class InvitationActivity : AppCompatActivity() {
    // widget view
    lateinit var inputInvitationCode : EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invitation)
        setupWidgetView()
    }

    private fun setupWidgetView() {
        inputInvitationCode = findViewById(R.id.input_invitation_code)
        findViewById<FloatingActionButton>(R.id.button_next).setOnClickListener {
            // TODO: getInput
            Toast.makeText(this, inputInvitationCode.text.toString(), Toast.LENGTH_SHORT).show()
        }
    }
}
