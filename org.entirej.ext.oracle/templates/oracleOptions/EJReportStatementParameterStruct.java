package org.entirej;

import java.sql.SQLData;
import java.sql.Types;

import org.entirej.framework.report.service.EJReportParameterType;
import org.entirej.framework.report.service.EJReportStatementParameter;

public class EJReportStatementParameterStruct<E extends SQLData> extends EJReportStatementParameter
{
    private static final int JDBC_TYPE = Types.STRUCT;

    private Class<E>         _eClass;
    private Object           _value;
    private String           _structTypeName;

    public EJReportStatementParameterStruct(Class<E> classInstance, EJReportParameterType type, String structTypeName)
    {
        this(classInstance, type, null, structTypeName);
    }

    public EJReportStatementParameterStruct(Class<E> classInstance, EJReportParameterType type, E value, String structTypeName)
    {
        super(type);
        setValue(value);
        _eClass = classInstance;
        _value = value;
        _structTypeName = structTypeName;
    }

    public int getJdbcType()
    {
        return JDBC_TYPE;
    }

    public Object getValue()
    {
        return _value;
    }
    
    public String getStructTypeName()
    {
        return _structTypeName;
    }

    public Class<E> getClassInstance()
    {
        return _eClass;
    }
    
    public void setValue(Object value)
    {
        _value = value;
    }
}
