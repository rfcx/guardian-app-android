package org.rfcx.incidents.view.guardian.checklist.storage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.rfcx.incidents.entity.guardian.socket.GuardianArchived
import org.rfcx.incidents.util.audiocoverage.AudioCoverageUtils

class HeatmapAudioCoverageViewModel() : ViewModel() {

    private val _archivedItemsState: MutableStateFlow<List<HeatmapItem>> = MutableStateFlow(emptyList())
    val archivedItemsState = _archivedItemsState.asStateFlow()

    private val _dateState: MutableStateFlow<String> = MutableStateFlow("")
    val dateState = _dateState.asStateFlow()

    private var archivedAudioStructure: JsonObject = JsonObject()
    var availableYearMonths = hashMapOf<Int, List<Int>>()
    var selectedMonth = 0
    var selectedYear = 1995

    fun setArchivedData(archived: List<GuardianArchived>) {
        val tempArchived = archived.map { it.toListOfTimestamp() }.sortedBy { it.listOfFile.firstOrNull() }
        archivedAudioStructure = AudioCoverageUtils.toDateTimeStructure(tempArchived)

        val latestMonthYear = AudioCoverageUtils.getLatestMonthYear(tempArchived)
        selectedMonth = latestMonthYear.first
        selectedYear = latestMonthYear.second
        availableYearMonths = AudioCoverageUtils.getAvailableMonths(archivedAudioStructure)
    }

    fun onPick(month: Int, year: Int) {
        selectedMonth = month
        selectedYear = year
        viewModelScope.launch(Dispatchers.IO) {
            val items = AudioCoverageUtils.filterByMonthYear(archivedAudioStructure, month, year)
            val months = arrayOf(
                "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
            )
            _dateState.tryEmit("Audio coverage on ${months[month]} $year")
            _archivedItemsState.tryEmit(items)
        }
    }
}
