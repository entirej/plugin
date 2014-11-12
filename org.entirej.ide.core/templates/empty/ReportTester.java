package org.entirej;

import java.io.IOException;

import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JasperViewer;

import org.entirej.framework.report.EJReport;
import org.entirej.framework.report.EJReportFrameworkInitialiser;
import org.entirej.framework.report.EJReportFrameworkManager;
import org.entirej.report.jasper.EJJasperReports;

public class ReportTester {

	public static void main(String[] args) throws IOException {

		EJReportFrameworkManager reportManager = EJReportFrameworkInitialiser
				.initialiseFramework("report.ejprop");
		System.out.println("EJReportFrameworkManager Loaded");

		String reportName = null;// Please set Report name that need to test
									// run.

		if (reportName == null) {
			System.err
					.println("reportName is null.Please set Report name that need to test run");
			return;
		}

		EJReport report = reportManager.createReport(reportName);

		JasperPrint print = EJJasperReports.fillReport(reportManager, report);
		JasperViewer.viewReport(print);

	}

}
