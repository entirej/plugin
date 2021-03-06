package ${package_name};

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.LinkedList;
import java.util.Objects;

import org.entirej.framework.report.EJReportFieldName;

import oracle.jdbc.OracleArray;
import oracle.jdbc.OracleData;
import oracle.jdbc.OracleDataFactory;
import oracle.jdbc.OracleTypes;
import oracle.jdbc.OracleStruct;
import oracle.jdbc.driver.OracleConnection;

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



    public String getSqlName()
    {
        return _SQL_NAME;
    }


    /* constructors */
    public ${JAVA_OBJECT_NAME}()
    {
        this((${JAVA_REC_NAME}[]) null);
    }

    public ${JAVA_OBJECT_NAME}(${JAVA_REC_NAME}[] a)
    {
        if (a != null)
        {
            _values = Arrays.asList(a);
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
    } 
    
    public static OracleDataFactory getOracleDataFactory()
    {
        return new ${JAVA_REC_NAME}();
    }

    @Override
    public Object toJDBCObject(Connection conn) throws SQLException
    {
        return ((OracleConnection) conn).createOracleArray(_SQL_NAME, _values.toArray(new ${JAVA_REC_NAME}[0]));
    }

    @Override
    public OracleData create(Object jdbcValue, int arg1) throws SQLException
    {
        if (Objects.isNull(jdbcValue))
        {
            return null;
        }

        Object[] recArray = (Object[]) ((OracleArray) jdbcValue).getArray();

        ArrayList<${JAVA_REC_NAME}> recs = new ArrayList<${JAVA_REC_NAME}>();
        for (Object obj : recArray)
        {
            recs.add((${JAVA_REC_NAME}) obj);

        }

        ${JAVA_OBJECT_NAME} tab = new ${JAVA_OBJECT_NAME}();
        tab.setValues(recs);
        return tab;
    }
    
    
    
    
<#else>
    public static final String _SQL_NAME     = "${DB_OBJECT_NAME}";
    public static final int    _SQL_TYPECODE = OracleTypes.STRUCT;   

    private HashMap<FieldNames<?>, Object> _values        = new HashMap<FieldNames<?>, Object>();
    private HashMap<FieldNames<?>, Object> _initialValues = new HashMap<FieldNames<?>, Object>();

    

    public String getSqlName()
    {
        return _SQL_NAME;
    }

    

    public ${JAVA_OBJECT_NAME}()
    {
    }

        
    private Object toJDBC(Object o,Connection conn) throws SQLException
    {
        if(o instanceof oracle.jdbc.OracleData) 
        {
            return ((oracle.jdbc.OracleData)o).toJDBCObject(conn);
        }
        
        return o;
    }
    
    @Override
    public Object toJDBCObject(Connection conn) throws SQLException
    {
        return conn.createStruct(getSqlName(),
                new Object[] {<#list columns as column> toJDBC(get${column.method_name}(),conn) ,</#list> });
    }
    
    

    @Override
    public OracleData create(Object jdbcValue, int sqltype) throws SQLException
    {
        if (Objects.isNull(jdbcValue))
        {
            return null;
        }

        LinkedList<Object> attr = new LinkedList<>(Arrays.asList(((OracleStruct) jdbcValue).getAttributes()));
        ${JAVA_OBJECT_NAME} r = new ${JAVA_OBJECT_NAME}();
        
        <#list columns as column>
        <#if column.is_array == "true" || column.is_struct == "true">
        r.set${column.method_name}((${column.data_type})${column.data_type}.getOracleDataFactory().create(attr.removeFirst(),${column.data_type}._SQL_TYPECODE)); 
        <#else>        
        r.set${column.method_name}((${column.data_type})attr.removeFirst());
        </#if> 
        
        </#list>
        
        return r;
    }

    public static OracleDataFactory getOracleDataFactory()
    {
        return new ${JAVA_OBJECT_NAME}();
    }
    
    

    public void clearInitialValues()
    {
        _initialValues.clear();
        <#list columns as column>
        
       _initialValues.put(FieldNames.${column.name}, _values.get(FieldNames.${column.name}));
    	</#list>
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
            return getValue(fieldName);
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
        
            _values.put(FieldNames.${column.name}, ${column.var_name});
            if (!_initialValues.containsKey(FieldNames.${column.name}))
            {
                _initialValues.put(FieldNames.${column.name}, ${column.var_name});
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
