package org.entirej.report;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.sql.Types;

import org.entirej.framework.report.EJReportRuntimeException;
import org.entirej.framework.report.service.EJReportParameterType;
import org.entirej.framework.report.service.EJReportStatementParameter;

public class EJStatementParameterBlob extends EJReportStatementParameter
{
    private static final int JDBC_TYPE = Types.BLOB;

    public EJStatementParameterBlob(EJReportParameterType type)
    {
        super(type);
    }

    public EJStatementParameterBlob(EJReportParameterType type, byte[] value)
    {
        super(type);
        setValue(value);
    }
    
    public EJStatementParameterBlob(EJReportParameterType type, File value)
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

		else if (value instanceof byte[]) {
			super.setValue(value);

		} else if (value instanceof File) {
			super.setValue(value);
			return;
		} else if (value instanceof Blob) {
			try (InputStream binaryStream = ((Blob) value).getBinaryStream();
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();) {

				copyStream(binaryStream, outputStream);

				super.setValue(outputStream.toByteArray());
			} catch (SQLException | IOException e) {
				throw new EJReportRuntimeException(e);
			}
		} else
			throw new IllegalArgumentException("Type not valid" + value.getClass().getName());
	}
    
    
    public static void copyStream(InputStream input, OutputStream output) throws IOException {
		byte[] buffer = new byte[1024];
		int bytesRead;
		while ((bytesRead = input.read(buffer)) != -1) {
			output.write(buffer, 0, bytesRead);
		}
	}

	public static Blob createBlobFromFile(Blob blob, File file) {

		try {
			OutputStream outStream = blob.setBinaryStream(0);
			FileInputStream blobStream = new FileInputStream(file);

			try {

				copyStream(blobStream, outStream);

			} finally {
				blobStream.close();
				outStream.close();
			}

			return blob;
		} catch (SQLException e) {
			throw new EJReportRuntimeException(e);
		} catch (IOException e) {
			throw new EJReportRuntimeException(e);
		}
	}
}
