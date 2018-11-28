package org.entirej.report;import java.io.Reader;import java.sql.Array;import java.sql.Blob;import java.sql.CallableStatement;import java.sql.Clob;import java.sql.Connection;import java.sql.SQLException;import java.sql.Struct;import java.sql.Types;import java.util.ArrayList;import java.util.List;import org.entirej.EJOraCollectionType;import org.entirej.framework.report.EJReport;import org.entirej.framework.report.EJReportRuntimeException;import org.entirej.framework.report.interfaces.EJReportFrameworkConnection;import org.entirej.framework.report.service.EJReportStatementExecutor;import org.entirej.framework.report.service.EJReportStatementParameter;import org.slf4j.Logger;import org.slf4j.LoggerFactory;import oracle.jdbc.OracleConnection;import oracle.sql.ORAData;public class OracleStatementExecutor extends EJReportStatementExecutor{    private static Logger logger = LoggerFactory.getLogger(OracleStatementExecutor.class);   // static final ThreadLocal<OracleConnection> CONNECTION_THREAD_LOCAL = new ThreadLocal<OracleConnection>();    public int executePLSQLStoredProcedure(EJReport form, String procedureStatement, EJReportStatementParameter... parameters)    {        if (form == null)        {            throw new NullPointerException("Form passed to executeStoredProcedure cannot be null");        }        return executePLSQLStoredProcedure(form.getConnection(), procedureStatement, parameters);    }    public int executePLSQLStoredProcedure(EJReportFrameworkConnection fwkConnection, String procedureStatement, EJReportStatementParameter... parameters)    {    	long start = 0;    	boolean traceEnabled = logger.isTraceEnabled();		if(traceEnabled)		{    		logger.trace("START executePLSQLStoredProcedure");    		start = System.currentTimeMillis();		}                if (fwkConnection == null)        {            throw new NullPointerException("No EJFrameworkConnection passed to OracleStatementExecutor.executeStoredProcedure");        }        CallableStatement proc = null;        try        {            if (fwkConnection.getConnectionObject() == null || !(fwkConnection.getConnectionObject() instanceof Connection))            {                throw new EJReportRuntimeException(("The StatementExecutor requires the ConnectionFactory to return a JDBC Connection but another type was returned"));            }            if(traceEnabled)        		logger.trace("     Getting connection");            Connection base = (Connection) fwkConnection.getConnectionObject();            OracleConnection oraConn = base.unwrap(OracleConnection.class);            if(traceEnabled)        		logger.trace("     Got connection");          //  CONNECTION_THREAD_LOCAL.set(oraConn);            if(traceEnabled)        		logger.trace("     Preparing Call");            proc = oraConn.prepareCall(procedureStatement);            if(traceEnabled)        		logger.trace("     Call Prepared");            int pos = 0;            if(traceEnabled)        		logger.trace("     Setting Parameters");            for (EJReportStatementParameter parameter : parameters)            {                pos++;                switch (parameter.getParameterType())                {                    case IN:                        if (parameter.getValue() == null)                        {                            proc.setNull(pos, parameter.getJdbcType());                        }                        else if (parameter.getJdbcType() == Types.CLOB)                        {                            Clob clob = base.createClob();                            clob.setString(1, (String) parameter.getValue());                            proc.setObject(pos, clob);                        }                        else if (parameter.getJdbcType() == Types.BLOB)                        {                            Blob blob = base.createBlob();                            blob.setBytes(1, (byte[])parameter.getValue());                            proc.setObject(pos, blob);                        }                        else                        {                            proc.setObject(pos, parameter.getValue());                        }                        break;                    case INOUT:                        parameter.setPosition(pos);                        if (parameter instanceof EJStatementParameterOraArray)                        {                            proc.setObject(pos, ((EJStatementParameterOraArray) parameter).getCollectionType());                                                    }                        if (parameter instanceof EJStatementParameterOraStruct)                        {                            if (parameter.getValue() == null)                            {                                proc.setNull(pos, parameter.getJdbcType());                            }                            else                            {                                proc.setObject(pos, ((EJStatementParameterOraStruct) parameter).getCollectionType());//                                proc.setObject(pos, (Struct) ((EJStatementParameterOraStruct) parameter).getCollectionType());                            }                        }                        else if (parameter.getJdbcType() == Types.CLOB)                        {                            Clob clob = base.createClob();                            clob.setString(1, (String) parameter.getValue());                            proc.setObject(pos, clob);                        }                        else if (parameter.getJdbcType() == Types.BLOB)                        {                            Blob blob = base.createBlob();                            blob.setBytes(1,(byte[])parameter.getValue());                            proc.setObject(pos, blob);                        }                        else                        {                            if (parameter.getValue() == null)                            {                                proc.setNull(pos, parameter.getJdbcType());                            }                            else                            {                                proc.setObject(pos, parameter.getValue());                            }                        }                        if (parameter.getJdbcType() == Types.ARRAY)                        {                            proc.registerOutParameter(pos, Types.ARRAY, ((EJStatementParameterOraArray) parameter).getCollectionType().getSqlName());                        }                        else if (parameter.getJdbcType() == Types.STRUCT)                        {                            proc.registerOutParameter(pos, Types.STRUCT, ((EJStatementParameterOraStruct) parameter).getCollectionType().getSqlName());                        }                        else                        {                            proc.registerOutParameter(pos, parameter.getJdbcType());                        }                        break;                    case OUT:                    case RETURN:                        if (parameter.getJdbcType() == Types.ARRAY)                        {                            proc.registerOutParameter(pos, Types.ARRAY, ((EJStatementParameterOraArray) parameter).getCollectionType().getSqlName());                        }                        else if (parameter.getJdbcType() == Types.STRUCT)                        {                            proc.registerOutParameter(pos, Types.STRUCT, ((EJStatementParameterOraStruct) parameter).getCollectionType().getSqlName());                        }                        else                        {                            proc.registerOutParameter(pos, parameter.getJdbcType());                        }                        parameter.setPosition(pos);                        break;                }            }            if(traceEnabled)        		logger.trace("     Parameters Set");            if(traceEnabled)        		logger.trace("     Executing ");            proc.execute();            if(traceEnabled)        		logger.trace("     Executing Completed");            if(traceEnabled)        		logger.trace("     Extracting Values");            for (EJReportStatementParameter parameter : parameters)            {                switch (parameter.getParameterType())                {                    case INOUT:                    case OUT:                    case RETURN:                        extractValue(proc, parameter.getPosition(), parameter);                        break;                    case IN:                        break;                }            }            if(traceEnabled)        		logger.trace("     Values Extracted");        }        catch (Exception e)        {            try            {                if (proc != null)                {                    proc.close();                }            }            catch (SQLException e2)            {            }            throw new EJReportRuntimeException(e.getMessage(), e);        }        finally        {            try            {                if (proc != null)                {                    proc.close();                }            }            catch (SQLException e)            {            }            if(traceEnabled)    		{            	logger.trace("executePLSQLStoredProcedure TIME : "+(System.currentTimeMillis()-start) );    		}        }        if(traceEnabled)    		logger.trace("END executePLSQLStoredProcedure");        return 0;    }    @SuppressWarnings("rawtypes")    private void extractValue(CallableStatement cstmt, int pos, EJReportStatementParameter parameter) throws SQLException, InstantiationException, IllegalAccessException    {        if (parameter instanceof EJStatementParameterOraArray)        {            EJOraCollectionType type = (EJOraCollectionType) ((EJStatementParameterOraArray) parameter).create(cstmt.getArray(pos));            ((EJStatementParameterOraArray) parameter).setResultCollectionType(type);            // Array arrayValue = cstmt.getArray(pos);            // extractArrayValue(arrayValue, (EJStatementParameterOraArray)            // parameter);        }        else if (parameter instanceof EJStatementParameterOraStruct)        {            EJOraCollectionType type = (EJOraCollectionType) ((EJStatementParameterOraStruct) parameter).create((Struct)cstmt.getObject(pos));            ((EJStatementParameterOraStruct) parameter).setResultCollectionType(type);            // Struct structValue = (Struct) cstmt.getObject(pos);            // extractStructValue(structValue,            // (EJStatementParameterOraStruct<?>) parameter);        }        else if (parameter instanceof EJStatementParameterClob)        {            Clob clobValue = cstmt.getClob(pos);            parameter.setValue(convertClobToString(clobValue));        }        else        {            parameter.setValue(cstmt.getObject(pos));        }    }    public static String convertClobToString(Clob clobValue) throws SQLException    {        String data = null;        if (clobValue != null)        {            Reader is = clobValue.getCharacterStream();            // Initialize local variables.            long length = clobValue.length();            // Check CLOB is not empty.            if (length > 0)            {                char[] buffer = new char[1024];                try                {                    final StringBuilder out = new StringBuilder();                    for (;;)                    {                        int rsz = is.read(buffer, 0, buffer.length);                        if (rsz < 0)                            break;                        out.append(buffer, 0, rsz);                    }                    data = out.toString();                }                catch (Exception e)                {                    throw new EJReportRuntimeException(e);                }            }            else            {                data = null;            }        }        else        {            data = (String) null;        }        return data;    }    @SuppressWarnings("unchecked")    public synchronized static <E> void extractArrayValue(Array arrayValue, EJStatementParameterOraArray parameter)    {    	boolean traceEnabled = logger.isTraceEnabled();		if(traceEnabled)    		logger.trace("START  extractArrayValue");        try        {        	if(traceEnabled)        		logger.trace("START  Creating value Array");            List<E> valueArray = new ArrayList<E>();            if(traceEnabled)        		logger.trace("START  creating Datum Array");            Object[] datumArray = (arrayValue != null) ? (Object[]) arrayValue.getArray() : null;            if(traceEnabled)        		logger.trace("DONE  Datum Array Created");            // parameter.create(arrayValue);            if(traceEnabled)        		logger.trace("START  extracting resultset");            if (datumArray != null)            {                for (Object element : datumArray)                {                    if (element != null)                    {                    	if(traceEnabled)                    		logger.trace("          -> getting new ClassInstance {} ", parameter.getFieldName());                        ORAData data = (ORAData) parameter.getCollectionType();                        if(traceEnabled)                    		logger.trace("          -> reading SQL ");                        // data.readSQL(new EJSQLInput((Struct) element),                        // data.getSQLTypeName());                        if(traceEnabled)                    		logger.trace("          -> adding to array");                        valueArray.add((E) data);                    }                    else                    {                        valueArray.add(null);                    }                }            }            if(traceEnabled)        		logger.trace("END  extracting resultset");            parameter.setValue(valueArray);            if(traceEnabled)        		logger.trace("END   extractArrayValue");        }        catch (SQLException e)        {            throw new RuntimeException(e);        }    }    //    // @SuppressWarnings("unchecked")    // public synchronized static <E> void extractStructValue(Struct    // structValue, EJStatementParameterOraStruct<?> parameter)    // {    // try    // {    // if (structValue != null)    // {    // ORAData data = (ORAData) parameter.getClassInstance().newInstance();    //// data.readSQL(new EJSQLInput(structValue), data.getSQLTypeName());    // parameter.setValue((E) data);    // }    // else    // {    // parameter.setValue(null);    // }    // }    // catch (InstantiationException e)    // {    // throw new RuntimeException(e);    // }    // catch (IllegalAccessException e)    // {    // throw new RuntimeException(e);    // }    //// catch (SQLException e)    //// {    //// throw new RuntimeException(e);    //// }    // }    //    // public static Array createArray(EJStatementParameterOraArray parameter)    // {    // try    // {    // OracleConnection connection = CONNECTION_THREAD_LOCAL.get();    // if (connection == null)    // {    // throw new IllegalStateException();    // }    //    // EjResultsTab tab = (EjResultsTab)parameter.getArrayType();    //    //// ArrayDescriptor arraydescriptor =    // ArrayDescriptor.createDescriptor(((EJStatementParameterOraArray)    // parameter).getArrayTypeName(), connection);    //    // Array aArray = new ARRAY(tab.getDescriptor(),    // tab.getDescriptor().getInternalConnection(), tab.getArray());    //    //// Array aArray = new ARRAY(arraydescriptor,    // arraydescriptor.getInternalConnection(), ((EJStatementParameterOraArray)    // parameter).getArray());    //    // return aArray;    // }    // catch (SQLException e)    // {    // e.printStackTrace();    // throw new RuntimeException(e);    // }    // }}