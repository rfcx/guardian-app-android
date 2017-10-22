package android.rfcx.org.ranger.entity.report

/**
 * Created by Jingjoeh on 10/22/2017 AD.
 */


data class Report(
		val data: Data
)

data class Data(
		val data: ReportData
)

data class ReportData(
		val id: String, //ffd5625d-f0cf-43cd-ad28-0851cbe8447a
		val type: String, //chainsaw
		val attributes: Attributes
)

data class Attributes(
		val start_time: String, //2017-10-05T13:06:21.000Z
		val end_time: String, //2017-10-05T13:06:21.000Z
		val lat: Double, //37.774929
		val lon: Double //-122.419416
)