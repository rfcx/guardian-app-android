package org.rfcx.ranger.entity.report

/**
 * Created by Jingjoeh on 10/22/2017 AD.
 */

data class SendReportResponse(
        val data: SendReportData,
        val links: Links
)

data class SendReportData(
        val id: String, //98f62a07-4c12-4fb1-a0bf-7859ed6a1629
        val type: String, //report
        val attributes: SendReportAttributes
)

data class SendReportAttributes(
        val reporter: String //2ada55fb-433f-4074-a3c8-aa3f146fc10e
)

data class Links(
        val self: String //http://staging-api.rfcx.org/v1/reports/98f62a07-4c12-4fb1-a0bf-7859ed6a1629
)