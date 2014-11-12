package org.entirej.db.connection;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.entirej.framework.report.EJReportFrameworkManager;
import org.entirej.framework.report.interfaces.EJReportConnectionFactory;
import org.entirej.framework.report.interfaces.EJReportFrameworkConnection;


public class EmbeddedReportConnectionFactory implements EJReportConnectionFactory
{
    String dbPath;

    public EmbeddedReportConnectionFactory() throws UnsupportedEncodingException
    {
        //access to current app root
        URL r = this.getClass().getResource("/");

        String decoded = URLDecoder.decode(r.getFile(), "UTF-8");



        dbPath = decoded;
    }

  
    public EJReportFrameworkConnection createConnection(EJReportFrameworkManager arg0)
    {

        try
        {

            // create embedded db in class path

            Class.forName("org.h2.Driver");

            final Connection connection = DriverManager.getConnection(String.format("jdbc:h2:%sdb/demo", dbPath), "SA", "");

            return new EJReportFrameworkConnection()
            {

               
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

                
                public Object getConnectionObject()
                {
                    return connection;
                }

               
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
