package org.entirej;

import java.sql.Connection;
import java.sql.SQLException;

import org.entirej.framework.report.EJReportConnectionHelper;
import org.entirej.framework.report.EJReportManagedFrameworkConnection;

import oracle.jdbc.OracleConnection;

public class EJReportOraSystemTypeHelper {

	public static Object toJDBCStruct(Connection conn, String sqlName, Object[] data) throws SQLException
	{

		EJReportManagedFrameworkConnection con = EJReportConnectionHelper.getConnection();
		try
		{
			return ((OracleConnection) ((Connection) con.getConnectionObject()).unwrap(OracleConnection.class))
					.createStruct(sqlName, data);
		} finally {
			con.close();
		}
	}

	public static Object toJDBCArray(Connection conn, String sqlName, Object[] data) throws SQLException
	{
		EJReportManagedFrameworkConnection con = EJReportConnectionHelper.getConnection();
		try
		{
			return ((OracleConnection) ((Connection) con.getConnectionObject()).unwrap(OracleConnection.class))
					.createOracleArray(sqlName, data);
		} finally {
			con.close();
		}
	}
}
