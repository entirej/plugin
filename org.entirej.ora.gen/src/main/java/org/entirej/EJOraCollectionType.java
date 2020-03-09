package org.entirej;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.SQLException;

import org.entirej.framework.core.EJApplicationException;

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
            return ((oracle.jdbc.OracleData) o).toJDBCObject(conn.unwrap(OracleConnection.class));
        }
        return o;
    }

    protected Object toJDBCClob(String text, Connection conn)
    {

        Clob descLong;
        try
        {
            descLong = conn.createClob();
            descLong.setString(1L, text);
            return descLong;
        }
        catch (SQLException e)
        {
            throw new EJApplicationException(e);
        }
    }

}
