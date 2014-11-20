package org.entirej.generators;

import java.util.ArrayList;
import java.util.List;

import org.entirej.framework.core.EJPojoHelper;
import org.entirej.framework.core.service.EJParameterType;
import org.entirej.framework.core.service.EJServiceContentGenerator;
import org.entirej.framework.core.service.EJServiceGeneratorType;
import org.entirej.framework.core.service.EJTableColumn;

public class EJFormOracleServiceGenerator implements EJServiceContentGenerator
{
    @Override
    public String generateContent(EJServiceGeneratorType type)
    {
        String pojoName = type.getPojo().getSimpleName();

        StringBuilder fileBuilder = new StringBuilder();

        fileBuilder.append("package ").append(type.getPackageName());
        fileBuilder.append(";\n\n");

        fileBuilder.append("import java.util.List;\n");

        fileBuilder.append("import org.entirej.EJStatementParameterArray;\n");
        fileBuilder.append("import org.entirej.OracleStatementExecutor;\n");
        fileBuilder.append("import org.entirej.framework.core.EJForm;\n");
        fileBuilder.append("import org.entirej.framework.core.service.EJBlockService;\n");
        fileBuilder.append("import org.entirej.framework.core.service.EJParameterType;\n");
        fileBuilder.append("import org.entirej.framework.core.service.EJQueryCriteria;\n");
        fileBuilder.append("import org.entirej.framework.core.service.EJStoredProcedureStatementParameter;\n");

        ArrayList<String> dataTypesForImport = new ArrayList<String>();
        for (Class<?> dataType : EJPojoHelper.getDataTypes(type.getPojo()))
        {
            if (!dataTypesForImport.contains(dataType) && (!dataType.getName().startsWith("java.lang")))
            {
                dataTypesForImport.add(dataType.getName());
            }
        }

        for (String dataType : dataTypesForImport)
        {
            fileBuilder.append("import ").append(dataType).append(";\n");
        }

        fileBuilder.append("import ").append(type.getPojo().getName()).append(";\n");

        fileBuilder.append("\npublic class ").append(type.getServiceName()).append(" implements EJBlockService<").append(pojoName).append(">\n{\n");

        fileBuilder.append("    private OracleStatementExecutor _statementExecutor;\n");

        fileBuilder.append("    \n    public ").append(type.getServiceName()).append("()\n");
        fileBuilder.append("    {\n");
        fileBuilder.append("        _statementExecutor = new OracleStatementExecutor();\n");
        fileBuilder.append("    }\n");

        fileBuilder.append("    @Override\n");
        fileBuilder.append("    public boolean canQueryInPages()\n");
        fileBuilder.append("    {\n");
        fileBuilder.append("        return false;\n");
        fileBuilder.append("    }\n\n");

        buildQueryStatement(fileBuilder, type, pojoName);
        buildInsertStatement(fileBuilder, type, pojoName);
        buildUpdateStatement(fileBuilder, type, pojoName);
        buildDeleteStatement(fileBuilder, type, pojoName);

        fileBuilder.append("\n}");

        return fileBuilder.toString();
    }

    private void buildQueryStatement(StringBuilder fileBuilder, EJServiceGeneratorType type, String pojoName)
    {
        fileBuilder.append("    @Override\n");
        fileBuilder.append("    public List<").append(pojoName).append("> executeQuery(EJForm form, EJQueryCriteria queryCriteria)\n");
        fileBuilder.append("    {\n");
        fileBuilder.append("        StringBuilder stmt = new StringBuilder();\n");
        fileBuilder.append("        stmt.append(\" BEGIN\");\n");

        boolean returnAdded = false;
        for (EJTableColumn column : type.getSelectProcedureParameters())
        {
            if (column.getParameterType().equals(EJParameterType.RETURN))
            {
                returnAdded = true;
                fileBuilder.append("        stmt.append(\"   ? := ");
            }
        }

        if (returnAdded)
        {
            fileBuilder.append(type.getSelectProcedureName()).append(" (\");\n");
        }
        else
        {
            fileBuilder.append("        stmt.append(\"   ").append(type.getSelectProcedureName()).append(" (\");\n");
        }

        int col = 0;
        for (EJTableColumn column : type.getSelectProcedureParameters())
        {
            if (column.getParameterType().equals(EJParameterType.RETURN))
            {
                continue;
            }

            fileBuilder.append("        stmt.append(\"         ");
            if (col != 0)
            {
                fileBuilder.append(",");
            }

            fileBuilder.append(column.getName()).append(" => ?\");\n");

            col++;
        }

        fileBuilder.append("        stmt.append(\"         );\");\n");
        fileBuilder.append("        stmt.append(\" END;\");\n\n");

        // Add the statement parameters
        String arrayTypeName = addParameters(fileBuilder, type, pojoName, type.getSelectProcedureParameters(), false);

        fileBuilder.append(");\n");
        fileBuilder.append("        return (List<").append(pojoName).append(">) "+arrayTypeName+".getValue();\n");
        fileBuilder.append("    }\n\n");
    }

    private void buildInsertStatement(StringBuilder fileBuilder, EJServiceGeneratorType type, String pojoName)
    {
        fileBuilder.append("   @Override\n");
        fileBuilder.append("    public void executeInsert(EJForm form, List<").append(pojoName).append("> newRecords)\n");
        fileBuilder.append("    {\n");

        if (type.getInsertProcedureName() != null)
        {
            fileBuilder.append("        StringBuilder stmt = new StringBuilder();\n");
            fileBuilder.append("        stmt.append(\" BEGIN\");\n");

            boolean returnAdded = false;
            for (EJTableColumn column : type.getSelectProcedureParameters())
            {
                if (column.getParameterType().equals(EJParameterType.RETURN))
                {
                    returnAdded = true;
                    fileBuilder.append("        stmt.append(\"   ? := ");
                }
            }

            if (returnAdded)
            {
                fileBuilder.append(type.getSelectProcedureName()).append(" (\");\n");
            }
            else
            {
                fileBuilder.append("        stmt.append(\"   ").append(type.getSelectProcedureName()).append(" (\");\n");
            }

            int col = 0;
            for (EJTableColumn column : type.getInsertProcedureParameters())
            {
                if (column.getParameterType().equals(EJParameterType.RETURN))
                {
                    continue;
                }

                fileBuilder.append("        stmt.append(\"         ");
                if (col != 0)
                {
                    fileBuilder.append(",");
                }

                fileBuilder.append(column.getName()).append(" => ?\");\n");

                col++;
            }

            fileBuilder.append("        stmt.append(\"         );\");\n");
            fileBuilder.append("        stmt.append(\" END;\");\n\n");

            fileBuilder.append("        ").append(pojoName).append("[] values = (").append(pojoName).append("[])newRecords.toArray();\n");
            // Add the statement parameters
            addParameters(fileBuilder, type, pojoName, type.getInsertProcedureParameters(), true);
            fileBuilder.append(");\n");
        }
        fileBuilder.append("    }\n\n");
    }

    private void buildUpdateStatement(StringBuilder fileBuilder, EJServiceGeneratorType type, String pojoName)
    {
        fileBuilder.append("   @Override\n");
        fileBuilder.append("    public void executeUpdate(EJForm form, List<").append(pojoName).append("> updateRecords)\n");
        fileBuilder.append("    {\n");

        if (type.getUpdateProcedureName() != null)
        {
            fileBuilder.append("        StringBuilder stmt = new StringBuilder();\n");
            fileBuilder.append("        stmt.append(\" BEGIN\");\n");

            boolean returnAdded = false;
            for (EJTableColumn column : type.getSelectProcedureParameters())
            {
                if (column.getParameterType().equals(EJParameterType.RETURN))
                {
                    returnAdded = true;
                    fileBuilder.append("        stmt.append(\"   ? := ");
                }
            }

            if (returnAdded)
            {
                fileBuilder.append(type.getSelectProcedureName()).append(" (\");\n");
            }
            else
            {
                fileBuilder.append("        stmt.append(\"   ").append(type.getSelectProcedureName()).append(" (\");\n");
            }

            int col = 0;
            for (EJTableColumn column : type.getUpdateProcedureParameters())
            {
                if (column.getParameterType().equals(EJParameterType.RETURN))
                {
                    continue;
                }

                fileBuilder.append("        stmt.append(\"         ");
                if (col != 0)
                {
                    fileBuilder.append(",");
                }

                fileBuilder.append(column.getName()).append(" => ?\");\n");

                col++;
            }

            fileBuilder.append("        stmt.append(\"         );\");\n");
            fileBuilder.append("        stmt.append(\" END;\");\n\n");

            fileBuilder.append("        ").append(pojoName).append("[] values = (").append(pojoName).append("[])updateRecords.toArray();\n");
            // Add the statement parameters
            addParameters(fileBuilder, type, pojoName, type.getUpdateProcedureParameters(), true);
            fileBuilder.append(");\n");
        }
        fileBuilder.append("    }\n\n");
    }

    private void buildDeleteStatement(StringBuilder fileBuilder, EJServiceGeneratorType type, String pojoName)
    {
        fileBuilder.append("   @Override\n");
        fileBuilder.append("    public void executeDelete(EJForm form, List<").append(pojoName).append("> deleteRecords)\n");
        fileBuilder.append("    {\n");

        if (type.getDeleteProcedureName() != null)
        {
            fileBuilder.append("        StringBuilder stmt = new StringBuilder();\n");
            fileBuilder.append("        stmt.append(\" BEGIN\");\n");

            boolean returnAdded = false;
            for (EJTableColumn column : type.getSelectProcedureParameters())
            {
                if (column.getParameterType().equals(EJParameterType.RETURN))
                {
                    returnAdded = true;
                    fileBuilder.append("        stmt.append(\"   ? := ");
                }
            }

            if (returnAdded)
            {
                fileBuilder.append(type.getSelectProcedureName()).append(" (\");\n");
            }
            else
            {
                fileBuilder.append("        stmt.append(\"   ").append(type.getSelectProcedureName()).append(" (\");\n");
            }

            int col = 0;
            for (EJTableColumn column : type.getDeleteProcedureParameters())
            {
                if (column.getParameterType().equals(EJParameterType.RETURN))
                {
                    continue;
                }

                fileBuilder.append("        stmt.append(\"         ");
                if (col != 0)
                {
                    fileBuilder.append(",");
                }

                fileBuilder.append(column.getName()).append(" => ?\");\n");

                col++;
            }

            fileBuilder.append("        stmt.append(\"         );\");\n");
            fileBuilder.append("        stmt.append(\" END;\");\n\n");

            fileBuilder.append("        ").append(pojoName).append("[] values = (").append(pojoName).append("[])deleteRecords.toArray();\n");
            // Add the statement parameters
            addParameters(fileBuilder, type, pojoName, type.getDeleteProcedureParameters(), true);
            fileBuilder.append(");\n");
        }
        fileBuilder.append("    }\n\n");
    }

    private String addParameters(StringBuilder fileBuilder, EJServiceGeneratorType type, String pojoName, List<EJTableColumn> columns, boolean addValuesParam)
    {
        String arrayTypeName = null;
        for (int i = 0; i < 2; i++)
        {
            for (EJTableColumn column : columns)
            {
                if (column.getParameterType().equals(EJParameterType.RETURN))
                {
                    if (i!=0)
                    {
                        continue;   
                    }
                    column.setName("return");
                }
                else
                {
                    if (i==0)
                    {
                        continue;   
                    }
                }
                
                String typeName = column.getDatatypeName().substring(column.getDatatypeName().lastIndexOf(".") + 1);
                
                if (column.isArray())
                {
                    arrayTypeName = column.getName() + "Type";
                    fileBuilder.append("        EJStatementParameterArray<").append(pojoName).append("> ");
                    fileBuilder.append(arrayTypeName);
                    fileBuilder.append(" = new EJStatementParameterArray<").append(pojoName).append("> (");
                    fileBuilder.append(pojoName).append(".class, ");
                    fileBuilder.append("EJParameterType.");
                    fileBuilder.append(column.getParameterType());
                    if (addValuesParam)
                    {
                        fileBuilder.append(", values");
                    }
                    fileBuilder.append(", \"").append(type.getTableName());
                    fileBuilder.append("\");\n");
                }
                else if (column.isStruct())
                {
                    fileBuilder.append("        EJStatementParameterStruct<").append(typeName).append("> ");
                    fileBuilder.append(column.getName()).append("Type");
                    fileBuilder.append(" = new EJStatementParameterStruct<").append(typeName).append("> (");
                    fileBuilder.append(typeName).append(".class, ");
                    fileBuilder.append("EJParameterType.");
                    fileBuilder.append(column.getParameterType());
                    fileBuilder.append(", \"").append(column.getProperty("OBJECT_NAME")).append("\");");
                }
                else
                {
                    fileBuilder.append("        EJStoredProcedureStatementParameter ");
                    fileBuilder.append(column.getName()).append("Parameter = new EJStoredProcedureStatementParameter(");
                    fileBuilder.append(column.getDatatypeName().substring(column.getDatatypeName().lastIndexOf('.') + 1) + ".class, EJParameterType.");
                    fileBuilder.append(column.getParameterType());
                    fileBuilder.append(");\n");
                }
            }
        }

        fileBuilder.append("\n        _statementExecutor.executePLSQLStoredProcedure(form, stmt.toString()");
        for (EJTableColumn column : columns)
        {
            if (column.getParameterType().equals(EJParameterType.RETURN))
            {
                fileBuilder.append(", ");
                fileBuilder.append(column.getName());
                fileBuilder.append("Type");
            }
        }
        for (EJTableColumn column : columns)
        {
            if (!column.getParameterType().equals(EJParameterType.RETURN))
            {
                fileBuilder.append(", ");
                fileBuilder.append(column.getName());
                fileBuilder.append("Type");
            }
        }
        
        return arrayTypeName;

    }

}
