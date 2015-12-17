package org.entirej.report;

import java.sql.Types;

import org.entirej.framework.report.service.EJReportParameterType;
import org.entirej.framework.report.service.EJReportStatementParameter;

public class EJStatementParameterClob extends EJReportStatementParameter
{
    private static final int JDBC_TYPE = Types.CLOB;

    public EJStatementParameterClob(EJReportParameterType type)
    {
        this(type, null);
    }

    public EJStatementParameterClob(EJReportParameterType type, String value)
    {
        super(type);
        setValue(value);
    }

    public int getJdbcType()
    {
        return JDBC_TYPE;
    }

}
