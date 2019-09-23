package org.rfcx.ranger

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify

import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.rfcx.ranger.localdb.ReportDb
import org.rfcx.ranger.localdb.ReportImageDb
import org.rfcx.ranger.view.report.ReportDetailViewModel



class ReportDetailViewModelUnitTest {
	@Rule
	@JvmField
	val rule = InstantTaskExecutorRule()
	
	@Test
	fun `1 + 1 = 2`() {
		assertEquals(4, (2 + 2).toLong())
	}
	
	@Test
	fun getReport() {
		
		val viewModel = ReportDetailViewModel(mock(), mock())
		
		viewModel.setReport(1)
		
		viewModel.getReport()
	}
	
//	@Test
//	fun getReportImages(){
//		val reportDb = mock<ReportDb>()
//		val reportImageDb = mock<ReportImageDb>()
//		val viewModel = ReportDetailViewModel(reportDb, reportImageDb)
//
//		viewModel.setReport(mock())
////		val result = viewModel.getReportImages()
//
////		assertEquals(reportImages, result.value)
//	}
	
//	@Test
//	fun addReportImages(){
//		val reportImageDb = mock<ReportImageDb>()
//		val viewModel = ReportDetailViewModel(mock(), reportImageDb)
//
//		viewModel.addReportImages(mock())
//
//
//	}
//
//	@Test
//	fun removeReportImage(){
//		val reportImageDb = mock<ReportImageDb>()
//		val viewModel = ReportDetailViewModel(mock(), reportImageDb)
//
////		viewModel.removeReportImage(mock())
//
//	}
//
}