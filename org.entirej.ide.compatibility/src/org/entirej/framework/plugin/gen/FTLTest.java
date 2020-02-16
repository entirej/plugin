package org.entirej.framework.plugin.gen;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Clob;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.entirej.framework.core.service.EJPojoGeneratorType;
import org.entirej.framework.core.service.EJTableColumn;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class FTLTest
{
    
    public static void main(String[] args)
    {
        test2();
    }
    
    private static void test2()
    {
        
        EJPojoGeneratorType pojoGeneratorType = new EJPojoGeneratorType();
        {
            pojoGeneratorType.setClassName("User");
            pojoGeneratorType.setPackageName("org.entirej");
            pojoGeneratorType.setProperty("dev", "anuradha");
            pojoGeneratorType.setProperty("JAVA_OBJECT_NAME", "TJT");
            pojoGeneratorType.setProperty("TABLE_NAME", "");
            pojoGeneratorType.setProperty("JAVA_REC_NAME", "TM01");
            pojoGeneratorType.setProperty("DB_OBJECT_NAME", "TM01");
            
            List<EJTableColumn> columns = new ArrayList<EJTableColumn>();
            
            {
                EJTableColumn column1 = new EJTableColumn();
                column1.setName("ID");
                column1.setDatatypeName("Integer");
                
                EJTableColumn column2 = new EJTableColumn();
                column2.setName("NAME");
                column2.setDatatypeName(Clob.class.getSimpleName());
                
                EJTableColumn column3 = new EJTableColumn();
                column3.setName("DATE");
                column3.setDatatypeName(Date.class.getName());
                columns.add(column1);
                columns.add(column2);
                columns.add(column3);
                
            }
            pojoGeneratorType.setColumnNames(columns);
        }
        String tl = getTemplate();
        FTLEngine.genrateFormPojo(tl, pojoGeneratorType);
    }
   
    
    
    private static void test1()
    {
        // Freemarker configuration object
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);
        try
        {
            
            String tl = "<#assign colors = [\"red\", \"green\", \"blue\"]>" + "${colors?join(\", \")}";
            
            Template template = new Template("test", new StringReader(tl), cfg);
            
            // Build the data-model
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("message", "Hello World!");
            
            // List parsing
            List<String> countries = new ArrayList<String>();
            countries.add("India");
            countries.add("United States");
            countries.add("Germany");
            countries.add("France");
            
            data.put("countries", countries);
            
            // Console output
            Writer out = new OutputStreamWriter(System.out);
            template.process(data, out);
            out.flush();
            
            // File output
            StringWriter writer = new StringWriter();
            template.process(data, writer);
            writer.flush();
            writer.close();
            System.out.println(writer.toString());
            
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (TemplateException e)
        {
            e.printStackTrace();
        }
    }
    
    
    public  static String getTemplate()
    {
        return asString("FTLTest.ftl");
    }

    public static String asString(String resourceNmae)
    {

        InputStream stream = FTLTest.class.getResourceAsStream(resourceNmae);
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
