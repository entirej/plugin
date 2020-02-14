package org.entirej;

import java.io.Reader;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.SQLException;

import org.entirej.framework.core.EJApplicationException;
import org.entirej.framework.core.EJManagedFrameworkConnection;

import oracle.jdbc.OracleConnection;

public class EJOraSystemTypeHelper
{

    public static Object toJDBCStruct(Connection conn, String sqlName, Object[] data) throws SQLException
    {

        EJManagedFrameworkConnection con = org.entirej.framework.core.EJSystemConnectionHelper.getConnection();
        try
        {
            return ((OracleConnection) ((Connection) con.getConnectionObject()).unwrap(OracleConnection.class)).createStruct(sqlName, data);
        }
        finally
        {
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
                if (data[i] instanceof EJOraCollectionType)
                {
                    EJOraCollectionType type = (EJOraCollectionType) data[i];
                    convertedData[i] = type.toJDBC(type, conn);
                }
            }

            return ((OracleConnection) ((Connection) con.getConnectionObject()).unwrap(OracleConnection.class)).createOracleArray(sqlName, convertedData);
        }
        finally
        {
            con.close();
        }
    }

    public static String convertClobToString(Clob clobValue)
    {
        String data = null;
        try
        {
            if (clobValue != null)
            {
                long length = clobValue.length();
                // Check CLOB is not empty.
                if (length > 0)
                {
                    Reader is = clobValue.getCharacterStream();
                    // Initialize local variables.
                    char[] buffer = new char[1024];

                    try
                    {
                        final StringBuilder out = new StringBuilder();
                        for (;;)
                        {
                            int rsz = is.read(buffer, 0, buffer.length);
                            if (rsz < 0)
                                break;
                            out.append(buffer, 0, rsz);
                        }
                        data = out.toString();
                    }
                    catch (Exception e)
                    {
                        throw new EJApplicationException(e);
                    }
                }
                else
                {
                    data = null;
                }
            }
            else
            {
                data = (String) null;
            }
        }
        catch (SQLException e)
        {
            throw new EJApplicationException(e);
        }
        return data;
    }
}
