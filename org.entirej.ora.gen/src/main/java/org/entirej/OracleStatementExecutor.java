package org.entirej;import java.io.File;import java.io.Reader;import java.sql.Blob;import java.sql.CallableStatement;import java.sql.Clob;import java.sql.Connection;import java.sql.SQLException;import java.sql.Struct;import java.sql.Types;import org.entirej.framework.core.EJApplicationException;import org.entirej.framework.core.EJForm;import org.entirej.framework.core.EJMessage;import org.entirej.framework.core.interfaces.EJFrameworkConnection;import org.entirej.framework.core.service.EJStatementExecutor;import org.entirej.framework.core.service.EJStatementParameter;import org.slf4j.Logger;import org.slf4j.LoggerFactory;import oracle.jdbc.OracleConnection;@SuppressWarnings("serial")public class OracleStatementExecutor extends EJStatementExecutor{    private static Logger logger = LoggerFactory.getLogger(OracleStatementExecutor.class);    public int executePLSQLStoredProcedure(EJForm form, String procedureStatement, EJStatementParameter... parameters)    {        if (form == null)        {            throw new NullPointerException("Form passed to executeStoredProcedure cannot be null");        }        return executePLSQLStoredProcedure(form.getConnection(), procedureStatement, parameters);    }        @SuppressWarnings({ "rawtypes"})    public int executePLSQLStoredProcedure(EJFrameworkConnection fwkConnection, String procedureStatement, EJStatementParameter... parameters)    {    	long start = 0;    	boolean traceEnabled = logger.isTraceEnabled();		if(traceEnabled)		{    		logger.trace("START executePLSQLStoredProcedure");    		start = System.currentTimeMillis();		}                if (fwkConnection == null)        {            throw new NullPointerException("No EJFrameworkConnection passed to OracleStatementExecutor.executeStoredProcedure");        }        CallableStatement proc = null;        try        {            if (fwkConnection.getConnectionObject() == null || !(fwkConnection.getConnectionObject() instanceof Connection))            {                throw new EJApplicationException(new EJMessage("The StatementExecutor requires the ConnectionFactory to return a JDBC Connection but another type was returned"));            }            if(traceEnabled)        		logger.trace("     Getting connection");            Connection base = (Connection) fwkConnection.getConnectionObject();            OracleConnection oraConn = base.unwrap(OracleConnection.class);            if(traceEnabled)        		logger.trace("     Got connection");            if(traceEnabled)        		logger.trace("     Preparing Call");            proc = oraConn.prepareCall(procedureStatement);            if(traceEnabled)        		logger.trace("     Call Prepared");            int pos = 0;            if(traceEnabled)        		logger.trace("     Setting Parameters");            for (EJStatementParameter parameter : parameters)            {                pos++;                switch (parameter.getParameterType())                {                    case IN:                        if (parameter.getValue() == null)                        {                            proc.setNull(pos, parameter.getJdbcType());                        }                        else if (parameter.getJdbcType() == Types.CLOB)                        {                            Clob clob = base.createClob();                            clob.setString(1, (String) parameter.getValue());                            proc.setObject(pos, clob);                        }                        else if (parameter.getJdbcType() == Types.BLOB)                        {                            Blob blob = base.createBlob();                            Object value = parameter.getValue();                            if(value instanceof byte[])                            {                            	blob.setBytes(1,(byte[])value);                            }                            else  if( value instanceof File)                            {                            	blob = EJStatementParameterBlob.createBlobFromFile(blob,(File)value);                            }                            proc.setObject(pos, blob);                        }                        else                        {                            proc.setObject(pos, parameter.getValue());                        }                        break;                    case INOUT:                        parameter.setPosition(pos);                        if (parameter instanceof EJStatementParameterOraArray)                        {                            proc.setObject(pos, ((EJStatementParameterOraArray) parameter).getCollectionType());                        }                        if (parameter instanceof EJStatementParameterOraStruct)                        {                            if (parameter.getValue() == null)                            {                                proc.setNull(pos, parameter.getJdbcType());                            }                            else                            {                                proc.setObject(pos, ((EJStatementParameterOraStruct) parameter).getCollectionType());                            }                        }                        else if (parameter.getJdbcType() == Types.CLOB)                        {                            Clob clob = base.createClob();                            clob.setString(1, (String) parameter.getValue());                            proc.setObject(pos, clob);                        }                        else if (parameter.getJdbcType() == Types.BLOB)                        {                            Blob blob = base.createBlob();                                                                                    Object value = parameter.getValue();                            if(value instanceof byte[])                            {                            	blob.setBytes(1,(byte[])value);                            }                            else  if( value instanceof File)                            {                            	blob = EJStatementParameterBlob.createBlobFromFile(blob,(File)value);                            }                                                                                    proc.setObject(pos, blob);                        }                        else                        {                            if (parameter.getValue() == null)                            {                                proc.setNull(pos, parameter.getJdbcType());                            }                            else                            {                                proc.setObject(pos, parameter.getValue());                            }                        }                        if (parameter.getJdbcType() == Types.ARRAY)                        {                            proc.registerOutParameter(pos, Types.ARRAY, ((EJStatementParameterOraArray) parameter).getCollectionType().getSqlName());                        }                        else if (parameter.getJdbcType() == Types.STRUCT)                        {                            proc.registerOutParameter(pos, Types.STRUCT, ((EJStatementParameterOraStruct) parameter).getCollectionType().getSqlName());                        }                        else                        {                            proc.registerOutParameter(pos, parameter.getJdbcType());                        }                        break;                    case OUT:                    case RETURN:                        if (parameter.getJdbcType() == Types.ARRAY)                        {                            proc.registerOutParameter(pos, Types.ARRAY, ((EJStatementParameterOraArray) parameter).getCollectionType().getSqlName());                        }                        else if (parameter.getJdbcType() == Types.STRUCT)                        {                            proc.registerOutParameter(pos, Types.STRUCT, ((EJStatementParameterOraStruct) parameter).getCollectionType().getSqlName());                        }                        else                        {                            proc.registerOutParameter(pos, parameter.getJdbcType());                        }                        parameter.setPosition(pos);                        break;                }            }            if(traceEnabled)        		logger.trace("     Parameters Set");            if(traceEnabled)        		logger.trace("     Executing ");            proc.execute();            if(traceEnabled)        		logger.trace("     Executing Completed");            if(traceEnabled)        		logger.trace("     Extracting Values");            for (EJStatementParameter parameter : parameters)            {                switch (parameter.getParameterType())                {                    case INOUT:                    case OUT:                    case RETURN:                        extractValue(proc, parameter.getPosition(), parameter);                        break;                    case IN:                        break;                }            }            if(traceEnabled)        		logger.trace("     Values Extracted");        }        catch (Exception e)        {            try            {                if (proc != null)                {                    proc.close();                }            }            catch (SQLException e2)            {            }            throw new EJApplicationException(e.getMessage(), e);        }        finally        {            try            {                if (proc != null)                {                    proc.close();                }            }            catch (SQLException e)            {            }            if(traceEnabled)    		{            	logger.trace("executePLSQLStoredProcedure TIME : "+(System.currentTimeMillis()-start) );    		}        }        if(traceEnabled)    		logger.trace("END executePLSQLStoredProcedure");        return 0;    }    @SuppressWarnings({ "rawtypes", "unchecked" })    private void extractValue(CallableStatement cstmt, int pos, EJStatementParameter parameter) throws SQLException, InstantiationException, IllegalAccessException    {        if (parameter instanceof EJStatementParameterOraArray)        {            EJOraCollectionType type = (EJOraCollectionType) ((EJStatementParameterOraArray) parameter).create(cstmt.getArray(pos));            ((EJStatementParameterOraArray) parameter).setResultCollectionType(type);        }        else if (parameter instanceof EJStatementParameterOraStruct)        {            EJOraCollectionType type = (EJOraCollectionType) ((EJStatementParameterOraStruct) parameter).create((Struct)cstmt.getObject(pos));            ((EJStatementParameterOraStruct) parameter).setResultCollectionType(type);        }        else if (parameter instanceof EJStatementParameterClob)        {            Clob clobValue = cstmt.getClob(pos);            parameter.setValue(convertClobToString(clobValue));        }        else        {            parameter.setValue(cstmt.getObject(pos));        }    }    public static String convertClobToString(Clob clobValue) throws SQLException    {        String data = null;        if (clobValue != null)        {            Reader is = clobValue.getCharacterStream();            // Initialize local variables.            long length = clobValue.length();            // Check CLOB is not empty.            if (length > 0)            {                char[] buffer = new char[1024];                try                {                    final StringBuilder out = new StringBuilder();                    for (;;)                    {                        int rsz = is.read(buffer, 0, buffer.length);                        if (rsz < 0)                            break;                        out.append(buffer, 0, rsz);                    }                    data = out.toString();                }                catch (Exception e)                {                    throw new EJApplicationException(e);                }            }            else            {                data = null;            }        }        else        {            data = (String) null;        }        return data;    }}