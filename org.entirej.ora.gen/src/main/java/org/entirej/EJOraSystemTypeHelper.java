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
			Object[] convertedData = new Object[data.length];
			 for (int i = 0; i < data.length; i++)
             {
                 if(data[i] instanceof EJOraCollectionType) {
                     EJOraCollectionType type = (EJOraCollectionType) data[i];
                     convertedData[i] = type.toJDBC(type, conn);
                 }
             }
			 
			return ((OracleConnection) ((Connection) con.getConnectionObject()).unwrap(OracleConnection.class))
					.createOracleArray(sqlName, convertedData);
		} finally {
			con.close();
		}
	}
}
