package org.entirej;

import java.io.Reader;
import java.sql.Clob;
import java.sql.Types;

import org.entirej.framework.core.EJApplicationException;
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
						((Clob) value).free();
					}
				}
			} catch (Exception e) {
				throw new EJApplicationException(e);
			}
		} else
			throw new IllegalArgumentException("Type not valid" + value.getClass().getName());
	}

}
