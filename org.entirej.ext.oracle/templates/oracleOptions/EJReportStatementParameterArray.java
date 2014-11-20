package org.entirej;

import java.sql.SQLData;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.entirej.framework.report.service.EJReportParameterType;
import org.entirej.framework.report.service.EJReportStatementParameter;

public class EJReportStatementParameterArray<E extends SQLData> extends EJReportStatementParameter
{
    private static final int JDBC_TYPE = Types.ARRAY;

    private Class<E>         _eClass;
    private Object[]         _array;
    private String           _arrayTypeName;

    public EJReportStatementParameterArray(Class<E> classInstance, EJReportParameterType type, String arrayTypeName)
    {
        this(classInstance, type, null, arrayTypeName);
    }

    public EJReportStatementParameterArray(Class<E> classInstance, EJReportParameterType type, E[] value, String arrayTypeName)
    {
        super(type);
        setValue(value);
        _eClass = classInstance;
        List<E> list = new ArrayList<E>();
        _array = list.toArray();
        _arrayTypeName = arrayTypeName;
    }

    public int getJdbcType()
    {
        return JDBC_TYPE;
    }

    public Object[] getArray()
    {
        return _array;
    }
    
    public String getArrayTypeName()
    {
        return _arrayTypeName;
    }

    public Class<E> getClassInstance()
    {
        return _eClass;
    }
}
