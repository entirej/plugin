package org.entirej;import java.math.BigDecimal;import java.sql.Date;import java.sql.Time;import java.sql.Timestamp;import java.sql.Types;import java.util.ArrayList;import java.util.LinkedHashMap;import org.entirej.framework.core.service.EJPojoContentGenerator;import org.entirej.framework.core.service.EJPojoGeneratorType;import org.entirej.framework.core.service.EJTableColumn;public class EJFormOraclePojoGenerator implements EJPojoContentGenerator{    private ArrayList<String>                    _addedImports = new ArrayList<String>();    private LinkedHashMap<String, EJTableColumn> _methodNames  = new LinkedHashMap<String, EJTableColumn>();    public String generateContent(EJPojoGeneratorType type)    {        StringBuilder fileBuilder = new StringBuilder();        fileBuilder.append("package ").append(type.getPackageName());        fileBuilder.append(";\n\n");        StringBuilder methodBuilder = new StringBuilder();        methodBuilder.append("\n");        StringBuilder paramaterBuilder = new StringBuilder();        ArrayList<String> propertyNames = new ArrayList<String>();        // add FieldName annotation to imports        fileBuilder.append("import ").append("org.entirej.framework.core.EJFieldName").append(";\n");        _addedImports.add("org.entirej.framework.core.EJFieldName");        // add EJPojoProperty imports        fileBuilder.append("import ").append("org.entirej.framework.core.service.EJPojoProperty").append(";\n");        _addedImports.add("org.entirej.framework.core.service.EJPojoProperty");        // Oracle Type Imports        fileBuilder.append("import java.sql.SQLData;\n");        fileBuilder.append("import java.sql.SQLException;\n");        fileBuilder.append("import java.sql.SQLInput;\n");        fileBuilder.append("import java.sql.SQLOutput;\n");        fileBuilder.append("import java.sql.Types;\n");        fileBuilder.append("import java.util.ArrayList;\n");        _addedImports.add("java.sql.SQLData");        _addedImports.add("java.sql.SQLException");        _addedImports.add("java.sql.SQLInput");        _addedImports.add("java.sql.SQLOutput");        _addedImports.add("java.sql.Types");        _addedImports.add("java.util.ArrayList");        boolean arrayAdded = false;                for (EJTableColumn param : type.getColumns())        {            if (param.getParameterType() != null)            {                continue;            }            String pack = null;            String typeName = null;                        if (param.getDatatypeName().contains("."))            {                pack = param.getDatatypeName().substring(0, param.getDatatypeName().lastIndexOf("."));                typeName = param.getDatatypeName().substring(param.getDatatypeName().lastIndexOf(".") + 1);            }            else            {                typeName = param.getDatatypeName();            }            // If the type is not in java.lang, then add the import            // statement to the package Add the package name to a list, to            // ensure that the import is not duplicated            if (pack != null && (!"java.lang".equals(pack)) && (!_addedImports.contains(param.getDatatypeName())))            {                fileBuilder.append("import ").append(param.getDatatypeName()).append(";\n");                _addedImports.add(param.getDatatypeName());            }            String propertyName = "_" + databaseNameToObjectName(param.getName());            if (param.isArray())            {                paramaterBuilder.append("    private EJPojoProperty<ArrayList<").append(typeName).append(">> ").append(propertyName).append(";\n");            }            else            {                paramaterBuilder.append("    private EJPojoProperty<").append(typeName).append("> ").append(propertyName).append(";\n");            }            if ((!arrayAdded) && param.isArray())            {                arrayAdded = true;                fileBuilder.append("import org.entirej.EJStatementParameterArray;\n");                fileBuilder.append("import org.entirej.framework.core.service.EJParameterType;\n");                fileBuilder.append("import org.entirej.OracleStatementExecutor;\n");                fileBuilder.append("import java.sql.Array;\n");            }            methodBuilder.append(getMethods(typeName, param));            propertyNames.add(propertyName);        }        fileBuilder.append("\npublic class ").append(type.getClassName()).append(" implements SQLData\n{\n");        fileBuilder.append("    public static final String  _SQL_NAME     = \"").append(type.getProperty("OBJECT_NAME")).append("\";\n");        fileBuilder.append("    public static final int     _SQL_TYPECODE = Types.STRUCT;\n\n");        fileBuilder.append(paramaterBuilder.toString());        fileBuilder.append(methodBuilder.toString());        fileBuilder.append(getSQLDataMethods(type));        fileBuilder.append(getClearMethod(propertyNames));        fileBuilder.append("\n}");        return fileBuilder.toString();    }    private String getSQLDataMethods(EJPojoGeneratorType type)    {        StringBuilder builder = new StringBuilder();        builder.append("    @Override\n");        builder.append("    public String getSQLTypeName() throws SQLException\n");        builder.append("    {\n");        builder.append("        return _SQL_NAME;\n");        builder.append("    }\n\n");        builder.append("    @Override\n");        builder.append("    public void readSQL(SQLInput stream, String typeName) throws SQLException\n");        builder.append("    {\n");        for (String name : _methodNames.keySet())        {            EJTableColumn col = _methodNames.get(name);            String typeName = col.getDatatypeName().substring(col.getDatatypeName().lastIndexOf(".") + 1);            if (col.isArray())            {                String paramName = name.toLowerCase();                builder.append("\n        EJStatementParameterArray<");                builder.append(typeName);                builder.append("> ").append(paramName).append(" = new EJStatementParameterArray<");                builder.append(typeName);                builder.append(">(").append(typeName).append(".class, EJParameterType.")                        .append(col.getParameterType() == null ? "INOUT" : col.getParameterType());                builder.append(",\"").append(col.getProperty("OBJECT_NAME"));                builder.append("\");\n");                builder.append("        OracleStatementExecutor.extractArrayValue(stream.readArray(), ");                builder.append(paramName);                builder.append(");\n");                builder.append("        set").append(name).append("((ArrayList<");                builder.append(typeName);                builder.append(">) ").append(paramName).append(".getValue());\n");            }            else if (col.isStruct())            {                String paramName = name.toLowerCase();                                builder.append("\n        EJStatementParameterStruct<");                builder.append(typeName);                builder.append("> ").append(paramName).append(" = new EJStatementParameterStruct<");                builder.append(typeName);                builder.append(">(").append(typeName).append(".class, EJParameterType.").append(col.getParameterType() == null ? "INOUT" : col.getParameterType());                builder.append(",\"").append(typeName).append("\");");                builder.append("\n        OracleStatementExecutor.extractStructValue((Struct) stream.readObject(),").append(paramName).append(");");                builder.append("        set").append(name).append("((").append(typeName).append(") ").append(paramName).append(".getValue());\n");            }            else            {                builder.append("        set").append(name).append("(stream.read").append(typeName).append("());\n");            }        }        builder.append("    }\n\n");        builder.append("    @Override\n");        builder.append("    public void writeSQL(SQLOutput stream) throws SQLException\n");        builder.append("    {\n");        for (String name : _methodNames.keySet())        {            EJTableColumn col = _methodNames.get(name);            String typeName = col.getDatatypeName().substring(col.getDatatypeName().lastIndexOf(".") + 1);            if (col.isArray())            {                String paramName = name.toLowerCase();                                builder.append("\n         if (get").append(name).append("() == null)");                builder.append("\n         {");                builder.append("\n              stream.writeArray(null);");                builder.append("\n         }         else\n         {    ");                                builder.append("\n             ").append(typeName).append("[] ");                builder.append(paramName).append("Array = (");                builder.append(typeName).append("[]) get").append(name).append("().toArray(new ");                builder.append(typeName).append("[] {});\n");                                builder.append("             EJStatementParameterArray<");                builder.append(typeName);                builder.append("> ").append(paramName).append(" = new EJStatementParameterArray<");                builder.append(typeName);                builder.append(">(").append(typeName).append(".class, EJParameterType.");                builder.append(col.getParameterType() == null ? "INOUT" : col.getParameterType());                builder.append(", ").append(paramName).append("Array");                builder.append(",\"").append(col.getProperty("TABLE_NAME")).append("\");");                                builder.append("\n            Array ").append(paramName).append("OraArray = OracleStatementExecutor.createArray(");                builder.append(paramName+");\n");                builder.append("             stream.writeArray(").append(paramName).append("OraArray);");                builder.append("\n         }\n    ");            }            else if (col.isStruct())            {                builder.append("\n         stream.writeObject(get").append(name).append("());\n");            }            else            {                builder.append("         stream.write").append(typeName).append("(get").append(name).append("());\n");            }        }        builder.append("    }\n\n");        return builder.toString();    }    private final String databaseNameToObjectName(String columnName)    {        if (columnName != null && !columnName.contains("_"))        {            if (columnName.toUpperCase().equals(columnName))            {                return columnName.toLowerCase();            }            return columnName;        }        int n = columnName.length();        StringBuilder fieldName = new StringBuilder(n);        for (int i = 0, flag = 0; i < n; i++)        {            char c = columnName.charAt(i);            if (c == '_')            {                flag = 1;                continue;            }            fieldName.append(flag == 0 ? Character.toLowerCase(c) : Character.toUpperCase(c));            flag = 0;        }        return fieldName.toString();    }    private String getMethods(String dataTypeName, EJTableColumn col)    {        String propertyName = databaseNameToObjectName(col.getName());        StringBuilder methodBuilder = new StringBuilder();        // Add the annotation        StringBuilder annotationString = new StringBuilder();        annotationString.append("    @EJFieldName(").append("\"").append(col.getName()).append("\")").append("\n");        methodBuilder.append(annotationString.toString());        String methodName = propertyName.substring(0, 1).toUpperCase();        methodName += propertyName.substring(1);        _methodNames.put(methodName, col);        // Add the getter method        if (col.isArray())        {            methodBuilder.append("    public ArrayList<").append(dataTypeName).append("> get");        }        else        {            methodBuilder.append("    public ").append(dataTypeName).append(" get");        }        methodBuilder.append(propertyName.substring(0, 1).toUpperCase());        methodBuilder.append(propertyName.substring(1)).append("()\n    {\n");        methodBuilder.append("        return EJPojoProperty.getPropertyValue(_" + propertyName).append(");\n    }\n\n");        // Add the annotation        methodBuilder.append(annotationString.toString());        // Now add the setter method        methodBuilder.append("    public void set");        methodBuilder.append(propertyName.substring(0, 1).toUpperCase());        methodBuilder.append(propertyName.substring(1)).append("(");        if (col.isArray())        {            methodBuilder.append("ArrayList<").append(dataTypeName).append(">");        }        else        {            methodBuilder.append(dataTypeName);        }        methodBuilder.append(" ").append(propertyName).append(")\n");        methodBuilder.append("    {\n");        methodBuilder.append("        _").append(propertyName).append(" = ").append("EJPojoProperty.setPropertyValue(_").append(propertyName);        methodBuilder.append(", ").append(propertyName).append(");\n    }\n\n");        // Now add the getting for the initial value        methodBuilder.append(annotationString.toString());        methodBuilder.append("    public ");        if (col.isArray())        {            methodBuilder.append("ArrayList<").append(dataTypeName).append(">");        }        else        {            methodBuilder.append(dataTypeName);        }        methodBuilder.append(" getInitial");        methodBuilder.append(propertyName.substring(0, 1).toUpperCase());        methodBuilder.append(propertyName.substring(1)).append("()\n    {\n");        methodBuilder.append("        return EJPojoProperty.getPropertyInitialValue(_" + propertyName).append(");\n");        methodBuilder.append("    }\n\n");        return methodBuilder.toString();    }    private String getClearMethod(ArrayList<String> initialParamNames)    {        StringBuilder methodBuilder = new StringBuilder();        methodBuilder.append("    public void clearInitialValues()\n");        methodBuilder.append("    {\n");        for (String name : initialParamNames)        {            methodBuilder.append("        EJPojoProperty.clearInitialValue(").append(name).append(");\n");        }        methodBuilder.append("    }\n");        return methodBuilder.toString();    }    public Class<?> getDataTypeForJdbcType(int jdbcType)    {        switch (jdbcType)        {            case Types.BOOLEAN:                return Boolean.class;            case Types.CHAR:            case Types.VARCHAR:            case Types.LONGVARCHAR:                return String.class;            case Types.FLOAT:            case Types.REAL:                return Float.class;            case Types.NUMERIC:            case Types.DECIMAL:            case Types.BIGINT:            case Types.DOUBLE:                return BigDecimal.class;            case Types.BIT:                return Boolean.class;            case Types.TINYINT:            case Types.SMALLINT:            case Types.INTEGER:                return Integer.class;            case Types.BINARY:            case Types.TIMESTAMP:                return Timestamp.class;            case Types.DATE:                return Date.class;            case Types.TIME:                return Time.class;            case Types.VARBINARY:            case Types.LONGVARBINARY:            default:                return String.class;        }    }}