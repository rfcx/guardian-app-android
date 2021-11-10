package org.rfcx.incidents.util

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.rfcx.incidents.R
import java.io.File
import java.io.FileWriter
import java.util.*

object GeoJsonUtils {
	
	fun generateFileName(submittedAt: Date): String {
		return "${submittedAt.toIsoString()}.json"
	}
	
	fun generateGeoJson(context: Context, fileName: String, points: List<DoubleArray>): File {
		val gson = Gson()
		val json = JsonObject()
		//add Type
		json.addProperty("type", "FeatureCollection")
		
		//create features
		val featureArray = JsonArray()
		
		val featureItem = JsonObject()
		featureItem.addProperty("type", "Feature")
		
		val propertyItem = JsonObject()
		propertyItem.addProperty("color", context.getString(R.string.tracking_line))
		featureItem.add("properties", propertyItem)
		
		//create Geometry type
		val geometry = JsonObject()
		geometry.addProperty("type", "LineString")
		//create Geometry coordinate
		geometry.add("coordinates", points.toJsonArray())
		featureItem.add("geometry", geometry)
		
		featureArray.add(featureItem)
		
		//combine all data
		json.add("features", gson.toJsonTree(featureArray).asJsonArray)
		
		//write to file
		return createFile(context, fileName, json)
	}
	
	private fun randomColor(): String {
		val rnd = Random()
		val color = rnd.nextInt(0xffffff + 1)
		return String.format("#%06x", color).toUpperCase(Locale.getDefault())
	}
	
	private fun List<DoubleArray>.toJsonArray(): JsonArray {
		val jsonArray = JsonArray()
		this.forEach { dbArray ->
			val tempJsonArray = JsonArray()
			dbArray.forEach { db ->
				tempJsonArray.add(db)
			}
			jsonArray.add(tempJsonArray)
		}
		return jsonArray
	}
	
	private fun createFile(context: Context, fileName: String, json: JsonObject): File {
		val gson = Gson()
		val dir = File(context.filesDir, "tracking")
		if (!dir.exists()) {
			dir.mkdir()
		}
		val file = File(dir, fileName)
		val writer = FileWriter(file)
		gson.toJson(json, writer)
		
		//close writer
		writer.close()
		return file
	}
}
