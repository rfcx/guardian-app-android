package android.rfcx.org.ranger.entity

/**
 * Created by Jingjoeh on 10/5/2017 AD.
 */

data class Message(
		val guid: String, //225c69ef-3cd3-44b6-85f1-b7bc94f6f956
		val time: String, //2008-09-15T10:53:00.000Z
		val text: String, //message text
		val type: String, //ranger-warning
		val from: From,
		val to: To,
		val coords: Coords
)

data class To(
		val guid: String, //2ada55fb-433f-4074-a3c8-aa3f146fc10e
		val email: String, //jingjoeh@gmail.com
		val firstname: String, //Komkrit
		val lastname: String, //Banglad
		val username: Any, //null
		val accessibleSites: List<Any>,
		val defaultSite: Any //null
)

data class From(
		val guid: String, //2ada55fb-433f-4074-a3c8-aa3f146fc10e
		val email: String, //jingjoeh@gmail.com
		val firstname: String, //Komkrit
		val lastname: String, //Banglad
		val username: Any, //null
		val accessibleSites: List<Any>,
		val defaultSite: Any //null
)

data class Coords(
		val lat: Double, //12.1
		val lon: Double //123.12
)