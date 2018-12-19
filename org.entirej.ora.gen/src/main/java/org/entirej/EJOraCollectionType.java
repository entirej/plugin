package org.entirej;

import java.sql.Connection;
import java.sql.SQLException;

import org.entirej.framework.core.EJManagedFrameworkConnection;

import oracle.jdbc.OracleConnection;
import oracle.jdbc.OracleData;
import oracle.jdbc.OracleDataFactory;

public abstract class EJOraCollectionType implements OracleData, OracleDataFactory
{

    public abstract String getSqlName();
    
    
    protected Object toJDBC(Object o, Connection conn) throws SQLException
    {
        if (o instanceof oracle.jdbc.OracleData)
        {
            EJManagedFrameworkConnection con = org.entirej.framework.core.EJSystemConnectionHelper.getConnection();
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
