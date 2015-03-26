package org.entirej.db.connection;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.entirej.framework.core.EJFrameworkManager;
import org.entirej.framework.core.interfaces.EJConnectionFactory;
import org.entirej.framework.core.interfaces.EJFrameworkConnection;

public class EmbeddedConnectionFactory implements EJConnectionFactory
{
    String dbPath;

    public EmbeddedConnectionFactory() throws UnsupportedEncodingException
    {
        // access to current app root
        URL r = this.getClass().getResource("/");

        String decoded = URLDecoder.decode(r.getFile(), "UTF-8");

        dbPath = decoded;
    }

    @Override
    public EJFrameworkConnection createConnection(EJFrameworkManager arg0)
    {

        return new EJFrameworkConnection()
        {
            private AtomicBoolean            init = new AtomicBoolean(false);
            Connection connection;

            @Override
            public void rollback()
            {
                try
                {
                    if (connection != null)
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

                try
                {
                    if(!init.get())
                    {
                        Class.forName("org.h2.Driver");
                        connection = DriverManager.getConnection(String.format("jdbc:h2:%sdb/demo", dbPath), "SA", "");
                        init.set(true);
                    }
                    return connection;
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

            @Override
            public void commit()
            {
                try
                {
                    if (connection != null)
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
                    if (connection != null)
                        connection.close();
                }
                catch (SQLException e)
                {
                    e.printStackTrace();
                }

            }
        };

    }

}
