package org.rfcx.ranger.entity.report

/**
 * Created by Jingjoeh on 10/22/2017 AD.
 */
enum class ReportType {
    chainsaw, gunshot, vehicle,trespasser
}

enum class ReportSight {
    /* 0 is immediate area (nearby)
          5 is not far away (but not visible)
          100 very far (faintly heard) */
    Immediate, NotFarAway, VeryFar
}