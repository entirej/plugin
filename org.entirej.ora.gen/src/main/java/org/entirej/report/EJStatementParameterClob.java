package org.entirej.report;

import java.io.Reader;
import java.sql.Clob;
import java.sql.Types;

import org.entirej.framework.report.EJReportRuntimeException;
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
    
    @Override
	public void setValue(Object value) {

		if (value == null) {
			super.setValue(null);

		}

		else if (value instanceof String) {
			super.setValue(value);

		} else if (value instanceof Clob) {
			
			try {
				long length = ((Clob) value).length();
				// Check CLOB is not empty.
				if (length > 0) {
					try (Reader is = ((Clob) value).getCharacterStream();) {

						// Initialize local variables.

						char[] buffer = new char[1024];

						final StringBuilder out = new StringBuilder();
						for (;;) {
							int rsz = is.read(buffer, 0, buffer.length);
							if (rsz < 0)
								break;
							out.append(buffer, 0, rsz);
						}

						super.setValue(out.toString());

					} finally {

					}
				}
			} catch (Exception e) {
				throw new EJReportRuntimeException(e);
			}
		} else
			throw new IllegalArgumentException("Type not valid" + value.getClass().getName());
	}

    public int getJdbcType()
    {
        return JDBC_TYPE;
    }

}
