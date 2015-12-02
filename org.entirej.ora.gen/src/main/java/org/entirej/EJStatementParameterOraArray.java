package org.entirej;

import java.sql.Array;
import java.sql.Types;

import org.entirej.framework.core.service.EJParameterType;
import org.entirej.framework.core.service.EJStatementParameter;

import oracle.sql.ARRAY;
import oracle.sql.ORAData;

public class EJStatementParameterOraArray<T extends EJOraCollectionType> extends EJStatementParameter
{
    private static final int JDBC_TYPE = Types.ARRAY;

    private T _tableType;

    public EJStatementParameterOraArray(Class<T> claszz, EJParameterType parameterType)
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

    public EJStatementParameterOraArray(T tableType, EJParameterType parameterType)
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

    public ORAData create(Array d)
    {
        return _tableType.create((ARRAY) d, JDBC_TYPE);
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