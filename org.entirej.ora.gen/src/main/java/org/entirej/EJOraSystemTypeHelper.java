package org.entirej;

import java.sql.Connection;
import java.sql.SQLException;

import org.entirej.framework.core.EJManagedFrameworkConnection;

import oracle.jdbc.OracleConnection;

public class EJOraSystemTypeHelper {

	public static Object toJDBCStruct(Connection conn, String sqlName, Object[] data) throws SQLException
	{

		EJManagedFrameworkConnection con = org.entirej.framework.core.EJSystemConnectionHelper.getConnection();
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
		EJManagedFrameworkConnection con = org.entirej.framework.core.EJSystemConnectionHelper.getConnection();
		try
		{
			return ((OracleConnection) ((Connection) con.getConnectionObject()).unwrap(OracleConnection.class))
					.createOracleArray(sqlName, data);
		} finally {
			con.close();
		}
	}
}
