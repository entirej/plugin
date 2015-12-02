package org.entirej.gen;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import org.entirej.framework.report.service.EJReportServiceContentGenerator;

public class EJReportOracleServiceGenerator implements EJReportServiceContentGenerator
{
    
    public String getTemplate()
    {
        return asString("EJFormOracleServiceGenerator.ftl");
    }

    public static String asString(String resourceNmae)
    {

        InputStream stream = EJReportOracleServiceGenerator.class.getResourceAsStream(resourceNmae);
        if (stream == null)
        {
            return "";
        }
        Scanner scanner = new Scanner(stream);
        try
        {

            return scanner.useDelimiter("\\A").next();
        }
        finally
        {
            try
            {
                stream.close();
                scanner.close();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
