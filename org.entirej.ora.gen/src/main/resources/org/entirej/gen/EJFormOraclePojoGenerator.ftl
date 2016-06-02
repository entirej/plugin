package ${package_name};

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;

import org.entirej.EJOraCollectionType;
import org.entirej.framework.core.EJApplicationException;
import org.entirej.framework.core.EJFieldName;
import org.entirej.framework.core.EJManagedFrameworkConnection;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.OracleTypes;
import oracle.jpub.runtime.MutableStruct;
import oracle.sql.Datum;
import oracle.sql.ORAData;
import oracle.sql.ORADataFactory;
import oracle.sql.STRUCT;

<#list imports as import>
import ${import};
</#list>
import java.util.HashMap;

public class ${JAVA_OBJECT_NAME} implements EJOraCollectionType
{
    
<#if TABLE_NAME != "">
    public static final String _SQL_NAME     = "${TABLE_NAME}";
    public static final int    _SQL_TYPECODE = OracleTypes.ARRAY;

    private List<${JAVA_REC_NAME}> _values = new ArrayList<${JAVA_REC_NAME}>();

    MutableArray _array;

    private static final ${JAVA_OBJECT_NAME} _${JAVA_OBJECT_NAME}Factory = new ${JAVA_OBJECT_NAME}();

    public String getSqlName()
    {
        return _SQL_NAME;
    }

    public static ORADataFactory getORADataFactory()
    {
        return _${JAVA_OBJECT_NAME}Factory;
    }

    /* constructors */
    public ${JAVA_OBJECT_NAME}()
    {
        this((${JAVA_REC_NAME}[]) null);
    }

    public ${JAVA_OBJECT_NAME}(${JAVA_REC_NAME}[] a)
    {
        _array = new MutableArray(2002, a, ${JAVA_REC_NAME}.getORADataFactory());
        if (a != null)
        {
            _values = Arrays.asList(a);
        }
    }

    /* ORAData interface */
    public Datum toDatum(Connection c) throws SQLException
    {
        if (c == null || c.isClosed())
        {
            EJManagedFrameworkConnection con = org.entirej.framework.core.EJConnectionHelper.getConnection();
            try
            {
                return _array.toDatum(((Connection) con.getConnectionObject()).unwrap(OracleConnection.class), _SQL_NAME);
            }
            finally
            {
                con.close();
            }
        }
        else
        {
            return _array.toDatum(c, _SQL_NAME);
        }
    }

    /* ORADataFactory interface */
    public ORAData create(Datum d, int sqlType)
    {
        try
        {
            if (d == null)
            {
                return null;
            }

            ${JAVA_OBJECT_NAME} a = new ${JAVA_OBJECT_NAME}();
            a._array = new MutableArray(2002, (ARRAY) d, ${JAVA_REC_NAME}.getORADataFactory());
            a.setValues(Arrays.asList((${JAVA_REC_NAME}[]) a._array.getObjectArray(new ${JAVA_REC_NAME}[a._array.length()])));

            return a;
        }
        catch (SQLException e)
        {
            throw new EJApplicationException(e);
        }
    }

    /* array accessor methods */
    @EJFieldName("VALUES")
    public List<${JAVA_REC_NAME}> getValues()
    {
        return Collections.unmodifiableList(_values);
    }

    @EJFieldName("VALUES")
    public void setValues(List<${JAVA_REC_NAME}> a)
    {
        _values = a;
        _array.setObjectArray(a.toArray(new ${JAVA_REC_NAME}[0]));
    } 
    
    
    
    
    
    
<#else>
    public static final String _SQL_NAME     = "${DB_OBJECT_NAME}";
    public static final int    _SQL_TYPECODE = OracleTypes.STRUCT;   


    protected MutableStruct _struct;

    protected static int[]                   _sqlType                  = { <#list columns as column>${column.JAVA_OBJECT_TYPE}<#if column?has_next >, </#if></#list> };
    protected static ORADataFactory[]        _factory                  = new ORADataFactory[${columns?size}];
    protected static final ${JAVA_OBJECT_NAME} _${JAVA_OBJECT_NAME}Factory = new ${JAVA_OBJECT_NAME}();

    private HashMap<FieldNames<?>, Object> _values        = new HashMap<FieldNames<?>, Object>();
    private HashMap<FieldNames<?>, Object> _initialValues = new HashMap<FieldNames<?>, Object>();

    static
    {
        <#list columns as column>
        <#if column.is_array == "true" || column.is_struct == "true">
        _factory[${column?index}] = ${column.data_type}.getORADataFactory();
        </#if>
        </#list>
    }

    public static ORADataFactory getORADataFactory()
    {
        return _${JAVA_OBJECT_NAME}Factory;
    }

    public String getSqlName()
    {
        return _SQL_NAME;
    }

    /* constructors */
    protected void _init_struct(boolean init)
    {
        if (init)
        {
            _struct = new MutableStruct(new Object[${columns?size}], _sqlType, _factory);
        }
    }

    public ${JAVA_OBJECT_NAME}()
    {
        _init_struct(true);
    }

    public ${JAVA_OBJECT_NAME}(<#list columns as column>${column.data_type} ${column.var_name}<#if column?has_next >, </#if></#list> )
    {
        _init_struct(true);
        <#list columns as column>
        set${column.method_name}(${column.var_name});
        </#list>
    }

    /* ORAData interface */
    public Datum toDatum(Connection c) throws SQLException
    {
        if (c == null || c.isClosed())
        {
            EJManagedFrameworkConnection con = org.entirej.framework.core.EJConnectionHelper.getConnection();
            try
            {
                return _struct.toDatum(((Connection) con.getConnectionObject()).unwrap(OracleConnection.class), _SQL_NAME);
            }
            finally
            {
                con.close();
            }
        }
        else
        {
            return _struct.toDatum(c, _SQL_NAME);
        }
    }

    /* ORADataFactory interface */
    public ORAData create(Datum d, int sqlType)
    {
        try
        {
            return create(null, d, sqlType);
        }
        catch (SQLException e)
        {
            throw new EJApplicationException(e);
        }
    }

    protected ORAData create(${JAVA_OBJECT_NAME} o, Datum d, int sqlType) throws SQLException
    {
        if (d == null)
        {
            return null;
        }
        if (o == null)
        {
            o = new ${JAVA_OBJECT_NAME}();
        }
        
        MutableStruct struct = new MutableStruct((STRUCT) d, _sqlType, _factory);
        
        <#list columns as column>
        o.set${column.method_name}((${column.data_type}) struct.getAttribute(${column?index}));
        </#list>

        return o;
    }

    public void clearInitialValues()
    {
        _initialValues.clear();
    }

    public void clearValues()
    {
        _values.clear();
    }

    @SuppressWarnings("unchecked")
    public <T> T getInitialValue(FieldNames<T> fieldName)
    {
        if (_initialValues.containsKey(fieldName))
        {
            return (T) _initialValues.get(fieldName);
        }
        else
        {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getValue(FieldNames<T> fieldName)
    {
        if (_values.containsKey(fieldName))
        {
            return (T) _values.get(fieldName);
        }
        else
        {
            return null;
        }
    }

    /* accessor methods */

/***********************************************************************************************/
    <#list columns as column>
    @EJFieldName("${column.name}")
    public ${column.data_type} get${column.method_name}()
    {
      return getValue(FieldNames.${column.name});
    }
    
    @EJFieldName("${column.name}")
    public void set${column.method_name}(${column.data_type} ${column.var_name})
    {
        try
        {
            _struct.setAttribute(${column?index}, ${column.var_name});
            _values.put(FieldNames.${column.name}, ${column.var_name});
            if (!_initialValues.containsKey(FieldNames.${column.name}))
            {
                _initialValues.put(FieldNames.${column.name}, ${column.var_name});
            }
        }
        catch (SQLException e)
        {
            throw new EJApplicationException(e);
        }
        
    }
    
</#list>


    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        
        
        
        
        <#list columns as column>
        builder.append("${column.name}").append(" : ").append(getValue(FieldNames.${column.name})).append(" : ").append(getInitialValue(FieldNames.${column.name})).append("\n");
        </#list>
        
        
        return builder.toString();
    }

/***********************************************************************************************/

    public static class FieldNames<T>
    {
    
    <#list columns as column>
        public static final FieldNames<${column.data_type}> ${column.name} = new FieldNames<>();
    </#list>
        T type;
    }
</#if>
}
