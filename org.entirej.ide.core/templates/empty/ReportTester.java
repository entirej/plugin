package org.entirej;

import java.io.IOException;


import org.entirej.framework.report.EJReport;
import org.entirej.framework.report.EJReportFrameworkInitialiser;
import org.entirej.framework.report.EJReportFrameworkManager;
import org.entirej.framework.report.interfaces.EJReportRunner;
import java.awt.Desktop;
import java.io.File;

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

		 EJReportRunner reportRunner = reportManager.createReportRunner();
                 String output = reportRunner.runReport(report);
                 Desktop.getDesktop().open(new File(output));
                 try {
                      Thread.sleep(2000);//wait for System to open file
                 } catch (InterruptedException e) {
                              e.printStackTrace();
                 }

	}

}
