package org.entirej;

import java.sql.Blob;
import java.sql.SQLException;
import java.sql.Types;

import org.entirej.framework.core.service.EJParameterType;
import org.entirej.framework.core.service.EJStatementParameter;


public class EJStatementParameterBlob extends EJStatementParameter
{
    private static final int JDBC_TYPE = Types.BLOB;

    public EJStatementParameterBlob(EJParameterType type)
    {
        this(type, null);
    }

    public EJStatementParameterBlob(EJParameterType type, byte[] value)
    {
        super(type);
        setValue(value);
    }

    

    public int getJdbcType()
    {
        return JDBC_TYPE;
    }

}
