package org.entirej;

import java.sql.Types;

import org.entirej.framework.core.service.EJParameterType;
import org.entirej.framework.core.service.EJStatementParameter;

public class EJStatementParameterClob extends EJStatementParameter
{
    private static final int JDBC_TYPE = Types.CLOB;

    public EJStatementParameterClob(EJParameterType type)
    {
        this(type, null);
    }

    public EJStatementParameterClob(EJParameterType type, String value)
    {
        super(type);
        setValue(value);
    }

    public int getJdbcType()
    {
        return JDBC_TYPE;
    }

}
