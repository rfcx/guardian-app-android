package org.rfcx.incidents.view.report.detail

import android.content.Context
import org.rfcx.incidents.R
import org.rfcx.incidents.entity.response.*

data class AnswerItem(val text: String, val color: Int)

fun Int.getAnswerItem(context: Context): AnswerItem? {
    return when {
        // LoggingScale
        this == LoggingScale.NONE.value -> {
            AnswerItem(
                context.getString(R.string.logging_scale) + " " + context.getString(R.string.common_none),
                R.color.logging_color
            )
        }
        this == LoggingScale.LARGE.value -> {
            AnswerItem(
                context.getString(R.string.logging_scale) + " " + context.getString(R.string.large_text),
                R.color.logging_color
            )
        }
        this == LoggingScale.SMALL.value -> {
            AnswerItem(
                context.getString(R.string.logging_scale) + " " + context.getString(R.string.small_text),
                R.color.logging_color
            )
        }

        // DamageScale
        this == DamageScale.NO_VISIBLE.value -> {
            AnswerItem(
                context.getString(R.string.damage) + " " + context.getString(R.string.no_visible),
                R.color.damage_color
            )
        }
        this == DamageScale.SMALL.value -> {
            AnswerItem(
                context.getString(R.string.damage) + " " + context.getString(R.string.small_trees_cut_down),
                R.color.damage_color
            )
        }
        this == DamageScale.MEDIUM.value -> {
            AnswerItem(
                context.getString(R.string.damage) + " " + context.getString(R.string.medium_trees_cut_down),
                R.color.damage_color
            )
        }
        this == DamageScale.LARGE.value -> {
            AnswerItem(
                context.getString(R.string.damage) + " " + context.getString(R.string.large_area_clear_cut),
                R.color.damage_color
            )
        }

        // EvidenceTypes
        this == EvidenceTypes.NONE.value -> {
            AnswerItem(context.getString(R.string.common_none), R.color.evidence_color)
        }
        this == EvidenceTypes.CUT_DOWN_TREES.value -> {
            AnswerItem(context.getString(R.string.cut_down_trees), R.color.evidence_color)
        }
        this == EvidenceTypes.CLEARED_AREAS.value -> {
            AnswerItem(context.getString(R.string.cleared_areas), R.color.evidence_color)
        }
        this == EvidenceTypes.LOGGING_EQUIPMENT.value -> {
            AnswerItem(context.getString(R.string.logging_equipment), R.color.evidence_color)
        }
        this == EvidenceTypes.LOGGERS_AT_SITE.value -> {
            AnswerItem(context.getString(R.string.loggers_at_site), R.color.evidence_color)
        }
        this == EvidenceTypes.ILLEGAL_CAMPS.value -> {
            AnswerItem(context.getString(R.string.illegal_camps), R.color.evidence_color)
        }
        this == EvidenceTypes.FIRED_BURNED_AREAS.value -> {
            AnswerItem(context.getString(R.string.fires_burned_areas), R.color.evidence_color)
        }
        this == EvidenceTypes.OTHER.value -> {
            AnswerItem(context.getString(R.string.other_text), R.color.evidence_color)
        }

        // Actions
        this == Actions.NONE.value -> {
            AnswerItem(context.getString(R.string.common_none), R.color.action_color)
        }
        this == Actions.COLLECTED_EVIDENCE.value -> {
            AnswerItem(context.getString(R.string.collected_evidence), R.color.action_color)
        }
        this == Actions.ISSUE_A_WARNING.value -> {
            AnswerItem(context.getString(R.string.issue_a_warning), R.color.action_color)
        }
        this == Actions.CONFISCATED_EQUIPMENT.value -> {
            AnswerItem(context.getString(R.string.confiscated_equipment), R.color.action_color)
        }
        this == Actions.OTHER.value -> {
            AnswerItem(context.getString(R.string.other_text), R.color.action_color)
        }
        this == Actions.DAMAGED_MACHINERY.value -> {
            AnswerItem(context.getString(R.string.damaged_machinery), R.color.action_color)
        }

        // PoachingEvidence
        this == PoachingEvidence.NONE.value -> {
            AnswerItem(context.getString(R.string.common_none), R.color.poaching_evidence_color)
        }
        this == PoachingEvidence.BULLET_SHELLS.value -> {
            AnswerItem(context.getString(R.string.bullet_shells), R.color.poaching_evidence_color)
        }
        this == PoachingEvidence.FOOTPRINTS.value -> {
            AnswerItem(context.getString(R.string.footprints), R.color.poaching_evidence_color)
        }
        this == PoachingEvidence.DOG_TRACKS.value -> {
            AnswerItem(context.getString(R.string.dog_tracks), R.color.poaching_evidence_color)
        }
        this == PoachingEvidence.OTHER.value -> {
            AnswerItem(context.getString(R.string.other_text), R.color.poaching_evidence_color)
        }

        // PoachingScale
        this == PoachingScale.NONE.value -> {
            AnswerItem(
                context.getString(R.string.poaching_scale) + " " + context.getString(R.string.common_none),
                R.color.poaching_scale_color
            )
        }
        this == PoachingScale.LARGE.value -> {
            AnswerItem(
                context.getString(R.string.poaching_scale) + " " + context.getString(R.string.large_text),
                R.color.poaching_scale_color
            )
        }
        this == PoachingScale.SMALL.value -> {
            AnswerItem(
                context.getString(R.string.poaching_scale) + " " + context.getString(R.string.small_text),
                R.color.poaching_scale_color
            )
        }

        else -> null
    }
}
