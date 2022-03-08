package org.rfcx.incidents.view.report.detail

import android.content.Context
import org.rfcx.incidents.R
import org.rfcx.incidents.entity.response.Actions
import org.rfcx.incidents.entity.response.EvidenceTypes
import org.rfcx.incidents.entity.response.PoachingEvidence

fun Int.getAnswerItem(context: Context): String? {
    return when {
        // EvidenceTypes
        this == EvidenceTypes.NONE.value -> context.getString(R.string.common_none)
        this == EvidenceTypes.CUT_DOWN_TREES.value -> context.getString(R.string.cut_down_trees)
        this == EvidenceTypes.CLEARED_AREAS.value -> context.getString(R.string.cleared_areas)
        this == EvidenceTypes.LOGGING_EQUIPMENT.value -> context.getString(R.string.logging_equipment)
        this == EvidenceTypes.LOGGERS_AT_SITE.value -> context.getString(R.string.loggers_at_site)
        this == EvidenceTypes.ILLEGAL_CAMPS.value -> context.getString(R.string.illegal_camps)
        this == EvidenceTypes.FIRED_BURNED_AREAS.value -> context.getString(R.string.fires_burned_areas)
        this == EvidenceTypes.OTHER.value -> context.getString(R.string.other_text)

        // Actions
        this == Actions.NONE.value -> context.getString(R.string.common_none)
        this == Actions.COLLECTED_EVIDENCE.value -> context.getString(R.string.collected_evidence)
        this == Actions.ISSUE_A_WARNING.value -> context.getString(R.string.issue_a_warning)
        this == Actions.CONFISCATED_EQUIPMENT.value -> context.getString(R.string.confiscated_equipment)
        this == Actions.OTHER.value -> context.getString(R.string.other_text)
        this == Actions.DAMAGED_MACHINERY.value -> context.getString(R.string.damaged_machinery)

        // PoachingEvidence
        this == PoachingEvidence.NONE.value -> context.getString(R.string.common_none)
        this == PoachingEvidence.BULLET_SHELLS.value -> context.getString(R.string.bullet_shells)
        this == PoachingEvidence.FOOTPRINTS.value -> context.getString(R.string.footprints)
        this == PoachingEvidence.DOG_TRACKS.value -> context.getString(R.string.dog_tracks)
        this == PoachingEvidence.OTHER.value -> context.getString(R.string.other_text)
        else -> ""
    }
}
