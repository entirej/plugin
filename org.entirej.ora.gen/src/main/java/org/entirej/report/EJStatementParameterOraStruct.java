package org.entirej.report;

import java.sql.SQLException;
import java.sql.Struct;
import java.sql.Types;

import org.entirej.EJOraCollectionType;
import org.entirej.framework.report.service.EJReportParameterType;
import org.entirej.framework.report.service.EJReportStatementParameter;

import oracle.jdbc.OracleData;

public class EJStatementParameterOraStruct<T extends EJOraCollectionType> extends EJReportStatementParameter
{
    private static final int JDBC_TYPE = Types.STRUCT;

    private T _recordType;

    public EJStatementParameterOraStruct(Class<T> claszz, EJReportParameterType parameterType)
    {
        super(parameterType);
        try
        {

            _recordType = claszz.newInstance();

        }
        catch (InstantiationException | IllegalAccessException e)
        {

            e.printStackTrace();
        }
    }

    public EJStatementParameterOraStruct(T tableType, EJReportParameterType parameterType)
    {
        super(parameterType);
        _recordType = tableType;
    }

    public int getJdbcType()
    {
        return JDBC_TYPE;
    }

    public void setResultCollectionType(T type)
    {
        if (type != null)
        {
            _recordType = type;
        }
    }

    public T getCollectionType()
    {
        return _recordType;
    }

    public OracleData create(Struct d)
    {
        try
        {
            return _recordType.create(d, JDBC_TYPE);
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
        _recordType = value;
    }

    public T getValue()
    {
        return _recordType;
    }

}