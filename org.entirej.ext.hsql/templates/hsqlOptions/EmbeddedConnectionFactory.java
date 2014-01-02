package org.entirej.db.connection;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.entirej.framework.core.EJFrameworkManager;
import org.entirej.framework.core.interfaces.EJConnectionFactory;
import org.entirej.framework.core.interfaces.EJFrameworkConnection;

public class EmbeddedConnectionFactory implements EJConnectionFactory
{
    String dbPath;

    public EmbeddedConnectionFactory() throws UnsupportedEncodingException
    {
        //access to current app root
        URL r = this.getClass().getResource("/");

        String decoded = URLDecoder.decode(r.getFile(), "UTF-8");



        dbPath = decoded;
    }

    @Override
    public EJFrameworkConnection createConnection(EJFrameworkManager arg0)
    {

        try
        {

            // create embedded db in class path

            Class.forName("org.h2.Driver");

            final Connection connection = DriverManager.getConnection(String.format("jdbc:h2:%sdb/demo", dbPath), "SA", "");

            return new EJFrameworkConnection()
            {

                @Override
                public void rollback()
                {
                    try
                    {
                        connection.rollback();
                    }
                    catch (SQLException e)
                    {
                        e.printStackTrace();
                    }

                }

                @Override
                public Object getConnectionObject()
                {
                    return connection;
                }

                @Override
                public void commit()
                {
                    try
                    {
                        connection.commit();
                    }
                    catch (SQLException e)
                    {

                        e.printStackTrace();
                    }

                }

                @Override
                public void close()
                {
                    try
                    {
                        connection.close();
                    }
                    catch (SQLException e)
                    {
                        e.printStackTrace();
                    }

                }
            };
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        return null;

    }

}
