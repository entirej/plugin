package org.entirej.framework.plugin.gen;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import org.entirej.framework.core.EJApplicationException;
import org.entirej.framework.core.EJFieldName;
import org.entirej.framework.core.EJForm;
import org.entirej.framework.core.EJPojoHelper;
import org.entirej.framework.core.service.EJBlockService;
import org.entirej.framework.core.service.EJPojoGeneratorType;
import org.entirej.framework.core.service.EJQueryCriteria;
import org.entirej.framework.core.service.EJRestrictions;
import org.entirej.framework.core.service.EJServiceGeneratorType;
import org.entirej.framework.core.service.EJStatementCriteria;
import org.entirej.framework.core.service.EJStatementExecutor;
import org.entirej.framework.core.service.EJStatementParameter;
import org.entirej.framework.core.service.EJTableColumn;
import org.entirej.framework.report.EJReport;
import org.entirej.framework.report.EJReportFieldName;
import org.entirej.framework.report.EJReportPojoHelper;
import org.entirej.framework.report.service.EJReportBlockService;
import org.entirej.framework.report.service.EJReportPojoGeneratorType;
import org.entirej.framework.report.service.EJReportQueryCriteria;
import org.entirej.framework.report.service.EJReportRestrictions;
import org.entirej.framework.report.service.EJReportServiceGeneratorType;
import org.entirej.framework.report.service.EJReportStatementCriteria;
import org.entirej.framework.report.service.EJReportStatementExecutor;
import org.entirej.framework.report.service.EJReportStatementParameter;
import org.entirej.framework.report.service.EJReportTableColumn;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class FTLEngine
{
    
    public static String ftlValue(String string)
    {
        if (string == null)
        {
            
            return "";
        }
        
        return string;
    }
    
    public static String genrateFormService(String tl, EJServiceGeneratorType serviceGeneratorType)
    {
        // Build the data-model
        Map<String, Object> data = new HashMap<String, Object>();
        if (serviceGeneratorType.getPojo() != null)
        {
            data.put("pojo_name_full",serviceGeneratorType.getPojo().getName());
            data.put("pojo_name", serviceGeneratorType.getPojo().getSimpleName());
        }
        data.put("service_name", serviceGeneratorType.getServiceName());
        data.put("package_name", serviceGeneratorType.getPackageName());
        data.put("table_name", serviceGeneratorType.getTableName());
        data.put("queryInPages", String.valueOf(serviceGeneratorType.canQueryInPages()));
        String queryStatement = serviceGeneratorType.getQueryStatement();
        if (queryStatement == null || queryStatement.trim().isEmpty())
        {
            queryStatement = buildSelectStatement(serviceGeneratorType);
        }
        data.put("query_statement", queryStatement);
        
        data.put("delete_statement", splitStatementToStrBuilder(serviceGeneratorType.getDeleteStatement()));
        data.put("insert_statement", splitStatementToStrBuilder(serviceGeneratorType.getInsertStatement()));
        data.put("update_statement", splitStatementToStrBuilder(serviceGeneratorType.getUpdateStatement()));
        
        data.put("query_procedure",  ftlValue(serviceGeneratorType.getSelectProcedureName()));
        data.put("delete_procedure",  ftlValue(serviceGeneratorType.getDeleteProcedureName()));
        data.put("insert_procedure",  ftlValue(serviceGeneratorType.getInsertProcedureName()));
        data.put("update_procedure",  ftlValue(serviceGeneratorType.getUpdateProcedureName()));
        Collection<String> props = serviceGeneratorType.getPropertyKeys();
        
        for (String key : props)
        {
            data.put(key, serviceGeneratorType.getProperty(key));
        }
        
        {
            List<EJTableColumn> fields = new ArrayList<EJTableColumn>();
            Map<String, Class<?>> gettersAndDatatypes = EJPojoHelper.getGettersAndDatatypes(serviceGeneratorType.getPojo());
            for (Entry<String, Class<?>> filed : gettersAndDatatypes.entrySet())
            {
                EJTableColumn column = new EJTableColumn();
                column.setName(EJPojoHelper.getFieldName(serviceGeneratorType.getPojo(), filed.getKey()));
                column.setDatatypeName(filed.getValue().getSimpleName());
                fields.add(column);
            }
            data.put("fields", fields);
            
        }
        
        Set<String> imports = new TreeSet<String>();
        
        ArrayList<Class<?>> types = EJPojoHelper.getDataTypes(serviceGeneratorType.getPojo());
        
        for (Class<?> dataType : types)
        {
            if ((!dataType.getName().startsWith("java.lang")))
            {
                imports.add(dataType.getName());
            }
        }
        
        if (serviceGeneratorType.getSelectProcedureParameters() != null)
        {
            
            data.put("query_parameters", serviceGeneratorType.getSelectProcedureParameters());
            // read imports for columns
            for (EJTableColumn param : serviceGeneratorType.getSelectProcedureParameters())
            {
                if (param.getParameterType() != null)
                {
                    continue;
                }
                
                String pack = null;
                if (param.getDatatypeName().contains("."))
                {
                    pack = param.getDatatypeName().substring(0, param.getDatatypeName().lastIndexOf("."));
                }
                
                // If the type is not in java.lang, then add the import list
                if (pack != null && (!"java.lang".equals(pack)))
                {
                    imports.add(param.getDatatypeName());
                }
                
            }
        }
        if (serviceGeneratorType.getInsertProcedureParameters() != null)
        {
            
            data.put("insert_parameters", serviceGeneratorType.getInsertProcedureParameters());
            // read imports for columns
            for (EJTableColumn param : serviceGeneratorType.getInsertProcedureParameters())
            {
                if (param.getParameterType() != null)
                {
                    continue;
                }
                
                String pack = null;
                if (param.getDatatypeName().contains("."))
                {
                    pack = param.getDatatypeName().substring(0, param.getDatatypeName().lastIndexOf("."));
                }
                
                // If the type is not in java.lang, then add the import list
                if (pack != null && (!"java.lang".equals(pack)))
                {
                    imports.add(param.getDatatypeName());
                }
                
            }
        }
        if (serviceGeneratorType.getUpdateProcedureParameters() != null)
        {
            
            data.put("update_parameters", serviceGeneratorType.getUpdateProcedureParameters());
            // read imports for columns
            for (EJTableColumn param : serviceGeneratorType.getUpdateProcedureParameters())
            {
                if (param.getParameterType() != null)
                {
                    continue;
                }
                
                String pack = null;
                if (param.getDatatypeName().contains("."))
                {
                    pack = param.getDatatypeName().substring(0, param.getDatatypeName().lastIndexOf("."));
                }
                
                // If the type is not in java.lang, then add the import list
                if (pack != null && (!"java.lang".equals(pack)))
                {
                    imports.add(param.getDatatypeName());
                }
                
            }
        }
        if (serviceGeneratorType.getDeleteProcedureParameters() != null)
        {
            
            data.put("delete_parameters", serviceGeneratorType.getDeleteProcedureParameters());
            // read imports for columns
            for (EJTableColumn param : serviceGeneratorType.getDeleteProcedureParameters())
            {
                if (param.getParameterType() != null)
                {
                    continue;
                }
                
                String pack = null;
                if (param.getDatatypeName().contains("."))
                {
                    pack = param.getDatatypeName().substring(0, param.getDatatypeName().lastIndexOf("."));
                }
                
                // If the type is not in java.lang, then add the import list
                if (pack != null && (!"java.lang".equals(pack)))
                {
                    imports.add(param.getDatatypeName());
                }
                
            }
        }
        
        imports.add(EJForm.class.getName());
        imports.add(EJApplicationException.class.getName());
        imports.add(EJBlockService.class.getName());
        imports.add(EJQueryCriteria.class.getName());
        imports.add(EJStatementCriteria.class.getName());
        imports.add(EJRestrictions.class.getName());
        imports.add(EJStatementExecutor.class.getName());
        imports.add(EJStatementParameter.class.getName());
        imports.add(ArrayList.class.getName());
        imports.add(List.class.getName());
        imports.add(serviceGeneratorType.getPojo().getName());
        
        data.put("imports", imports);
        
        // Freemarker configuration object
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);
        cfg.setObjectWrapper(new EJObjectWrapper(cfg.getIncompatibleImprovements()));
        try
        {
            
            Template template = new Template("SERVICE_TEMPLATE", new StringReader(tl), cfg);
            
            // Console output
            Writer out = new OutputStreamWriter(System.out);
            template.process(data, out);
            out.flush();
            
            // File output
            StringWriter writer = new StringWriter();
            
            template.process(data, writer);
            writer.flush();
            writer.close();
            return writer.toString();
            
        }
        catch (IOException e)
        {
            throw new EJApplicationException(e.getMessage());
        }
        catch (TemplateException e)
        {
            throw new EJApplicationException(e.getMessage());
        }
    }
    
    public static String genrateReportService(String tl, EJReportServiceGeneratorType serviceGeneratorType)
    {
        // Build the data-model
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("pojo_name_full", serviceGeneratorType.getPojo().getName());
        data.put("pojo_name", serviceGeneratorType.getPojo().getSimpleName());
        data.put("service_name", serviceGeneratorType.getServiceName());
        data.put("package_name", serviceGeneratorType.getPackageName());
        data.put("table_name", serviceGeneratorType.getTableName());
        String queryStatement = serviceGeneratorType.getQueryStatement();
        if (queryStatement == null || queryStatement.trim().isEmpty())
        {
            queryStatement = buildReportSelectStatement(serviceGeneratorType);
        }
        data.put("query_statement", queryStatement);
        
        data.put("query_procedure", serviceGeneratorType.getSelectProcedureName());
        Collection<String> props = serviceGeneratorType.getPropertyKeys();
        
        for (String key : props)
        {
            data.put(key, serviceGeneratorType.getProperty(key));
        }
        
        {
            List<EJTableColumn> fields = new ArrayList<EJTableColumn>();
            Map<String, Class<?>> gettersAndDatatypes = EJPojoHelper.getGettersAndDatatypes(serviceGeneratorType.getPojo());
            for (Entry<String, Class<?>> filed : gettersAndDatatypes.entrySet())
            {
                EJTableColumn column = new EJTableColumn();
                column.setName(EJPojoHelper.getFieldName(serviceGeneratorType.getPojo(), filed.getKey()));
                column.setDatatypeName(filed.getValue().getSimpleName());
                fields.add(column);
            }
            data.put("fields", fields);
            
        }
        
        Set<String> imports = new TreeSet<String>();
        
        ArrayList<Class<?>> types = EJPojoHelper.getDataTypes(serviceGeneratorType.getPojo());
        
        for (Class<?> dataType : types)
        {
            if ((!dataType.getName().startsWith("java.lang")))
            {
                imports.add(dataType.getName());
            }
        }
        
        if (serviceGeneratorType.getSelectProcedureParameters() != null)
        {
            
            data.put("query_parameters", serviceGeneratorType.getSelectProcedureParameters());
            // read imports for columns
            for (EJReportTableColumn param : serviceGeneratorType.getSelectProcedureParameters())
            {
                if (param.getParameterType() != null)
                {
                    continue;
                }
                
                String pack = null;
                if (param.getDatatypeName().contains("."))
                {
                    pack = param.getDatatypeName().substring(0, param.getDatatypeName().lastIndexOf("."));
                }
                
                // If the type is not in java.lang, then add the import list
                if (pack != null && (!"java.lang".equals(pack)))
                {
                    imports.add(param.getDatatypeName());
                }
                
            }
        }
        
        imports.add(EJReport.class.getName());
        imports.add(EJApplicationException.class.getName());
        imports.add(EJReportBlockService.class.getName());
        imports.add(EJReportQueryCriteria.class.getName());
        imports.add(EJReportStatementCriteria.class.getName());
        imports.add(EJReportRestrictions.class.getName());
        imports.add(EJReportStatementExecutor.class.getName());
        imports.add(EJReportStatementParameter.class.getName());
        imports.add(ArrayList.class.getName());
        imports.add(List.class.getName());
        imports.add(serviceGeneratorType.getPojo().getName());
        
        data.put("imports", imports);
        
        // Freemarker configuration object
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);
        cfg.setObjectWrapper(new EJObjectWrapper(cfg.getIncompatibleImprovements()));
        try
        {
            
            Template template = new Template("SERVICE_TEMPLATE", new StringReader(tl), cfg);
            
            // Console output
            Writer out = new OutputStreamWriter(System.out);
            template.process(data, out);
            out.flush();
            
            // File output
            StringWriter writer = new StringWriter();
            
            template.process(data, writer);
            writer.flush();
            writer.close();
            return writer.toString();
            
        }
        catch (IOException e)
        {
            throw new EJApplicationException(e.getMessage());
        }
        catch (TemplateException e)
        {
            throw new EJApplicationException(e.getMessage());
        }
    }
    
    public static String genrateFormPojo(String tl, EJPojoGeneratorType pojoGeneratorType)
    {
        // Build the data-model
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("class_name", pojoGeneratorType.getClassName());
        data.put("package_name", pojoGeneratorType.getPackageName());
        
        Collection<String> props = pojoGeneratorType.getPropertyKeys();
        
        for (String key : props)
        {
            data.put(key, pojoGeneratorType.getProperty(key));
        }
        data.put("columns", pojoGeneratorType.getColumns());
        
        Set<String> imports = new TreeSet<String>();
        imports.add(EJFieldName.class.getName());
        // imports.add(EJPojoProperty.class.getName());
        
        // read imports for columns
        for (EJTableColumn param : pojoGeneratorType.getColumns())
        {
            if (param.getParameterType() != null)
            {
                continue;
            }
            
            String pack = null;
            if (param.getDatatypeName().contains("."))
            {
                pack = param.getDatatypeName().substring(0, param.getDatatypeName().lastIndexOf("."));
            }
            
            // If the type is not in java.lang, then add the import list
            if (pack != null && (!"java.lang".equals(pack)))
            {
                imports.add(param.getDatatypeName());
            }
            
        }
        
        data.put("imports", imports);
        
        // Freemarker configuration object
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);
        cfg.setObjectWrapper(new EJObjectWrapper(cfg.getIncompatibleImprovements()));
        try
        {
            
            Template template = new Template("POJO_TEMPLATE", new StringReader(tl), cfg);
            
            // Console output
            Writer out = new OutputStreamWriter(System.out);
            template.process(data, out);
            out.flush();
            
            // File output
            StringWriter writer = new StringWriter();
            
            template.process(data, writer);
            writer.flush();
            writer.close();
            return writer.toString();
            
        }
        catch (IOException e)
        {
            throw new EJApplicationException(e.getMessage());
        }
        catch (TemplateException e)
        {
            throw new EJApplicationException(e.getMessage());
        }
    }
    
    public static String genrateReportPojo(String tl, EJReportPojoGeneratorType pojoGeneratorType)
    {
        // Build the data-model
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("class_name", pojoGeneratorType.getClassName());
        data.put("package_name", pojoGeneratorType.getPackageName());
        
        Collection<String> props = pojoGeneratorType.getPropertyKeys();
        
        for (String key : props)
        {
            data.put(key, pojoGeneratorType.getProperty(key));
        }
        data.put("columns", pojoGeneratorType.getColumns());
        
        Set<String> imports = new TreeSet<String>();
        imports.add(EJReportFieldName.class.getName());
        // imports.add(EJPojoProperty.class.getName());
        
        // read imports for columns
        for (EJReportTableColumn param : pojoGeneratorType.getColumns())
        {
            if (param.getParameterType() != null)
            {
                continue;
            }
            
            String pack = null;
            if (param.getDatatypeName().contains("."))
            {
                pack = param.getDatatypeName().substring(0, param.getDatatypeName().lastIndexOf("."));
            }
            
            // If the type is not in java.lang, then add the import list
            if (pack != null && (!"java.lang".equals(pack)))
            {
                imports.add(param.getDatatypeName());
            }
            
        }
        
        data.put("imports", imports);
        
        // Freemarker configuration object
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);
        cfg.setObjectWrapper(new EJObjectWrapper(cfg.getIncompatibleImprovements()));
        try
        {
            
            Template template = new Template("POJO_TEMPLATE", new StringReader(tl), cfg);
            
            // Console output
            Writer out = new OutputStreamWriter(System.out);
            template.process(data, out);
            out.flush();
            
            // File output
            StringWriter writer = new StringWriter();
            
            template.process(data, writer);
            writer.flush();
            writer.close();
            return writer.toString();
            
        }
        catch (IOException e)
        {
            throw new EJApplicationException(e.getMessage());
        }
        catch (TemplateException e)
        {
            throw new EJApplicationException(e.getMessage());
        }
    }
    
    public static String asString(String resourceNmae)
    {
        
        InputStream stream = FTLEngine.class.getClassLoader().getResourceAsStream(resourceNmae);
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
    
    public static String buildSelectStatement(EJServiceGeneratorType generatorType)
    {
        String baseTableName = generatorType.getTableName();
        
        if (baseTableName == null || baseTableName.trim().length() == 0)
        {
            return "";
        }
        
        // Only create a statement if there has been no select statement defined
        // for the block
        StringBuffer selectStatementBuffer = new StringBuffer();
        selectStatementBuffer.append("\"");
        selectStatementBuffer.append("SELECT ");
        
        // Add select columns
        int col = 0;
        for (String fieldName : EJPojoHelper.getFieldNames(generatorType.getPojo()))
        {
            col++;
            if (col > 1)
            {
                selectStatementBuffer.append(",");
            }
            selectStatementBuffer.append(fieldName);
            
        }
        selectStatementBuffer.append(" FROM ");
        selectStatementBuffer.append(baseTableName);
        selectStatementBuffer.append("\"");
        return selectStatementBuffer.toString();
    }
    
    public static String buildReportSelectStatement(EJReportServiceGeneratorType generatorType)
    {
        String baseTableName = generatorType.getTableName();
        
        if (baseTableName == null || baseTableName.trim().length() == 0)
        {
            return "";
        }
        
        // Only create a statement if there has been no select statement defined
        // for the block
        StringBuffer selectStatementBuffer = new StringBuffer();
        selectStatementBuffer.append("\"");
        selectStatementBuffer.append("SELECT ");
        
        // Add select columns
        int col = 0;
        for (String fieldName : EJReportPojoHelper.getFieldNames(generatorType.getPojo()))
        {
            col++;
            if (col > 1)
            {
                selectStatementBuffer.append(",");
            }
            selectStatementBuffer.append(fieldName);
            
        }
        selectStatementBuffer.append(" FROM ");
        selectStatementBuffer.append(baseTableName);
        selectStatementBuffer.append("\"");
        return selectStatementBuffer.toString();
    }
    
    private static String splitStatementToStrBuilder(String stmt)
    {
        
        if (stmt == null || stmt.isEmpty())
        {
            return "";
        }
        
        StringBuilder builder = new StringBuilder();
        
        builder.append("new StringBuilder()");
        
        String[] splits = stmt.split("\n");
        
        for (String split : splits)
        {
            builder.append(".append(\"").append("\\n").append(split.replace('\n', ' ').trim()).append(" \")");
        }
        
        builder.append(".toString()");
        
        return builder.toString();
        
    }
    
}
