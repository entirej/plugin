package org.entirej;

import java.sql.Struct;
import java.sql.Types;

import org.entirej.framework.core.service.EJParameterType;
import org.entirej.framework.core.service.EJStatementParameter;

import oracle.sql.ORAData;
import oracle.sql.STRUCT;

public class EJStatementParameterOraStruct<T extends EJOraCollectionType> extends EJStatementParameter
{
    private static final int JDBC_TYPE = Types.STRUCT;

    private T _tableType;

    public EJStatementParameterOraStruct(Class<T> claszz, EJParameterType parameterType)
    {
        super(parameterType);
        try
        {

            _tableType = claszz.newInstance();

        }
        catch (InstantiationException | IllegalAccessException e)
        {

            e.printStackTrace();
        }
    }

    public EJStatementParameterOraStruct(T tableType, EJParameterType parameterType)
    {
        super(parameterType);
        _tableType = tableType;
    }

    public int getJdbcType()
    {
        return JDBC_TYPE;
    }

    public void setResultCollectionType(T type)
    {
        if (type != null)
        {
            _tableType = type;
        }
    }

    public T getColelctionType()
    {
        return _tableType;
    }

    public ORAData create(Struct d)
    {
        return _tableType.create((STRUCT) d, JDBC_TYPE);
    }
    
    public void setValue(T value)
    {
        _tableType = value;
    }

    public T getValue()
    {
        return _tableType;
    }

}