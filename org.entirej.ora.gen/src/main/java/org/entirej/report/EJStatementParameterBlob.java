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

    public Object getValue()
    {
        try
        {
            Blob blob = (Blob) super.getValue();
            if (blob == null)
            {
                return null;
            }
            
            byte[] blobAsBytes = blob.getBytes(1, (int) blob.length());
            blob.free();
            return blobAsBytes;
        }
        catch (SQLException e)
        {
            return null;
        }
    }

    public int getJdbcType()
    {
        return JDBC_TYPE;
    }

}
