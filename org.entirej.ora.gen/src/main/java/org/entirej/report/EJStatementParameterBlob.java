package org.entirej.report;

import java.sql.Blob;
import java.sql.SQLException;
import java.sql.Types;

import org.entirej.framework.report.service.EJReportParameterType;
import org.entirej.framework.report.service.EJReportStatementParameter;

public class EJStatementParameterBlob extends EJReportStatementParameter
{
    private static final int JDBC_TYPE = Types.BLOB;

    public EJStatementParameterBlob(EJReportParameterType type)
    {
        this(type, null);
    }

    public EJStatementParameterBlob(EJReportParameterType type, byte[] value)
    {
        super(type);
        setValue(value);
    }

  

    public int getJdbcType()
    {
        return JDBC_TYPE;
    }

}
