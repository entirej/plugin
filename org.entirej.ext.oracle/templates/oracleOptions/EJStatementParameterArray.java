package org.entirej;

import java.sql.SQLData;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.entirej.framework.core.service.EJParameterType;
import org.entirej.framework.core.service.EJStatementParameter;

public class EJStatementParameterArray<E extends SQLData> extends EJStatementParameter
{
    private static final int JDBC_TYPE = Types.ARRAY;

    private Class<E>         _eClass;
    private Object[]         _array;

    public EJStatementParameterArray(Class<E> classInstance, EJParameterType type)
    {
        this(classInstance, type, null);
    }

    public EJStatementParameterArray(Class<E> classInstance, EJParameterType type, E[] value)
    {
        super(type);
        setValue(value);
        _eClass = classInstance;
        List<E> list = new ArrayList<E>();
        _array = list.toArray();
    }

    public int getJdbcType()
    {
        return JDBC_TYPE;
    }

    public Object[] getArray()
    {
        return _array;
    }

    public Class<E> getClassInstance()
    {
        return _eClass;
    }
}
