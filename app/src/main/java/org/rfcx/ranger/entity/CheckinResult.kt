package org.rfcx.ranger.entity

/**
 * Created by Jingjoeh on 10/8/2017 AD.
 */

data class CheckInResult(
		val success: Boolean, //true
		val sqlResult: SqlResult
)

data class SqlResult(
		val fieldCount: Int, //0
		val affectedRows: Int, //0
		val insertId: Int, //0
		val serverStatus: Int, //34
		val warningCount: Int, //0
		val message: String, //&Records: 0  Duplicates: 0  Warnings: 0
		val protocol41: Boolean, //true
		val changedRows: Int //0
)