package org.entirej.generators;

import java.util.ArrayList;
import java.util.List;

import javax.swing.filechooser.FileNameExtensionFilter;

import org.entirej.framework.report.EJReportPojoHelper;
import org.entirej.framework.report.service.EJReportServiceContentGenerator;
import org.entirej.framework.report.service.EJReportServiceGeneratorType;
import org.entirej.framework.report.service.EJReportTableColumn;

public class OracleReportServiceGenerator implements EJReportServiceContentGenerator
{
    @Override
    public String generateContent(EJReportServiceGeneratorType type)
    {
        String pojoName = type.getPojo().getSimpleName();

        StringBuilder fileBuilder = new StringBuilder();

        fileBuilder.append("package ").append(type.getPackageName());
        fileBuilder.append(";\n\n");

        fileBuilder.append("import java.util.List;\n");

        fileBuilder.append("import org.entirej.EJStatementParameterArray;\n");
        fileBuilder.append("import org.entirej.OracleReportStatementExecutor;\n");
        fileBuilder.append("import org.entirej.framework.report.EJReport;\n");
        fileBuilder.append("import org.entirej.framework.report.service.EJReportBlockService;\n");
        fileBuilder.append("import org.entirej.framework.report.service.EJReportParameterType;\n");
        fileBuilder.append("import org.entirej.framework.report.service.EJReportQueryCriteria;\n");
        fileBuilder.append("import org.entirej.EJStoredProcedureStatementParameter;\n");
        fileBuilder.append("import org.entirej.EJStatementParameterArray;\n");
        fileBuilder.append("import org.entirej.EJStatementParameterStruct;\n");

        ArrayList<String> dataTypesForImport = new ArrayList<String>();
        for (Class<?> dataType : EJReportPojoHelper.getDataTypes(type.getPojo()))
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

        fileBuilder.append("\npublic class ").append(type.getServiceName()).append(" implements EJReportBlockService<").append(pojoName).append(">\n{\n");

        fileBuilder.append("    private OracleReportStatementExecutor _statementExecutor;\n");

        fileBuilder.append("    \n    public ").append(type.getServiceName()).append("()\n");
        fileBuilder.append("    {\n");
        fileBuilder.append("        _statementExecutor = new OracleReportStatementExecutor();\n");
        fileBuilder.append("    }\n");

        buildQueryStatement(fileBuilder, type, pojoName);

        fileBuilder.append("\n}");

        return fileBuilder.toString();
    }

    private void buildQueryStatement(StringBuilder fileBuilder, EJReportServiceGeneratorType type, String pojoName)
    {
        fileBuilder.append("    @Override\n");
        fileBuilder.append("    public List<").append(pojoName).append("> executeQuery(EJReport report, EJReportQueryCriteria queryCriteria)\n");
        fileBuilder.append("    {\n");
        fileBuilder.append("        StringBuilder stmt = new StringBuilder();\n");
        fileBuilder.append("        stmt.append(\" BEGIN\");\n");
        fileBuilder.append("        stmt.append(\"   ").append(type.getSelectProcedureName()).append(" (\");\n");

        int col = 0;
        for (EJReportTableColumn column : type.getSelectProcedureParameters())
        {
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
        String arrayTypeName = addParameters(fileBuilder, type, pojoName, type.getSelectProcedureParameters());

        fileBuilder.append(");\n");
        fileBuilder.append("        return (List<").append(pojoName).append(">) " + arrayTypeName + ".getValue();\n");
        fileBuilder.append("    }\n\n");
    }

    private String addParameters(StringBuilder fileBuilder, EJReportServiceGeneratorType type, String pojoName, List<EJReportTableColumn> columns)
    {
        String arrayTypeName = null;

        for (EJReportTableColumn column : columns)
        {
            String typeName = column.getDatatypeName().substring(column.getDatatypeName().lastIndexOf(".") + 1);

            if (column.isArray())
            {
                arrayTypeName = column.getName() + "Type";
                fileBuilder.append("        EJStatementParameterArray<").append(pojoName).append("> ");
                fileBuilder.append(column.getName()).append("Type");
                fileBuilder.append(" = new EJStatementParameterArray<").append(pojoName).append("> (");
                fileBuilder.append(pojoName).append(".class, ");
                fileBuilder.append("EJReportParameterType.");
                fileBuilder.append(column.getParameterType());
                fileBuilder.append(", \"").append(type.getTableName()).append("\"");

                fileBuilder.append(");\n");
            }
            else if (column.isStruct())
            {
                fileBuilder.append("        EJStatementParameterStruct<").append(typeName).append("> ");
                fileBuilder.append(column.getName()).append("Type");
                fileBuilder.append(" = new EJStatementParameterStruct<").append(typeName).append("> (");
                fileBuilder.append(typeName).append(".class, ");
                fileBuilder.append("EJReportParameterType.");
                fileBuilder.append(column.getParameterType());
                fileBuilder.append(", \"").append(column.getProperty("OBJECT_NAME")).append("\");");
            }
            else
            {
                fileBuilder.append("        EJStoredProcedureStatementParameter ");
                fileBuilder.append(column.getName()).append("Parameter = new EJStoredProcedureStatementParameter(");
                fileBuilder.append(column.getDatatypeName().substring(column.getDatatypeName().lastIndexOf('.') + 1) + ".class, EJReportParameterType.");
                fileBuilder.append(column.getParameterType());
                fileBuilder.append(");\n");
            }
        }

        fileBuilder.append("\n        _statementExecutor.executePLSQLStoredProcedure(report, stmt.toString()");
        for (EJReportTableColumn column : columns)
        {
            fileBuilder.append(", ");
            fileBuilder.append(column.getName());
            fileBuilder.append("Type");

        }

        return arrayTypeName;
    }

}
