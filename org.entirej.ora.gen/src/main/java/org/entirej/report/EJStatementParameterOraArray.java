package org.entirej.report;

import java.sql.Array;
import java.sql.Types;

import org.entirej.EJOraCollectionType;
import org.entirej.framework.report.service.EJReportParameterType;
import org.entirej.framework.report.service.EJReportStatementParameter;

import oracle.sql.ARRAY;
import oracle.sql.ORAData;

public class EJStatementParameterOraArray<T extends EJOraCollectionType> extends EJReportStatementParameter
{
    private static final int JDBC_TYPE = Types.ARRAY;

    private T _tableType;

    public EJStatementParameterOraArray(Class<T> claszz, EJReportParameterType parameterType)
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

    public EJStatementParameterOraArray(T tableType, EJReportParameterType parameterType)
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