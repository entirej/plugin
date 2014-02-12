package org.entirej;

import java.sql.SQLData;
import java.sql.Types;

import org.entirej.framework.core.service.EJParameterType;
import org.entirej.framework.core.service.EJStatementParameter;

public class EJStatementParameterStruct<E extends SQLData> extends EJStatementParameter
{
    private static final int JDBC_TYPE = Types.STRUCT;

    private Class<E>         _eClass;
    private Object           _value;
    private String           _structTypeName;

    public EJStatementParameterStruct(Class<E> classInstance, EJParameterType type, String structTypeName)
    {
        this(classInstance, type, null, structTypeName);
    }

    public EJStatementParameterStruct(Class<E> classInstance, EJParameterType type, E value, String structTypeName)
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
