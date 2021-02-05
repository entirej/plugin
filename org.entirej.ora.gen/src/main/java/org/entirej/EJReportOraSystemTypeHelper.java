package org.entirej;

import java.sql.Connection;
import java.sql.SQLException;

import org.entirej.framework.report.EJReportConnectionHelper;
import org.entirej.framework.report.EJReportManagedFrameworkConnection;

import oracle.jdbc.OracleConnection;

public class EJReportOraSystemTypeHelper
{

    public static Object toJDBCStruct(Connection conn, String sqlName, Object[] data) throws SQLException
    {

        return ((OracleConnection) (conn).unwrap(OracleConnection.class)).createStruct(sqlName, data);

    }

    public static Object toJDBCArray(Connection conn, String sqlName, Object[] data) throws SQLException
    {

        Object[] convertedData = new Object[data.length];

        for (int i = 0; i < data.length; i++)
        {
            if (data[i] instanceof EJReportOraCollectionType)
            {
                EJReportOraCollectionType type = (EJReportOraCollectionType) data[i];
                convertedData[i] = type.toJDBC(type, conn);
            }
        }
        return ((OracleConnection) (conn).unwrap(OracleConnection.class)).createOracleArray(sqlName, convertedData);

    }
}
