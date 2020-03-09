package org.entirej;

import java.sql.Connection;
import java.sql.SQLException;

import org.entirej.framework.report.EJReportConnectionHelper;
import org.entirej.framework.report.EJReportManagedFrameworkConnection;

import oracle.jdbc.OracleConnection;
import oracle.jdbc.OracleData;
import oracle.jdbc.OracleDataFactory;

public abstract class EJReportOraCollectionType implements OracleData, OracleDataFactory
{

    public abstract String getSqlName();
    
    
    protected Object toJDBC(Object o, Connection conn) throws SQLException
    {
        if (o instanceof oracle.jdbc.OracleData)
        {
            EJReportManagedFrameworkConnection con = EJReportConnectionHelper.getConnection();
            try
            {
                return ((oracle.jdbc.OracleData) o).toJDBCObject(((OracleConnection) ((Connection) con.getConnectionObject()).unwrap(OracleConnection.class)));
            }
            finally
            {
                con.close();
            }
        }
        return o;
    }
}
