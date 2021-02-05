package org.entirej;

import java.sql.Connection;
import java.sql.SQLException;

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
           return ((oracle.jdbc.OracleData) o).toJDBCObject(((OracleConnection)conn.unwrap(OracleConnection.class)));
            
        }
        return o;
    }
}
