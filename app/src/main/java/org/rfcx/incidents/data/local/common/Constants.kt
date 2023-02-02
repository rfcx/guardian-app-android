package org.rfcx.incidents.data.local.common

class Constants {
    companion object {
        val GUARDIAN_ASSEMBLY_CHECKLIST = listOf(
            "Software Update",
            "Classifier Upload",
            "Power Diagnostics",
            "Communication Configuration"
        )
        val GUARDIAN_SETUP_CHECKLIST = listOf(
            "Guardian Registration",
            "Network Signal Test",
            "Audio Parameter Config",
            "Microphone Capture Test",
            "Storage Inspector",
            "Guardian Installation Location"
        )
        val GUARDIAN_OPTIONAL_CHECKLIST = listOf(
            "Add Photo(s)",
            "Confirm CheckIn Status"
        )
    }
}
