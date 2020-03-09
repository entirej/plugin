package org.entirej;

import java.sql.Array;
import java.sql.SQLException;
import java.sql.Types;

import org.entirej.framework.core.service.EJParameterType;
import org.entirej.framework.core.service.EJStatementParameter;

import oracle.jdbc.OracleData;

public class EJStatementParameterOraArray<T extends EJOraCollectionType> extends EJStatementParameter
{
    private static final int JDBC_TYPE = Types.ARRAY;

    private T                _tableType;

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

    public T getCollectionType()
    {
        return _tableType;
    }

    public OracleData create(Array d)
    {
        try
        {
            return _tableType.create(d, JDBC_TYPE);
        }
        catch (SQLException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
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